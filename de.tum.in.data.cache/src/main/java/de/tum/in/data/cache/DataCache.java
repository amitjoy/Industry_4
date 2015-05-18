/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tum.in.data.cache;

import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.tum.in.data.format.RealtimeData;

/**
 * OSGi Event Listener to cache the data in a Concurrent Map
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
@Component
public class DataCache implements EventHandler {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DataCache.class);

	/**
	 * Common OSGi Topic Placeholder to cache data
	 */
	private static final String DATA_CACHE_TOPIC = "de/tum/in/device/cache/*";

	/**
	 * Placeholder to store the device address from the event properties
	 */
	private String m_deviceAddress;

	/**
	 * Placeholder to store the data timestamp name from the event properties
	 */
	private String m_timestamp;

	/**
	 * Placeholder to store the data from the event properties
	 */
	private String m_realtimeData;

	/**
	 * The cache to store data
	 */
	@SuppressWarnings("unchecked")
	private Cache<String, Object> m_cache;

	/**
	 * The callback while the component gets registered in the service registry
	 */
	@Activate
	protected synchronized void activate(ComponentContext componentContext) {
		LOGGER.info("Activating Caching Component...");
		m_cache = CacheBuilder.newBuilder().concurrencyLevel(5).weakValues()
				.maximumSize(50000).expireAfterWrite(2, TimeUnit.HOURS)
				.removalListener(new RemoveRealtimeDataListener()).build();
		LOGGER.info("Activating Caching Component...Done");
	}

	/**
	 * The callback while the component gets deregistered in the service
	 * registry
	 */
	@Deactivate
	protected synchronized void deactivate(ComponentContext componentContext) {
		LOGGER.info("Deactivating Caching Component...");
		m_cache.cleanUp();
		m_cache = null;
		LOGGER.info("Deactivating Caching Component...Done");
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(Event event) {
		LOGGER.debug("Cache Event Handler starting....");

		Preconditions.checkNotNull(event);
		if (DATA_CACHE_TOPIC.startsWith(event.getTopic())) {
			LOGGER.debug("Cache Event Handler caching....");

			// Extract all the event properties
			m_deviceAddress = (String) event.getProperty("device.id");
			m_timestamp = (String) event.getProperty("timestamp");
			m_realtimeData = (String) event.getProperty("data");

			// Prepare the data and wrap it
			final RealtimeData data = new RealtimeData.Builder()
					.setDeviceAddress(m_deviceAddress)
					.setTimestamp(m_timestamp).setValue(m_realtimeData).build();

			// Now put the data in the cache
			m_cache.put(String.valueOf(System.currentTimeMillis()), data);

			LOGGER.debug("Cache Event Handler Caching...done");
		}
	}
}
