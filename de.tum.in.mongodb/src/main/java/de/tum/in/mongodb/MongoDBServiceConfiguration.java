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
package de.tum.in.mongodb;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * Used to consume all the service record provided by all the paired Bluetooth
 * Enabled Milling Machines
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = false, name = "de.tum.in.mongodb")
@Service(value = { MongoDBServiceConfiguration.class })
public class MongoDBServiceConfiguration extends Cloudlet implements
		ConfigurableComponent {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MongoDBServiceConfiguration.class);

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "MONGODB-V1";

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.mongodb";

	/**
	 * Configurable property to set Mongo DB Server Address
	 */
	private static final String MONGO_DB_HOST = "mongo.db.host";

	/**
	 * Configurable Property to set Mongo DB Server Port No
	 */
	private static final String MONGO_DB_PORT = "mongo.db.port";

	/**
	 * Configurable Property to set Mongo DB Database Name
	 */
	private static final String MONGO_DB_DBNAME = "mongo.db.dbname";

	/**
	 * Configurable Property for topic to publish realtime data
	 */
	private static final String MONGO_DB_USERNAME = "mongo.db.username";

	/**
	 * Configurable Property to set Mongo DB Password
	 */
	private static final String MONGO_DB_PASSWORD = "mongo.db.password";

	/**
	 * Eclipse Kura Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Eclipse Kura Configuration Service Dependency
	 */
	@Reference(bind = "bindConfigurationService", unbind = "unbindConfigurationService")
	private volatile ConfigurationService m_configurationService;

	/**
	 * Service Component Context
	 */
	private ComponentContext m_context;

	/**
	 * Place holder for host
	 */
	private String m_host;

	/**
	 * Place holder for port
	 */
	private int m_port;

	/**
	 * Place holder for db name
	 */
	private String m_dbname;

	/**
	 * Place holder for username
	 */
	private String m_username;

	/**
	 * Place holder for password
	 */
	private String m_password;

	/**
	 * Place holder for MongoDB Client
	 */
	private MongoClient m_mongoClient;;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/* Constructor */
	public MongoDBServiceConfiguration() {
		super(APP_ID);
	}

	/**
	 * Callback to be used while {@link CloudService} is registering
	 */
	public synchronized void bindCloudService(CloudService cloudService) {
		if (m_cloudService == null) {
			super.setCloudService(m_cloudService = cloudService);
		}
	}

	/**
	 * Callback to be used while {@link CloudService} is deregistering
	 */
	public synchronized void unbindCloudService(CloudService cloudService) {
		if (m_cloudService == cloudService)
			super.setCloudService(m_cloudService = null);
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is registering
	 */
	public synchronized void bindConfigurationService(
			ConfigurationService configurationService) {
		if (m_configurationService == null) {
			m_configurationService = configurationService;
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is deregistering
	 */
	public synchronized void unbindConfigurationService(
			ConfigurationService configurationService) {
		if (m_configurationService == configurationService)
			m_configurationService = null;
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(ComponentContext componentContext,
			Map<String, Object> properties) {
		LOGGER.info("Activating MongoDB Component...");

		super.setCloudService(m_cloudService);
		super.activate(componentContext);
		m_context = componentContext;

		doRegister(componentContext, properties);

		LOGGER.info("Activating MongoDB Component... Done.");

	}

	/**
	 * Register MongoDB Service
	 */
	private void doRegister(ComponentContext componentContext,
			Map<String, Object> properties) {
		m_properties = properties;
		m_host = (String) m_properties.get(MONGO_DB_HOST);
		m_port = (int) m_properties.get(MONGO_DB_PORT);
		m_dbname = (String) m_properties.get(MONGO_DB_DBNAME);
		m_username = (String) m_properties.get(MONGO_DB_USERNAME);
		m_password = (String) m_properties.get(MONGO_DB_PASSWORD);

		if (m_username != null) {
			final MongoCredential credential = MongoCredential
					.createCredential("user1", "test",
							"password1".toCharArray());
			m_mongoClient = new MongoClient(new ServerAddress(m_host),
					Arrays.asList(credential));
			LOGGER.info("Authenticated as '" + m_username + "'");
		}

		registerMongoDBService(componentContext);
	}

	private void registerMongoDBService(ComponentContext componentContext) {
		final Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put("dbName", m_dbname);
		final MongoDBService dbService = new MongoDBServiceImpl(m_mongoClient,
				m_mongoClient.getDatabase(m_dbname));
		componentContext.getBundleContext().registerService(
				MongoDBService.class.getName(), dbService, properties);
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(ComponentContext context) {
		LOGGER.debug("Deactivating MongoDB Component...");
		LOGGER.info("Releasing CloudApplicationClient for {}...", APP_ID);

		super.deactivate(context);

		LOGGER.debug("Deactivating MongoDB Component... Done.");
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(Map<String, Object> properties) {
		LOGGER.info("Updated MongoDB Component...");

		m_properties = properties;
		properties.keySet().forEach(
				s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));
		doRegister(m_context, properties);

		LOGGER.info("Updated MongoDB Component... Done.");
	}

}