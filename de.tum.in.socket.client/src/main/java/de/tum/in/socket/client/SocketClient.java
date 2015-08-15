/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
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
package de.tum.in.socket.client;

import java.util.Map;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.activity.log.ActivityLogService;
import de.tum.in.activity.log.IActivityLogService;

/**
 * This bundle is responsible for communicating with the Socket Server
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.socket.client")
@Service(value = { SocketClient.class })
public class SocketClient extends Cloudlet implements ConfigurableComponent {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "SOCKET-V1";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SocketClient.class);

	/**
	 * Configurable property to set Socket Server Connection Port Number
	 */
	private static final String SOCKET_CONNECT_PORT = "socket.connect.port";

	/**
	 * Configurable property to set socket server IP address
	 */
	private static final String SOCKET_IP = "socket.server.ip";

	/**
	 * Activity Log Service Dependency
	 */
	@Reference(bind = "bindActivityLogService", unbind = "unbindActivityLogService")
	private volatile IActivityLogService m_activityLogService;

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
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * Placeholder for socket IP Address
	 */
	private String m_socketIPAddress;

	/**
	 * Placeholder for socket Port
	 */
	private Integer m_socketPort;

	/* Constructor */
	public SocketClient() {
		super(APP_ID);
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating Socket Client Component...");

		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);
		this.reinitializeConfiguration(properties);

		LOGGER.info("Activating Socket Client Component... Done.");

	}

	/**
	 * Callback to be used while {@link ActivityLogService} is registering
	 */
	public synchronized void bindActivityLogService(final IActivityLogService activityLogService) {
		if (this.m_activityLogService == null) {
			this.m_activityLogService = activityLogService;
		}
	}

	/**
	 * Callback to be used while {@link CloudService} is registering
	 */
	public synchronized void bindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == null) {
			super.setCloudService(this.m_cloudService = cloudService);
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is registering
	 */
	public synchronized void bindConfigurationService(final ConfigurationService configurationService) {
		if (this.m_configurationService == null) {
			this.m_configurationService = configurationService;
		}
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating OPC-UA Component...");

		LOGGER.debug("Deactivating OPC-UA Component... Done.");
	}

	/** {@inheritDoc}} */
	@Override
	protected void doExec(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Socket Communication Started...");

		// TODO Socket Client Logic
		this.m_activityLogService.saveLog("Socket Communication Started");

		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

		LOGGER.info("Socket Communication Done");
	}

	/** {@inheritDoc}} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("OPC-UA Configuration Retrieving...");
		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			final ComponentConfiguration configuration = this.m_configurationService.getComponentConfiguration(APP_ID);

			final IterableMap map = new HashedMap(configuration.getConfigurationProperties());
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}
			this.m_activityLogService.saveLog("Socket Client Configuration Retrieved");

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("Socket Client Configuration Retrieved");
	}

	/** {@inheritDoc}} */
	@Override
	protected void doPut(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Socket Client Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			this.m_configurationService.updateConfiguration(APP_ID, reqPayload.metrics());

			this.m_activityLogService.saveLog("Socket Client Configuration Updated");
			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("Socket Client Configuration Updated");
	}

	/**
	 * Used to extract configuration for populating placeholders with respective
	 * values
	 */
	private void extractConfiguration() {
		this.m_socketIPAddress = (String) this.m_properties.get(SOCKET_IP);
		this.m_socketPort = (Integer) this.m_properties.get(SOCKET_CONNECT_PORT);
	}

	/**
	 * Reinitialize Configurations as it gets updated
	 */
	private void reinitializeConfiguration(final Map<String, Object> properties) {
		this.m_properties = properties;
		this.extractConfiguration();
	}

	/**
	 * Callback to be used while {@link ActivityLogService} is deregistering
	 */
	public synchronized void unbindActivityLogService(final IActivityLogService activityLogService) {
		if (this.m_activityLogService == activityLogService) {
			this.m_activityLogService = null;
		}
	}

	/**
	 * Callback to be used while {@link CloudService} is deregistering
	 */
	public synchronized void unbindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == cloudService) {
			super.setCloudService(this.m_cloudService = null);
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is deregistering
	 */
	public synchronized void unbindConfigurationService(final ConfigurationService configurationService) {
		if (this.m_configurationService == configurationService) {
			this.m_configurationService = null;
		}
	}

	/**
	 * Used to be called when configurations get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updated Socket Client Component...");

		this.reinitializeConfiguration(properties);

		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updated Socket Client Component... Done.");
	}

}