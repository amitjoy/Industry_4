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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.bluetooth.ServiceRecord;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
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
import org.osgi.service.event.EventAdmin;
import org.osgi.service.io.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

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
	private final ExecutorService m_deletegate = Executors
			.newFixedThreadPool(5);

	/**
	 * Used to control threads while for asynchronous operation of getting data
	 * from paired bluetooth milling machines
	 */
	private final ExecutorService m_deletegateForAsyncFunction = Executors
			.newFixedThreadPool(5);

	/**
	 * Represents the thread pool for initiating data retrieval from bluetooth
	 * devices
	 */
	private final ListeningExecutorService m_pool = MoreExecutors
			.listeningDecorator(m_deletegate);

	/**
	 * Represents the thread pool to operate async operation
	 */
	private final ListeningExecutorService m_poolForAsyncFunction = MoreExecutors
			.listeningDecorator(m_deletegateForAsyncFunction);

	/**
	 * The intermediary result retrieved before the async operation
	 */
	private ListenableFuture<String> m_resultFromWorker;

	/**
	 * The final result computed after the async operation
	 */
	private ListenableFuture<String> m_finalResult;

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
	 * OSGi Event Admin Service Dependency
	 */
	@Reference(bind = "bindEventAdmin", unbind = "unbindEventAdmin")
	private volatile EventAdmin m_eventAdmin;

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
	private final List<ServiceRecord> m_serviceRecords = Lists
			.newCopyOnWriteArrayList();;

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

	/* Constructor */
	public BluetoothMillingMachine() {
		super(APP_ID);
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is registering
	 */
	public synchronized void bindServiceRecord(ServiceRecord serviceRecord) {
		if (!m_serviceRecords.contains(serviceRecord)) {
			m_serviceRecords.add(serviceRecord);
		}
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is deregistering
	 */
	public synchronized void unbindServiceRecord(ServiceRecord serviceRecord) {
		if (m_serviceRecords.size() > 0
				&& m_serviceRecords.contains(serviceRecord))
			m_serviceRecords.remove(serviceRecord);
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
	 * Callback to be used while {@link EventAdmin} is registering
	 */
	public synchronized void bindEventAdmin(EventAdmin eventAdmin) {
		if (m_eventAdmin == null) {
			m_eventAdmin = eventAdmin;
		}
	}

	/**
	 * Callback to be used while {@link EventAdmin} is deregistering
	 */
	public synchronized void unbindEventAdmin(EventAdmin eventAdmin) {
		if (m_cloudService == eventAdmin)
			m_eventAdmin = null;
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

		super.setCloudService(m_cloudService);
		super.activate(componentContext);

		m_devices = loadMillingMachines((String) m_properties
				.get(BLUETOOH_ENABLED_MILLING_MACHINES));

		LOGGER.info("Activating Bluetooth Milling Machine Component... Done.");

		for (final ServiceRecord serviceRecord : m_serviceRecords) {
			// If the device is mentioned in the configuration of this
			// component, then we have to publish the realtime data
			if (m_devices.contains(serviceRecord.getHostDevice()
					.getBluetoothAddress()))
				doPublish(serviceRecord);
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
	private void doPublish(ServiceRecord serviceRecord) {

		final String remoteDeviceAddress = serviceRecord.getHostDevice()
				.getBluetoothAddress();

		final BluetoothConnector bluetoothConnector = new BluetoothConnector.Builder()
				.setConnectorService(m_connectorService)
				.setServiceRecord(serviceRecord).build();

		bluetoothConnector.connect();

		// first retrieve the bluetooth realtime data from the data retriever
		// thread
		m_resultFromWorker = m_pool.submit(new DataRetrieverWorker(
				bluetoothConnector));

		// next do the async operation to save the result to cache retrieved by
		// the
		// data retriever thread
		m_finalResult = Futures.transform(m_resultFromWorker,
				new DataCacheAsyncOperation(m_poolForAsyncFunction,
						m_eventAdmin, remoteDeviceAddress));

		final String topic = (String) m_properties.get(PUBLISH_TOPIC_PROP_NAME);

		// finally publish the final transformed result to our listenable thread
		// callback
		Futures.addCallback(m_finalResult, new FuturePublishDataCallback(
				getCloudApplicationClient(), topic + "/" + remoteDeviceAddress,
				DFLT_PUB_QOS, DFLT_RETAIN, DFLT_PRIORITY));

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
		m_pool.shutdown();
		m_poolForAsyncFunction.shutdown();
		m_serviceRecords.clear();

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
		// TO-DO Add activity log service to all the doX()
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
			if (!m_pool.isShutdown()) {
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
			if (!m_pool.isShutdown()) {
				m_pool.shutdown();
				m_poolForAsyncFunction.shutdown();
			}
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