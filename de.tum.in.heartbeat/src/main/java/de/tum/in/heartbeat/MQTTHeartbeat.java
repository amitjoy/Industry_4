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
package de.tum.in.heartbeat;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.component.ComponentContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import de.tum.in.scheduler.annotations.RepeatForever;
import de.tum.in.scheduler.annotations.RepeatInterval;

/**
 * This is used to broadcast MQTT Heartbeat messages
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
@RepeatForever
@RepeatInterval(period = RepeatInterval.SECOND, value = 10)
@Component(immediate = false, name = "de.tum.in.mqtt.heartbeat")
@Service(value = { MQTTHeartbeat.class, Job.class })
public class MQTTHeartbeat extends Cloudlet implements ConfigurableComponent,
		Job {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MQTTHeartbeat.class);

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "HEARTBEAT-V1";

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.mqtt.heartbeat";

	/**
	 * Eclipse Kura Cloud Service Dependency
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Configurable property to set MQTT Hearbeat Topic Namespace
	 */
	private static final String HEARTBEAT_TOPIC = "de.tum.in.mqtt.heartbeat.topic";

	/**
	 * Service Component Context
	 */
	private ComponentContext m_context;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/* Constructor */
	public MQTTHeartbeat() {
		super(APP_ID);
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(ComponentContext componentContext,
			Map<String, Object> properties) {
		LOGGER.info("Activating MQTT Heartbeat Component...");

		m_properties = properties;
		super.setCloudService(m_cloudService);
		super.activate(componentContext);
		m_context = componentContext;

		LOGGER.info("Activating MQTT Heartbeat Component... Done.");

	}

	/**
	 * Broadcasts the heartbeat message
	 */
	private void doBroadcastHeartbeat(Map<String, Object> properties)
			throws KuraException {
		LOGGER.info("Sending MQTT Heartbeat...");
		m_properties = properties;
		final KuraPayload kuraPayload = new KuraPayload();
		kuraPayload.addMetric("data", "live");
		getCloudApplicationClient().publish(
				(String) m_properties.get(HEARTBEAT_TOPIC), kuraPayload,
				DFLT_PUB_QOS, DFLT_RETAIN);
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(ComponentContext context) {
		LOGGER.debug("Deactivating MQTT Heartbeat Component...");
		super.deactivate(context);
		LOGGER.debug("Deactivating MQTT Heartbeat Component... Done.");
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
	 * Used to be called when configurations will get updated
	 */
	public void updated(Map<String, Object> properties) {
		LOGGER.info("Updated MQTT Heartbeat Component...");

		m_properties = properties;
		properties.keySet().forEach(
				s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updated MQTT Heartbeat Component... Done.");
	}

	/** {@inheritDoc} */
	@Override
	public void execute(JobExecutionContext executionContext)
			throws JobExecutionException {
		try {
			doBroadcastHeartbeat(m_properties);
		} catch (final KuraException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
	}
}