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

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.tum.in.data.format.MillingMachineData;
import de.tum.in.data.format.RealtimeData;
import de.tum.in.data.util.CacheUtil;

/**
 * OSGi Event Listener to cache the data in a Concurrent Map
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class DataCache implements EventHandler {

	/**
	 * Common OSGi Topic Placeholder to cache data
	 */
	private static final String DATA_CACHE_TOPIC = "de/tum/in/device/cache/*";

	/**
	 * Placeholder to store the data format class name from the event properties
	 */
	private Class<RealtimeData> m_dataFormatClass;

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
	private final Cache<String, Object> CACHE = CacheBuilder.newBuilder()
			.concurrencyLevel(5).weakValues().maximumSize(50000)
			.removalListener(new RemoveRealtimeDataListener()).build();

	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(Event event) {
		if (DATA_CACHE_TOPIC.startsWith(event.getTopic())) {

			m_dataFormatClass = ((Class<RealtimeData>) event
					.getProperty("class"));
			m_deviceAddress = (String) event.getProperty("device.id");
			m_timestamp = (String) event.getProperty("timestamp");
			m_realtimeData = (String) event.getProperty("data");

			final MillingMachineData data = new MillingMachineData(
					m_deviceAddress, m_realtimeData, m_timestamp);

			final RealtimeData format = CacheUtil.convert(data,
					m_dataFormatClass);

			CACHE.put(String.valueOf(System.currentTimeMillis()), format);
		}
	}

}
