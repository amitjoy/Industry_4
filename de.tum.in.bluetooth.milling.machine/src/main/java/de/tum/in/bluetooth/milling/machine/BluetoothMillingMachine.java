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
package de.tum.in.bluetooth.milling.machine;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.ServiceRecord;

import org.apache.commons.io.IOUtils;
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
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.io.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Used to consume all the service record provided by all the paired Bluetooth
 * Enabled Milling Machines
 * 
 * TO-DO Refactor and Deploy
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = false, name = "de.tum.in.bluetooth.milling.machine")
@Service(value = { BluetoothMillingMachine.class })
public class BluetoothMillingMachine extends Cloudlet implements
		ConfigurableComponent {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BluetoothMillingMachine.class);

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "Bluetooth-Milling-Machine-V1";

	/**
	 * Used to control thread while maintaining connections between devices and
	 * RPi
	 */
	private final ExecutorService m_executorService = Executors
			.newFixedThreadPool(5);

	/**
	 * Configurable Property for getting list of paired bluetooth enabled
	 * milling machines
	 */
	private static final String BLUETOOH_ENABLED_MILLING_MACHINES = "bluetooth.devices.address";

	/**
	 * Configurable Property to check the setpoint speed for milling machines
	 */
	private static final String PROGRAM_SETPOINT_NAME = "program.setPoint";

	/**
	 * Configurable Property to set the rate by which the realtime data would be
	 * pushed to the clients
	 */
	private static final String PUBLISH_RATE_PROP_NAME = "publish.rate";

	/**
	 * Configurable Property for topic to publish realtime data
	 */
	private static final String PUBLISH_TOPIC_PROP_NAME = "publish.semanticTopic";

	/**
	 * Eclipse Kura Cloud Service Dependency
	 */
	@Reference
	private volatile CloudService m_cloudService;

	/**
	 * Eclipse Kura System Service Dependency
	 */
	@Reference
	private volatile SystemService m_systemService;

	/**
	 * Eclipse Kura Configuration Service Dependency
	 */
	@Reference
	private volatile ConfigurationService m_configurationService;

	/**
	 * Bluetooth Service Record Dependency for paired bluetooth devices
	 */
	@Reference
	private volatile ServiceRecord m_serviceRecord;

	/**
	 * Connection Service Dependency
	 */
	@Reference
	private volatile ConnectorService m_connectorService;

	/**
	 * Holds List of Service Record for all the paired devices
	 */
	private List<ServiceRecord> m_serviceRecords;

	/**
	 * Place holder for the milling machine speed
	 */
	private float m_speed;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/* Constructor */
	public BluetoothMillingMachine() {
		super(APP_ID);
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is registering
	 */
	public synchronized void setServiceRecord(ServiceRecord serviceRecord) {
		if (!m_serviceRecords.contains(serviceRecord)) {
			m_serviceRecords.add(serviceRecord);
		}
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is deregistering
	 */
	public synchronized void unsetServiceRecord(ServiceRecord serviceRecord) {
		if (m_serviceRecords.size() > 0)
			m_serviceRecords.clear();
	}

	/**
	 * Callback to be used while {@link ConnectorService} is registering
	 */
	public synchronized void setConnectorService(
			ConnectorService connectorService) {
		if (m_connectorService == null)
			m_connectorService = connectorService;
	}

	/**
	 * Callback to be used while {@link ConnectorService} is deregistering
	 */
	public synchronized void unsetConnectorService(
			ConnectorService connectorService) {
		if (m_connectorService != null)
			m_connectorService = null;
	}

	/**
	 * Callback to be used while {@link CloudService} is registering
	 */
	@Override
	public synchronized void setCloudService(CloudService cloudService) {
		if (m_cloudService == null) {
			super.setCloudService(m_cloudService = cloudService);
		}
	}

	/**
	 * Callback to be used while {@link CloudService} is deregistering
	 */
	@Override
	public synchronized void unsetCloudService(CloudService cloudService) {
		if (m_cloudService == cloudService)
			super.setCloudService(m_cloudService = null);
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is registering
	 */
	public synchronized void setConfigurationService(
			ConfigurationService configurationService) {
		if (m_cloudService == null) {
			m_configurationService = configurationService;
		}
	}

	/**
	 * Callback to be used while {@link ConfigurationService} is deregistering
	 */
	public synchronized void unsetConfigurationService(
			ConfigurationService configurationService) {
		if (m_configurationService == configurationService)
			m_configurationService = null;
	}

	/**
	 * Callback to be used while {@link SystemService} is registering
	 */
	public synchronized void setSystemService(SystemService systemService) {
		if (m_systemService == null)
			m_systemService = systemService;
	}

	/**
	 * Callback to be used while {@link SystemService} is deregistering
	 */
	public synchronized void unsetSystemService(SystemService systemService) {
		if (m_systemService == systemService)
			m_systemService = null;
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(ComponentContext componentContext,
			Map<String, Object> properties) {
		LOGGER.info("Activating Bluetooth Milling Machine Component...");

		m_properties = properties;
		m_serviceRecords = Lists.newCopyOnWriteArrayList();

		super.setCloudService(m_cloudService);
		super.activate(componentContext);
		LOGGER.info("Activating Bluetooth Milling Machine Component... Done.");

		for (final ServiceRecord serviceRecord : m_serviceRecords) {
			doPublish(serviceRecord);
		}
	}

	/**
	 * Used to publish realtime data retrieved from all the milling machines
	 */
	private void doPublish(ServiceRecord serviceRecord) {

		final BluetoothConnector bluetoothConnector = new BluetoothConnector.Builder()
				.setConnectorService(m_connectorService)
				.setServiceRecord(serviceRecord).build();
		bluetoothConnector.connect();

		// TO-DO Refactor Logic while publishing realtime data
		// TO-DO Add logic to refactor topic for each paired device
		String realtimeData = null;
		final int readByte = 1;
		final Callable<String> readTask = new Callable<String>() {
			@Override
			public String call() throws Exception {
				return IOUtils.toString(bluetoothConnector.getInputStream(),
						Charsets.UTF_8.name());
			}
		};
		while (true) {
			final Future<String> future = m_executorService.submit(readTask);
			try {
				realtimeData = future.get(1000, TimeUnit.MILLISECONDS);
				final String topic = (String) m_properties
						.get(PUBLISH_TOPIC_PROP_NAME);
				final String payload = Integer.toString(readByte);
				try {
					getCloudApplicationClient().controlPublish("DEVICE", topic,
							payload.getBytes(), DFLT_PUB_QOS, DFLT_RETAIN,
							DFLT_PRIORITY);
				} catch (final KuraException e) {
					LOGGER.error(Throwables.getStackTraceAsString(e));
				}
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				LOGGER.error(Throwables.getStackTraceAsString(e));
			}
			if (readByte >= 0)
				System.out.println("Read: " + readByte);
		}
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(ComponentContext context) {
		LOGGER.debug("Deactivating Bluetooth Milling Machine Component...");
		LOGGER.info("Releasing CloudApplicationClient for {}...", APP_ID);

		super.deactivate(context);
		m_executorService.shutdown();

		LOGGER.debug("Deactivating Bluetooth Milling Machine Component... Done.");
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(Map<String, Object> properties) {
		LOGGER.info("Updated Bluetooth Milling Machine Component...");

		m_properties = properties;
		for (final String s : properties.keySet()) {
			LOGGER.info("Update - " + s + ": " + properties.get(s));
		}

		LOGGER.info("Updated Bluetooth Milling Machine Component... Done.");
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(CloudletTopic reqTopic, KuraRequestPayload reqPayload,
			KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Bluetooth Milling Machine Component GET handler"
				+ m_configurationService);
		// TO-DO Add logic to retrieve the list of devices paired with RPi
		final ComponentConfiguration configuration = m_configurationService
				.getComponentConfiguration("de.tum.in.bluetooth.milling.machine");
		final Iterator<?> entries = configuration.getConfigurationProperties()
				.entrySet().iterator();
		while (entries.hasNext()) {
			final Entry thisEntry = (Entry) entries.next();
			final Object key = thisEntry.getKey();
			final Object value = thisEntry.getValue();
			respPayload.addMetric((String) key, value);
		}
		System.out.println(Arrays.asList(reqTopic.getResources()));
		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
	}

	/** {@inheritDoc} */
	@Override
	protected void doExec(CloudletTopic reqTopic,
			KuraRequestPayload reqPayload, KuraResponsePayload respPayload)
			throws KuraException {
		LOGGER.info("Bluetooth Milling Machine Component EXEC handler");
		// TO-DO add logic to stop reading data from devices
	}
}