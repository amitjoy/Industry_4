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

import de.tum.in.activity.log.ActivityLogService;

/**
 * Used to consume all the service record provided by all the paired Bluetooth
 * Enabled Milling Machines
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = false, name = "de.tum.in.bluetooth.milling.machine")
@Service(value = { BluetoothMillingMachine.class })
public class BluetoothMillingMachine extends Cloudlet implements ConfigurableComponent {

	/**
	 * Defines Application Configuration Metatype Id
	 */
	private static final String APP_CONF_ID = "de.tum.in.bluetooth.milling.machine";

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "MILLING-V1";

	/**
	 * Configurable Property for getting list of paired bluetooth enabled
	 * milling machines
	 */
	private static final String BLUETOOH_ENABLED_MILLING_MACHINES = "bluetooth.devices.address";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothMillingMachine.class);

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
	 * Activity Log Service Dependency
	 */
	@Reference(bind = "bindActivityLogService", unbind = "unbindActivityLogService")
	private volatile ActivityLogService m_activityLogService;

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
	 * Connection Service Dependency
	 */
	@Reference(bind = "bindConnectorService", unbind = "unbindConnectorService")
	private volatile ConnectorService m_connectorService;

	/**
	 * Used to control thread while maintaining connections between devices and
	 * RPi
	 */
	private final ExecutorService m_deletegate = Executors.newFixedThreadPool(5);

	/**
	 * Used to control threads while for asynchronous operation of getting data
	 * from paired bluetooth milling machines
	 */
	private final ExecutorService m_deletegateForAsyncFunction = Executors.newFixedThreadPool(5);

	/**
	 * Place holder for all the {@link ServiceRecord} needed for this component
	 */
	private Properties m_devices;

	/**
	 * OSGi Event Admin Service Dependency
	 */
	@Reference(bind = "bindEventAdmin", unbind = "unbindEventAdmin")
	private volatile EventAdmin m_eventAdmin;

	/**
	 * The final result computed after the async operation
	 */
	private ListenableFuture<String> m_finalResult;

	/**
	 * Represents the thread pool for initiating data retrieval from bluetooth
	 * devices
	 */
	private final ListeningExecutorService m_pool = MoreExecutors.listeningDecorator(this.m_deletegate);

	/**
	 * Represents the thread pool to operate async operation
	 */
	private final ListeningExecutorService m_poolForAsyncFunction = MoreExecutors
			.listeningDecorator(this.m_deletegateForAsyncFunction);

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * The intermediary result retrieved before the async operation
	 */
	private ListenableFuture<String> m_resultFromWorker;

	/**
	 * Bluetooth Service Record Dependency for paired bluetooth devices
	 */
	@Reference(bind = "bindServiceRecord", unbind = "unbindServiceRecord", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private volatile ServiceRecord m_serviceRecord;;

	/**
	 * Holds List of Service Record for all the paired devices
	 */
	private final List<ServiceRecord> m_serviceRecords = Lists.newCopyOnWriteArrayList();

	/**
	 * Place holder for the milling machine speed
	 */
	private float m_speed;

	/**
	 * Eclipse Kura System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/* Constructor */
	public BluetoothMillingMachine() {
		super(APP_ID);
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating Bluetooth Milling Machine Component...");

		this.m_properties = properties;

		super.setCloudService(this.m_cloudService);
		super.activate(componentContext);

		this.doLoadMachines();
		LOGGER.info("Activating Bluetooth Milling Machine Component... Done.");
	}

	/**
	 * Callback to be used while {@link ActivityLogService} is registering
	 */
	public synchronized void bindActivityLogService(final ActivityLogService activityLogService) {
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
	 * Callback to be used while {@link ConnectorService} is registering
	 */
	public synchronized void bindConnectorService(final ConnectorService connectorService) {
		if (this.m_connectorService == null) {
			this.m_connectorService = connectorService;
		}
	}

	/**
	 * Callback to be used while {@link EventAdmin} is registering
	 */
	public synchronized void bindEventAdmin(final EventAdmin eventAdmin) {
		if (this.m_eventAdmin == null) {
			this.m_eventAdmin = eventAdmin;
		}
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is registering
	 */
	public synchronized void bindServiceRecord(final ServiceRecord serviceRecord) {
		if (!this.m_serviceRecords.contains(serviceRecord)) {
			this.m_serviceRecords.add(serviceRecord);
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is registering
	 */
	public synchronized void bindSystemService(final SystemService systemService) {
		if (this.m_systemService == null) {
			this.m_systemService = systemService;
		}
	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Override
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating Bluetooth Milling Machine Component...");
		LOGGER.info("Releasing CloudApplicationClient for {}...", APP_ID);

		super.deactivate(context);
		this.m_pool.shutdown();
		this.m_poolForAsyncFunction.shutdown();
		this.m_serviceRecords.clear();

		LOGGER.debug("Deactivating Bluetooth Milling Machine Component... Done.");
	}

	/** {@inheritDoc} */
	@Override
	protected void doExec(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Bluetooth Milling Machine Communication Termination Started...");

		// Terminate bluetooth communication
		if ("terminate".equals(reqTopic.getResources()[0])) {
			if (!this.m_pool.isShutdown()) {
				this.m_pool.shutdown();
				this.m_poolForAsyncFunction.shutdown();
			}
		}
		this.m_activityLogService.saveLog("Bluetooth Milling Machine Communication Terminated");

		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

		LOGGER.info("Bluetooth Milling Machine Communication Termination Done");
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {
		LOGGER.info("Bluetooth Milling Machine Component GET handler");

		// Retrieve the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Milling Machine Configuration Retrieval Started...");

			final ComponentConfiguration configuration = this.m_configurationService
					.getComponentConfiguration(APP_CONF_ID);

			final IterableMap map = (IterableMap) configuration.getConfigurationProperties();
			final MapIterator it = map.mapIterator();

			while (it.hasNext()) {
				final Object key = it.next();
				final Object value = it.getValue();

				respPayload.addMetric((String) key, value);
			}
			this.m_activityLogService.saveLog("Bluetooth Milling Machine Configuration Retrieved");

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);

			LOGGER.info("Bluetooth Milling Machine Configuration Retrieval Finished");
		}

		// Retrieve the list of paired bluetooth devices
		if ("devices".equals(reqTopic.getResources()[0])) {
			LOGGER.info("Bluetooth Milling Machine Paired Device List Retrieval Started...");
			// if the communication is not in progress and the client asks for
			// list
			// of paired bluetooth devices, it must return nothing
			if (!this.m_pool.isShutdown()) {
				final String devicesAsString = this.getDevicelist();
				respPayload.setBody(devicesAsString.getBytes());
				respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
			}
			this.m_activityLogService.saveLog("List of Bluetooth Milling Machines Retrieved");
			LOGGER.info("Bluetooth Milling Machine Paired Device List Retrieval Finished");
		}
	}

	/**
	 * Loads Milling Machines
	 */
	private void doLoadMachines() {
		this.m_devices = this.loadMillingMachines((String) this.m_properties.get(BLUETOOH_ENABLED_MILLING_MACHINES));

		// If the device is mentioned in the configuration of this
		// component, then we have to publish the realtime data
		this.m_serviceRecords.stream()
				.filter(serviceRecord -> this.m_devices.contains(serviceRecord.getHostDevice().getBluetoothAddress()))
				.forEach(serviceRecord -> this.doPublish(serviceRecord));

	}

	/**
	 * Used to publish realtime data retrieved from all the milling machines and
	 * cache it
	 */
	private void doPublish(final ServiceRecord serviceRecord) {

		final String remoteDeviceAddress = serviceRecord.getHostDevice().getBluetoothAddress();

		final BluetoothConnector bluetoothConnector = new BluetoothConnector.Builder()
				.setConnectorService(this.m_connectorService).setServiceRecord(serviceRecord).build();

		bluetoothConnector.connect();

		// first retrieve the bluetooth realtime data from the data retriever
		// thread
		this.m_resultFromWorker = this.m_pool.submit(new DataRetrieverWorker(bluetoothConnector));

		// next do the async operation to save the result to cache retrieved by
		// the
		// data retriever thread
		this.m_finalResult = Futures.transform(this.m_resultFromWorker,
				new DataCacheAsyncOperation(this.m_poolForAsyncFunction, this.m_eventAdmin, remoteDeviceAddress));

		final String topic = (String) this.m_properties.get(PUBLISH_TOPIC_PROP_NAME);

		// finally publish the final transformed result to our listenable thread
		// callback
		Futures.addCallback(this.m_finalResult, new FuturePublishDataCallback(this.getCloudApplicationClient(),
				topic + "/" + remoteDeviceAddress, DFLT_PUB_QOS, DFLT_RETAIN, DFLT_PRIORITY));

	}

	/** {@inheritDoc} */
	@Override
	protected void doPut(final CloudletTopic reqTopic, final KuraRequestPayload reqPayload,
			final KuraResponsePayload respPayload) throws KuraException {

		LOGGER.info("Bluetooth Milling Machine Configuration Updating...");

		// Update the configurations
		if ("configurations".equals(reqTopic.getResources()[0])) {
			this.m_configurationService.updateConfiguration(APP_CONF_ID, reqPayload.metrics());

			respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
		}
		this.m_activityLogService.saveLog("Bluetooth Milling Machine Configuration Updated");

		LOGGER.info("Bluetooth Milling Machine Configuration Updated");
	}

	/**
	 * Returns the list of devices paired with this component
	 */
	private String getDevicelist() {

		final StringWriter writer = new StringWriter();

		try {
			this.m_devices.store(writer, "");
		} catch (final IOException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		return writer.getBuffer().toString();
	}

	/**
	 * Used to parse configuration set to discover specified bluetooth enabled
	 * devices
	 *
	 * @param devices
	 *            The Configuration input as Property K-V Format
	 * @return the parsed input as properties
	 */
	private Properties loadMillingMachines(final String devices) {
		final String SEPARATOR = ";";
		final String NEW_LINE = "\n";

		final Splitter splitter = Splitter.on(SEPARATOR).omitEmptyStrings().trimResults();
		final Joiner stringDevicesJoiner = Joiner.on(NEW_LINE).skipNulls();

		final Properties properties = new Properties();

		final String deviceAsPropertiesFormat = stringDevicesJoiner.join(splitter.splitToList(devices));

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
	 * Callback to be used while {@link ActivityLogService} is deregistering
	 */
	public synchronized void unbindActivityLogService(final ActivityLogService activityLogService) {
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
	 * Callback to be used while {@link ConnectorService} is deregistering
	 */
	public synchronized void unbindConnectorService(final ConnectorService connectorService) {
		if (this.m_connectorService != null) {
			this.m_connectorService = null;
		}
	}

	/**
	 * Callback to be used while {@link EventAdmin} is deregistering
	 */
	public synchronized void unbindEventAdmin(final EventAdmin eventAdmin) {
		if (this.m_cloudService == eventAdmin) {
			this.m_eventAdmin = null;
		}
	}

	/**
	 * Callback to be used while {@link ServiceRecord} is deregistering
	 */
	public synchronized void unbindServiceRecord(final ServiceRecord serviceRecord) {
		if ((this.m_serviceRecords.size() > 0) && this.m_serviceRecords.contains(serviceRecord)) {
			this.m_serviceRecords.remove(serviceRecord);
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is deregistering
	 */
	public synchronized void unbindSystemService(final SystemService systemService) {
		if (this.m_systemService == systemService) {
			this.m_systemService = null;
		}
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updated Bluetooth Milling Machine Component...");

		this.m_properties = properties;
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));
		this.doLoadMachines();

		LOGGER.info("Updated Bluetooth Milling Machine Component... Done.");
	}
}