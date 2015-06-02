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
package de.tum.in.hsql.conf;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is used to configure HypeSQL Connection
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.hsql.conf")
@Service(value = { HSqlConfiguration.class })
public class HSqlConfiguration implements ConfigurableComponent {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HSqlConfiguration.class);

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.hsql.conf";

	/**
	 * Configurable Property to set HyperSQL Connection URL
	 */
	private static final String DB_SERVICE_URL = "db.service.hsqldb.url";

	/**
	 * Configurable Property to set caching of rows
	 */
	private static final String DB_CACHE_ROWS = "db.service.hsqldb.cache_rows";

	/**
	 * Configurable Property to set lob file scaling size
	 */
	private static final String DB_LOB_FILE_SCALE = "db.service.hsqldb.lob_file_scale";

	/**
	 * Configurable Property to set defragmentation limit
	 */
	private static final String DB_DEFRAG_LIMIT = "db.service.hsqldb.defrag_limit";

	/**
	 * Configurable Property to set log data
	 */
	private static final String DB_LOG_DATA = "db.service.hsqldb.log_data";

	/**
	 * Configurable Property to set log size
	 */
	private static final String DB_LOG_SIZE = "db.service.hsqldb.log_size";

	/**
	 * Configurable Property to set NIO data file
	 */
	private static final String DB_NIO_DATA_FILE = "db.service.hsqldb.nio_data_file";

	/**
	 * Configurable Property to set write delay in milliseconds
	 */
	private static final String DB_WRITE_DELAY = "db.service.hsqldb.write_delay_millis";

	/**
	 * Eclipse Kura System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/* Constructor */
	public HSqlConfiguration() {
	}

	/**
	 * Callback to be used while {@link SystemService} is registering
	 */
	public synchronized void bindSystemService(SystemService systemService) {
		if (m_systemService == null)
			m_systemService = systemService;
	}

	/**
	 * Callback to be used while {@link SystemService} is deregistering
	 */
	public synchronized void unbindSystemService(SystemService systemService) {
		if (m_systemService == systemService)
			m_systemService = null;
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(ComponentContext componentContext,
			Map<String, Object> properties) {
		LOGGER.info("Activating HyperSQL Configuration Component...");

		m_properties = properties;
		setConfiguration();

		LOGGER.info("Activating HyperSQL Configuration Component... Done.");

	}

	/**
	 * Sets the configuration parameters to system service
	 */
	private void setConfiguration() {
		m_systemService.getProperties().put(DB_SERVICE_URL,
				m_properties.get(DB_SERVICE_URL));

		m_systemService.getProperties().put(DB_CACHE_ROWS,
				m_properties.get(DB_CACHE_ROWS));

		m_systemService.getProperties().put(DB_DEFRAG_LIMIT,
				m_properties.get(DB_DEFRAG_LIMIT));

		m_systemService.getProperties().put(DB_LOB_FILE_SCALE,
				m_properties.get(DB_LOB_FILE_SCALE));

		m_systemService.getProperties().put(DB_LOG_DATA,
				m_properties.get(DB_LOG_DATA));

		m_systemService.getProperties().put(DB_LOG_SIZE,
				m_properties.get(DB_LOG_SIZE));

		m_systemService.getProperties().put(DB_NIO_DATA_FILE,
				m_properties.get(DB_NIO_DATA_FILE));

		m_systemService.getProperties().put(DB_WRITE_DELAY,
				m_properties.get(DB_WRITE_DELAY));
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Deactivate
	protected void deactivate(ComponentContext context) {
		LOGGER.debug("Deactivating HyperSQL Configuration Component...");

		LOGGER.debug("Deactivating HyperSQL Configuration Component... Done.");
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(Map<String, Object> properties) {
		LOGGER.info("Updated HyperSQL Configuration Component...");

		m_properties = properties;
		setConfiguration();
		properties.keySet().forEach(
				s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updated HyperSQL Configuration Component... Done.");
	}

}