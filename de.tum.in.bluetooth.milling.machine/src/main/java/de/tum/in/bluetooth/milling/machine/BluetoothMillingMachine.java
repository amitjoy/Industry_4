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

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
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
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import de.tum.in.bluetooth.milling.machine.data.RealtimeData;

/**
 * Used to consume all the service record provided by all the paired Bluetooth
 * Enabled Milling Machines
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
	private static final String APP_ID = "MILLING-V1";

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.bluetooth.milling.machine";

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
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * Eclipse Kura System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/**
	 * Eclipse Kura Configuration Service Dependency
	 */
	@Reference(bind = "bindConfigurationService", unbind = "unbindConfigurationService")
	private volatile ConfigurationService m_configurationService;

	/**
	 * Bluetooth Service Record Dependency for paired bluetooth devices
	 */
	@Reference(bind = "bindServiceRecord", unbind = "unbindServiceRecord", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private volatile ServiceRecord m_serviceRecord;

	/**
	 * Connection Service Dependency
	 */
	@Reference(bind = "bindConnectorService", unbind = "unbindConnectorService")
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
	 * Place holder for all the {@link ServiceRecord} needed for this component
	 */
	private Properties m_devices;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * Holds List of results retrieved by all the paired devices
	 */
	private List<? extends RealtimeData> m_realTimeData;

	/* Constructor */
	public BluetoothMillingMachine() {
		super(APP_ID);
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is registering
	 */
	public synchronized void bindServiceRecord(ServiceRecord serviceRecord) {
		if (m_serviceRecords.size() > 0)
			if (!m_serviceRecords.contains(serviceRecord)) {
				m_serviceRecords.add(serviceRecord);
			}
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is deregistering
	 */
	public synchronized void unbindServiceRecord(ServiceRecord serviceRecord) {
		if (m_serviceRecords.size() > 0)
			m_serviceRecords.clear();
	}

	/**
	 * Callback to be used while {@link ConnectorService} is registering
	 */
	public synchronized void bindConnectorService(
			ConnectorService connectorService) {
		if (m_connectorService == null)
			m_connectorService = connectorService;
	}

	/**
	 * Callback to be used while {@link ConnectorService} is deregistering
	 */
	public synchronized void unbindConnectorService(
			ConnectorService connectorService) {
		if (m_connectorService != null)
			m_connectorService = null;
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
		LOGGER.info("Activating Bluetooth Milling Machine Component...");

		m_properties = properties;
		m_serviceRecords = Lists.newCopyOnWriteArrayList();
		m_realTimeData = Lists.newCopyOnWriteArrayList();

		super.setCloudService(m_cloudService);
		super.activate(componentContext);

		m_devices = loadMillingMachines(BLUETOOH_ENABLED_MILLING_MACHINES);

		LOGGER.info("Activating Bluetooth Milling Machine Component... Done.");

		for (final ServiceRecord serviceRecord : m_serviceRecords) {
			// If the device is mentioned in the configuration of this
			// component, then we have to publish the realtime data
			if (m_devices.contains(serviceRecord.getHostDevice()
					.getBluetoothAddress()))
				doPublishRealtimeDataAndStoreInCache(serviceRecord);
		}
	}

	/**
	 * Used to parse configuration set to discover specified bluetooth enabled
	 * devices
	 * 
	 * @param devices
	 *            The Configuration input as Property K-V Format
	 * @return the parsed input as properties
	 */
	private Properties loadMillingMachines(String devices) {
		final String SEPARATOR = ";";
		final String NEW_LINE = "\n";

		final Splitter splitter = Splitter.on(SEPARATOR).omitEmptyStrings()
				.trimResults();
		final Joiner stringDevicesJoiner = Joiner.on(NEW_LINE).skipNulls();

		final Properties properties = new Properties();

		final String deviceAsPropertiesFormat = stringDevicesJoiner
				.join(splitter.splitToList(devices));

		if (isNullOrEmpty(deviceAsPropertiesFormat.toString())) {
			LOGGER.error("No Bluetooth Enabled Device Addess Found");
			return properties;
		}

		try {
			properties.load(new StringReader(deviceAsPropertiesFormat));
		} catch (final IOException e) {
			LOGGER.error("Error while parsing list of input bluetooth devices");
		}
		return properties;
	}

	/**
	 * Used to publish realtime data retrieved from all the milling machines and
	 * cache it
	 */
	private void doPublishRealtimeDataAndStoreInCache(
			ServiceRecord serviceRecord) {
		final String remoteDeviceAddress = serviceRecord.getHostDevice()
				.getBluetoothAddress();

		final BluetoothConnector bluetoothConnector = new BluetoothConnector.Builder()
				.setConnectorService(m_connectorService)
				.setServiceRecord(serviceRecord).build();

		bluetoothConnector.connect();

		String realtimeData = null;
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
				m_realTimeData.add(wrapData(remoteDeviceAddress, realtimeData));
				// TO-DO Add camel connector to dump to mongo db
				// TO-DO Add camel connector for scheduling
				final String topic = (String) m_properties
						.get(PUBLISH_TOPIC_PROP_NAME);
				final String payload = realtimeData;
				try {
					// will publish data to
					// $EDC/app_id/client_id/milling_machine/{some_bluetooth_address}
					getCloudApplicationClient().controlPublish(
							"milling_machine", remoteDeviceAddress,
							payload.getBytes(), DFLT_PUB_QOS, DFLT_RETAIN,
							DFLT_PRIORITY);
				} catch (final KuraException e) {
					LOGGER.error(Throwables.getStackTraceAsString(e));
				}
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				LOGGER.error(Throwables.getStackTraceAsString(e));
			}
		}
	}

	/**
	 * Used to wrap data for the predefined format needed
	 * 
	 * @param bluetoothAddress
	 *            the bluetooth address of the {@link RemoteDevice}
	 * @param realtimeData
	 *            The data retrieved from the input stream
	 * @return
	 */
	private <T extends RealtimeData> T wrapData(String bluetoothAddress,
			String realtimeData) {
		// for temporary purposes, it is hardcoded to be a default predefined
		// format
		final RealtimeData data = new RealtimeData(bluetoothAddress,
				realtimeData);
		return (T) data;
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
		LOGGER.info("Bluetooth Milling Machine Component GET handler");

		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Milling Machine Configuration Retrieval Started...");

			final ComponentConfiguration configuration = m_configurationService
					.getComponentConfiguration(APP_CONF_ID);

			final IterableMap map = (IterableMap) configuration
					.getConfigurationProperties();
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

			LOGGER.info("Bluetooth Milling Machine Configuration Retrieval Finished");
		}

		// Retrieve the list of paired bluetooth devices
		if ("devices".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Milling Machine Paired Device List Retrieval Started...");
			// if the communication is not in progress and the client asks for
			// list
			// of paired bluetooth devices, it must return nothing
			if (!m_executorService.isShutdown()) {
				final String devicesAsString = getDevicelist();
				respPayload.setBody(devicesAsString.getBytes());
				respPayload
						.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
			}
			LOGGER.info("Bluetooth Milling Machine Paired Device List Retrieval Finished");
		}
	}

	/**
	 * Returns the list of devices paired with this component
	 */
	private String getDevicelist() {
		final StringWriter writer = new StringWriter();
		try {
			m_devices.store(writer, "");
		} catch (final IOException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		return writer.getBuffer().toString();
	}

	/** {@inheritDoc} */
	@Override
	protected void doExec(CloudletTopic reqTopic,
			KuraRequestPayload reqPayload, KuraResponsePayload respPayload)
			throws KuraException {
		LOGGER.info("Bluetooth Milling Machine Communication Termination Started...");

		// Terminate bluetooth communication
		if ("terminate".equals(reqTopic.getResources()[0])) {
			if (!m_executorService.isShutdown())
				m_executorService.shutdown();
		}
		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

		LOGGER.info("Bluetooth Milling Machine Communication Termination Done");
	}

	/** {@inheritDoc} */
	@Override
	protected void doPut(CloudletTopic reqTopic, KuraRequestPayload reqPayload,
			KuraResponsePayload respPayload) throws KuraException {

		LOGGER.info("Bluetooth Milling Machine Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			m_configurationService.updateConfiguration(APP_CONF_ID,
					reqPayload.metrics());

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}

		LOGGER.info("Bluetooth Milling Machine Configuration Updated");
	}
}