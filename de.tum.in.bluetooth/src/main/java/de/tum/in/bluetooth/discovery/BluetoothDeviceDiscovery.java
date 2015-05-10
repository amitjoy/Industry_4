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
package de.tum.in.bluetooth.discovery;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.xml.bind.JAXBException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.eclipse.kura.watchdog.CriticalComponent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.intel.bluetooth.RemoteDeviceHelper;

import de.tum.in.bluetooth.BluetoothController;
import de.tum.in.bluetooth.devices.Device;
import de.tum.in.bluetooth.devices.DeviceList;

/**
 * Component Discovering bluetooth device periodically and publishing a
 * {@link RemoteDevice} service per found device. Notice that bluetooth is an
 * active discovery protocol which means that device departures and arrivals are
 * detected periodically. So interacting with a bluetooth device can throw
 * {@link IOException} at any time. If bluetooth is not available, the component
 * just stops. Inquiries can not be run concurrently.
 * 
 * @author AMIT KUMAR MONDAL
 */
@Component(policy = ConfigurationPolicy.REQUIRE, name = "de.tum.in.bluetooth")
@Service(value = { BluetoothDeviceDiscovery.class })
public class BluetoothDeviceDiscovery extends Cloudlet implements
		BluetoothController, ConfigurableComponent, CriticalComponent {

	/**
	 * Defines Application ID for Pi's bluetooth application
	 */
	private static final String APP_ID = "bluetooth";

	/**
	 * Defines Quality of Service for the bluetooth Application
	 */
	private static final int QOS = 5;

	/**
	 * Configurable Property specifying the time between two inquiries. This
	 * time is specified in <b>second</b>, and should be carefully chosen. Too
	 * many inquiries flood the network and block correct discovery. A too big
	 * period, makes the device dynamism hard to track.
	 */
	private static final String PERIOD = "bluetooth.discovery.period";

	/**
	 * Configurable property to set list of bluetooth enabled devices to be
	 * discovered
	 */
	private static final String DEVICES = "bluetooh.discovery.devices";

	/**
	 * Configuration property enabling the support of unnamed devices. Unnamed
	 * devices do not communicate their name.
	 */
	private static final String IGNORE_UNNAMED_DEVICES = "bluetooth.ignore.unnamed.devices";

	/**
	 * This configuration property enables the online check when a device is
	 * found. It turns around the Windows 7 behavior, where the device discovery
	 * returns all paired devices even if they are not reachable anymore.
	 * However it introduces a performance cost ( a service discovery for each
	 * cached device on every discovery search). It should be used in
	 * combination with <tt>bluetooth.discovery.unpairOnDeparture</tt>.
	 */
	private static final String ONLINE_CHECK_ON_DISCOVERY = "bluetooth.discovery.onlinecheck";

	/**
	 * Configuration property enabling the unpairing of matching devices (filter
	 * given in the fleet description) when they are not reachable anymore.
	 */
	private static final String UNPAIR_LOST_DEVICES = "bluetooth.discovery.unpairOnDeparture";

	/**
	 * Configurable property specifying the discovery mode among GIAC and LIAC.
	 */
	private static final String DISCOVERY_MODE = "bluetooth.discovery.mode";

	/**
	 * Watchdog Critical Timeout Component
	 */
	private static final int TIMEOUT_COMPONENT = 10;

	/**
	 * All the supported bluetooth stacks in Service Gateway
	 */
	private static final List<String> SUPPORTED_STACKS = Arrays.asList(
			"winsock", "widcomm", "mac", "bluez"); // "bluez-dbus"

	/**
	 * Configurable Properties set using Metatype Configuration Management
	 */
	private Map<String, Object> m_properties;

	/**
	 * Placeholder for M_IGNORE_UNNAMED_DEVICES
	 */
	boolean m_ignoreUnnamedDevices;

	/**
	 * Placeholder for M_ONLINE_CHECK_ON_DISCOVERY
	 */
	private boolean m_onlineCheckOnDiscovery;

	/**
	 * Placeholder for M_UNPAIR_LOST_DEVICES
	 */
	private boolean m_unpairLostDevices;

	/**
	 * Placeholder for M_PERIOD
	 */
	private int m_period;

	/**
	 * Placeholder for SUBSCRIBE_TOPICPREFIX_PROP_NAME
	 */
	private String m_topic;

	/**
	 * Bluetooth discovery mode (inquiry).
	 */
	public enum DiscoveryMode {
		/**
		 * Global inquiry.
		 */
		GIAC,
		/**
		 * Limited inquiry.
		 */
		LIAC
	}

	/**
	 * Placeholder for M_DISCOVERY_MODE
	 */
	private DiscoveryMode m_discoveryMode;

	/**
	 * Bundle Context.
	 */
	private BundleContext m_context;

	/**
	 * Kura Cloud Service Injection
	 */
	@Reference
	private volatile CloudService m_cloudService;

	/**
	 * Map storing the currently exposed bluetooth device.
	 */
	private final Map<RemoteDevice, ServiceRegistration> m_devices = Maps
			.newHashMap();

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BluetoothDeviceDiscovery.class);

	/**
	 * Set of devices loaded from the <tt>devices.xml</tt> file. This file
	 * contains the authentication information for the device.
	 */
	private DeviceList m_fleet = null;

	/**
	 * The file storing the mac -> name association. This file is updated every
	 * time a new device is discovered. If set to <code>null</code> the list is
	 * not persisted.
	 */
	private File m_deviceNameFile;

	/**
	 * Map storing the MAC address to name association (ex.
	 * AXERWSD3452U=MY_BLUETOOTH_NAME). It avoids ignoring unnamed devices, as
	 * once we get a name, it is stored in this list. This map can be persisted
	 * if the device name file is set.
	 */
	private Properties m_names = new Properties();

	/**
	 * The fleet device filter (regex configured in the devices.xml file).
	 */
	Pattern m_filter;

	/**
	 * Device Discovery Agent to handle bluetooth enabled device detection
	 */
	private DeviceDiscoveryAgent m_agent;

	/**
	 * Creates a {@link BluetoothDeviceDiscovery}.
	 */
	public BluetoothDeviceDiscovery() {
		super(APP_ID);
	}

	/**
	 * Creates a {@link BluetoothDeviceDiscovery}.
	 *
	 * @param context
	 *            the bundle context
	 */
	public BluetoothDeviceDiscovery(BundleContext context) {
		super(APP_ID);
		m_context = checkNotNull(context,
				"Bluetooth Bundle Context must not be null");
		;
	}

	/**
	 * Kura Cloud Service Binding Callback
	 */
	public synchronized void bindCloudService(CloudService cloudService) {
		if (m_cloudService == null) {
			super.setCloudService(m_cloudService = cloudService);
		}
	}

	/**
	 * Kura Cloud Service Callback while deregistering
	 */
	public synchronized void unbindCloudService(CloudService cloudService) {
		if (m_cloudService == cloudService)
			super.setCloudService(m_cloudService = null);
	}

	public void setAutopairingConfiguration(File file) throws IOException {
		if (!file.exists()) {
			m_fleet = null;
			LOGGER.warn("No devices.xml file found, ignoring auto-pairing and device filter");
		} else {
			try {
				final FileInputStream fis = new FileInputStream(file);
				m_fleet = ConfigurationUtils.unmarshal(DeviceList.class, fis);
				final String filter = m_fleet.getDeviceFilter();

				if (filter != null) {
					m_filter = Pattern.compile(filter);
				}

				LOGGER.info(m_fleet.getDevices().size()
						+ " devices loaded from devices.xml");
				if (m_filter != null) {
					LOGGER.info("Device filter set to : " + m_filter.pattern());
				} else {
					LOGGER.info("No device filter set - Accepting all devices");
				}

				fis.close();
			} catch (final JAXBException e) {
				LOGGER.error(
						"Cannot unmarshall devices from "
								+ file.getAbsolutePath(), e);
			} catch (final IOException e) {
				LOGGER.error(
						"Cannot read devices from " + file.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * Sets the device name file. If set to <code>null</code> or to
	 * <code>""</code> or to <code>"null"</code>, the persistent support is
	 * disabled. Otherwise, the file is read to initialize the device list and
	 * written each time we find a new device.
	 *
	 * @param name
	 *            the path to the file relative to the working directory.
	 */
	public void setDeviceNameFile(String name) {
		if (name == null || name.equals("null") || name.trim().length() == 0) {
			LOGGER.warn("No device name file set, disabling persistent support");
			return;
		}
		m_deviceNameFile = new File(name);

		m_names = loadDeviceNames();
	}

	private Properties loadDeviceNames() {
		final Properties properties = new Properties();

		if (m_deviceNameFile == null) {
			LOGGER.error("No device name files, ignoring persistent support");
			return properties;
		}

		if (!m_deviceNameFile.exists()) {
			LOGGER.error("The device name file does not exist, ignoring ("
					+ m_deviceNameFile.getAbsolutePath() + ")");
			return properties;
		}

		try {
			final FileInputStream fis = new FileInputStream(m_deviceNameFile);
			properties.load(fis);
			fis.close();
			LOGGER.info("Device name file loaded, " + properties.size()
					+ " devices read");
		} catch (final IOException e) {
			LOGGER.error("Cannot load the device name file ("
					+ m_deviceNameFile.getAbsolutePath() + ")", e);
		}

		return properties;
	}

	private void storeDeviceNames(Properties properties) {
		if (m_deviceNameFile == null) {
			return;
		}

		if (!m_deviceNameFile.exists()) {
			final File parent = m_deviceNameFile.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
		}

		try {
			final FileOutputStream fos = new FileOutputStream(m_deviceNameFile);
			properties.store(fos, "Mac to Name file");
			fos.close();
		} catch (final IOException e) {
			LOGGER.error(
					"Cannot store the 'names' in "
							+ m_deviceNameFile.getAbsolutePath(), e);
		}
	}

	/**
	 * Callback while this component is getting registered
	 * 
	 * @param properties
	 *            the service configuration properties
	 */
	@Activate
	protected synchronized void activate(ComponentContext context,
			Map<String, Object> properties) {
		LOGGER.info("Activating Bluetooth....");
		super.setCloudService(m_cloudService);
		super.activate(context);
		m_properties = properties;
		m_context = context.getBundleContext();
		LOGGER.info("Activating Bluetooth... Done.");
	}

	/**
	 * Callback while this component is getting deregistered
	 * 
	 * @param properties
	 *            the service configuration properties
	 */
	@Deactivate
	@Override
	protected synchronized void deactivate(ComponentContext componentContext) {
		super.deactivate(componentContext);
		stop();
	}

	/**
	 * Initializes the discovery.
	 */
	@Override
	public void start() {
		LOGGER.info("Enabling Bluetooth...");

		extractRequiredConfigurations();

		if (m_agent != null) {
			return;
		}

		if (!isBluetoothStackSupported()) {
			LOGGER.error("The Bluetooth stack " + getBluetoothStack()
					+ " is not supported (" + SUPPORTED_STACKS + ")");
			return;
		}

		if ("winsock".equals(getBluetoothStack())) {
			LOGGER.info("Winsock stack detected, forcing online check and lost device unpairing");
			m_onlineCheckOnDiscovery = true;
			m_unpairLostDevices = true;
		}

		m_agent = new DeviceDiscoveryAgent(this, m_discoveryMode,
				m_onlineCheckOnDiscovery);
		BluetoothThreadManager.scheduleJob(m_agent, m_period);
	}

	/**
	 * Extracting required configuration for the bluetooth device discovery
	 */
	private void extractRequiredConfigurations() {

		LOGGER.info("Extracting Required Configurations...");

		m_period = (int) m_properties.get(PERIOD);
		m_ignoreUnnamedDevices = (boolean) m_properties
				.get(IGNORE_UNNAMED_DEVICES);
		m_onlineCheckOnDiscovery = (boolean) m_properties
				.get(ONLINE_CHECK_ON_DISCOVERY);
		m_unpairLostDevices = (boolean) m_properties.get(UNPAIR_LOST_DEVICES);

		if ((Integer) m_properties.get(DISCOVERY_MODE) == 0)
			m_discoveryMode = DiscoveryMode.GIAC;
		else
			m_discoveryMode = DiscoveryMode.LIAC;

		// m_names = loadDeviceNames(); //Used for testing purposes
		m_names = loadListOfDevicesToBeDiscovered((String) m_properties
				.get(DEVICES));

		if (m_period == 0) {
			m_period = 10; // Default to 10 seconds.
		}

		LOGGER.info("Configuration Extraction Complete");
	}

	/**
	 * Used to parse configuration set to discover specified bluetooth enabled
	 * devices
	 * 
	 * @param devices
	 *            The Configuration input as Property K-V Format
	 * @return the parsed input as properties
	 */
	private Properties loadListOfDevicesToBeDiscovered(String devices) {
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
	 * Stops the bluetooth discovery
	 */
	@Override
	public void stop() {
		LOGGER.info("Disabling Bluetooth...");
		if (m_agent == null) {
			return;
		}
		storeDeviceNames(m_names);
		m_agent = null;
		BluetoothThreadManager.stopScheduler();
		unregisterAll();
		LOGGER.info("Disabling Bluetooth...Done");
		LOGGER.info("Releasing all subscription...");
		getCloudApplicationClient().release();
		LOGGER.info("Releasing all subscription...done");

	}

	@Override
	public String getBluetoothStack() {
		return LocalDevice.getProperty("bluecove.stack");
	}

	@Override
	public boolean isBluetoothDeviceTurnedOn() {
		return LocalDevice.isPowerOn();
	}

	@Override
	public boolean isBluetoothStackSupported() {
		return SUPPORTED_STACKS.contains(getBluetoothStack());
	}

	/**
	 * Callback receiving the new set of reachable devices.
	 *
	 * @param discovered
	 *            the set of found RemoteDevice
	 */
	public void discovered(Set<RemoteDevice> discovered) {
		if (discovered == null) {
			// Bluetooth error, we unregister all devices
			LOGGER.warn("Bluetooth error detected, unregistering all devices");
			unregisterAll();
			return;
		}

		// Detect devices that have left
		// We must create a copy of the list to avoid concurrent modifications
		Set<RemoteDevice> presents = Collections.unmodifiableSet(m_devices
				.keySet());
		for (final RemoteDevice old : presents) {
			LOGGER.info("Did we lost contact with " + old.getBluetoothAddress()
					+ " => " + (!contains(discovered, old)));
			if (!contains(discovered, old)) {
				final ServiceCheckAgent serviceCheckAgent = new ServiceCheckAgent(
						old, SERVICECHECK_UNREGISTER_IF_NOT_HERE);
				BluetoothThreadManager.submit(serviceCheckAgent);
			}
		}

		// Detect new devices
		for (final RemoteDevice remote : discovered) {
			if (!m_devices.containsKey(remote)) {
				if (matchesDeviceFilter(remote)) {
					LOGGER.info("New device found ("
							+ remote.getBluetoothAddress() + ")");
					register(remote); // register the service as RemoteDevice
				} else {
					LOGGER.info("Device ignored because it does not match the device filter");
				}
			} else {
				LOGGER.info("Already known device "
						+ remote.getBluetoothAddress());
			}
		}

		if ("bluez".equals(getBluetoothStack())) {
			// Workaround for bluez : trying to keep all the paired devices.
			// Has bluez doesn't return the paired devices when we have an
			// inquiry, we can try to search if some of the
			// cached devices is are reachable
			LocalDevice local = null;
			try {
				local = LocalDevice.getLocalDevice();
			} catch (final BluetoothStateException e) {
				LOGGER.error("Bluetooth Adapter not started.");
			}
			final RemoteDevice[] cachedDevices = local.getDiscoveryAgent()
					.retrieveDevices(DiscoveryAgent.CACHED);
			if (cachedDevices == null || cachedDevices.length == 0) {
				return;
			}
			presents = Collections.unmodifiableSet(m_devices.keySet());
			for (final RemoteDevice cached : cachedDevices) {
				if (!contains(presents, cached)) {
					final ServiceCheckAgent serviceCheckAgent = new ServiceCheckAgent(
							cached, SERVICECHECK_REGISTER_IF_HERE);
					BluetoothThreadManager.submit(serviceCheckAgent);
				}
			}
		}
	}

	/**
	 * Checks whether the given list contains the given device. The check is
	 * based on the bluetooth address.
	 *
	 * @param list
	 *            a non-null list of remote device
	 * @param device
	 *            the device to check
	 * @return <code>true</code> if the device is in the list,
	 *         <code>false</code> otherwise.
	 */

	public static boolean contains(Set<RemoteDevice> list, RemoteDevice device) {
		for (final RemoteDevice d : list) {
			if (d.getBluetoothAddress().equals(device.getBluetoothAddress())) {
				return true;
			}
		}
		return false;
	}

	public boolean matchesDeviceFilter(RemoteDevice device) {
		if (m_filter == null) {
			// No filter... all devices accepted
			return true;
		}

		final String address = device.getBluetoothAddress();
		final String name = getDeviceName(device);

		return (m_filter.matcher(address).matches() || (name != null && m_filter
				.matcher(name).matches()));
	}

	private String getDeviceName(RemoteDevice device) {
		String name = m_names.getProperty(device.getBluetoothAddress());
		if (name == null) {
			try {
				name = device.getFriendlyName(false);
				if (name != null && name.length() != 0) {
					LOGGER.info("New device name discovered : "
							+ device.getBluetoothAddress() + " => " + name);
					m_names.setProperty(device.getBluetoothAddress(), name);
				}
			} catch (final IOException e) {
				LOGGER.info("Not able to get the device friendly name of "
						+ device.getBluetoothAddress(), e);
			}
		} else {
			LOGGER.info("Found the device name in memory : "
					+ device.getBluetoothAddress() + " => " + name);
		}
		return name;
	}

	private synchronized void unregisterAll() {
		for (final Map.Entry<RemoteDevice, ServiceRegistration> entry : m_devices
				.entrySet()) {
			entry.getValue().unregister();
			unpair(entry.getKey());
		}
		m_devices.clear();
	}

	private synchronized void unregister(RemoteDevice device) {
		final ServiceRegistration reg = m_devices.remove(device);
		if (reg != null) {
			reg.unregister();
		}
		unpair(device);

	}

	/**
	 * Used to register a service per device discovered
	 * 
	 * @param device
	 *            The found device
	 */
	private synchronized void register(RemoteDevice device) {
		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("device.id", device.getBluetoothAddress());
		final String name = getDeviceName(device);

		if (name != null) {
			// Switch device to our own implementation
			device = new RemoteNamedDevice(device, name);
			props.put("device.name", name);
		} else if (m_ignoreUnnamedDevices) {
			LOGGER.warn("Ignoring device " + device.getBluetoothAddress()
					+ " - discovery set to ignore " + "unnamed devices");
			return;
		}

		LOGGER.info("Registering new service for "
				+ device.getBluetoothAddress() + " with properties " + props);

		// check autopairing
		if (!device.isAuthenticated()) {
			if (!pair(device)) {
				LOGGER.warn("Aborting registering for "
						+ device.getBluetoothAddress());
				return;
			}
		}

		final ServiceRegistration<?> reg = m_context.registerService(
				RemoteDevice.class.getName(), device, props);
		m_devices.put(device, reg);

	}

	void unpair(final RemoteDevice device) {
		if (matchesDeviceFilter(device) && m_unpairLostDevices) {
			try {
				RemoteDeviceHelper.removeAuthentication(device);
			} catch (final IOException e) {
				LOGGER.error(
						"Can't unpair device " + device.getBluetoothAddress(),
						e);
			}
		}
	}

	/**
	 * Used to pair {@link RemoteDevice}
	 * 
	 * @param device
	 *            The currently discovered Remote Device
	 * @return if paired then true else false
	 */
	boolean pair(final RemoteDevice device) {
		if (m_fleet == null || m_fleet.getDevices() == null) {
			LOGGER.info("Ignoring autopairing - no fleet configured");
			return true;
		}

		final String address = device.getBluetoothAddress();
		final String name = getDeviceName(device);

		if (name == null && m_ignoreUnnamedDevices) {
			LOGGER.warn("Pairing not attempted, ignoring unnamed devices");
			return false;
		}

		final List<Device> devices = m_fleet.getDevices();
		for (final Device model : devices) {
			final String regex = model.getId();
			final String pin = model.getPin();
			if (Pattern.matches(regex, address)
					|| (name != null && Pattern.matches(regex, name))) {
				LOGGER.info("Paring pattern match for " + address + " / "
						+ name + " with " + regex);
				try {
					RemoteDeviceHelper.authenticate(device, pin);
					LOGGER.info("Device " + address + " paired");
					return true;
				} catch (final IOException e) {
					LOGGER.error(
							"Cannot authenticate device despite it match the regex "
									+ regex, e);
				}
			}
		}
		return false;
	}

	private static final int SERVICECHECK_UNREGISTER_IF_NOT_HERE = 0;

	private static final int SERVICECHECK_REGISTER_IF_HERE = 1;

	class ServiceCheckAgent implements Runnable, DiscoveryListener {

		private final RemoteDevice m_device;

		private final int m_action;

		private boolean m_searchInProgress = false;

		private final Logger m_logger = LoggerFactory
				.getLogger(ServiceCheckAgent.class);

		public ServiceCheckAgent(RemoteDevice remoteDevice, int action) {
			if (action != SERVICECHECK_REGISTER_IF_HERE
					&& action != SERVICECHECK_UNREGISTER_IF_NOT_HERE) {
				throw new IllegalArgumentException();
			}
			m_device = remoteDevice;
			m_action = action;
		}

		private LocalDevice initialize() {
			LocalDevice local = null;
			try {
				local = LocalDevice.getLocalDevice();
			} catch (final BluetoothStateException e) {
				m_logger.error("Bluetooth Adapter not started.");
			}
			return local;
		}

		@Override
		public void run() {
			try {
				final LocalDevice local = initialize();
				if (!LocalDevice.isPowerOn() || local == null) {
					m_logger.error("Bluetooth adapter not ready");
					unregister(m_device);
					return;
				}
				doSearch(local);
			} catch (final Throwable e) {
				m_logger.error("Unexpected exception during service inquiry", e);
				unregister(m_device);
			}
		}

		void doSearch(LocalDevice local) {
			synchronized (this) {
				m_searchInProgress = true;
				try {

					if (Env.isTestEnvironmentEnabled()) {
						m_logger.warn("=== TEST ENVIRONMENT ENABLED ===");
					} else {
						final javax.bluetooth.UUID[] searchUuidSet = { UUIDs.PUBLIC_BROWSE_GROUP };
						local.getDiscoveryAgent().searchServices(null,
								searchUuidSet, m_device, this);
					}
					wait();
				} catch (final InterruptedException e) {
					if (m_searchInProgress) {
						// we're stopping, aborting discovery.
						m_searchInProgress = false;
						m_logger.warn("Interrupting bluetooth service discovery - interruption");
					} else {
						// Search done !
					}
				} catch (final BluetoothStateException e) {
					// well ... bad choice. Bluetooth driver not ready
					// Just abort.
					m_logger.error("Cannot search for bluetooth services", e);
					unregister(m_device);
					return;
				}
				// Do nothing
			}
		}

		/*
		 * 
		 * ********* DiscoveryListener **********
		 */
		@Override
		public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
			// Not used here.
		}

		@Override
		public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
			synchronized (this) {
				if (!m_searchInProgress) {
					// We were stopped.
					notifyAll();
					return;
				}
			}
			// Do nothing
		}

		@Override
		public void serviceSearchCompleted(int transID, int respCode) {
			if (respCode != SERVICE_SEARCH_COMPLETED) {
				if (m_action == SERVICECHECK_UNREGISTER_IF_NOT_HERE) {
					m_logger.info("Device " + m_device.getBluetoothAddress()
							+ " have disappeared : Unregister it.");
					unregister(m_device);
				} else if (m_action == SERVICECHECK_REGISTER_IF_HERE) {
					m_logger.info("Device " + m_device.getBluetoothAddress()
							+ " is not here");
				}
			} else {
				if (m_action == SERVICECHECK_REGISTER_IF_HERE) {
					m_logger.info("Device " + m_device.getBluetoothAddress()
							+ " is here : Register it.");
					register(m_device);
				} else if (m_action == SERVICECHECK_UNREGISTER_IF_NOT_HERE) {
					m_logger.info("Device " + m_device.getBluetoothAddress()
							+ " is still here.");
				}
			}

			synchronized (this) {
				m_searchInProgress = false;
				notifyAll();
			}
		}

		@Override
		public void inquiryCompleted(int discType) {
			// Not used here.
		}
	}

	@Override
	public void onConnectionEstablished() {
		LOGGER.info("Connected to Message Broker");
	}

	@Override
	public void onConnectionLost() {
		LOGGER.info("Disconnected from Message Broker");
	}

	@Override
	public String getCriticalComponentName() {
		return APP_ID;
	}

	@Override
	public int getCriticalComponentTimeout() {
		return TIMEOUT_COMPONENT;
	}

	@Override
	protected void doExec(CloudletTopic reqTopic,
			KuraRequestPayload reqPayload, KuraResponsePayload respPayload)
			throws KuraException {
		if ("start".equals(reqTopic.getResources()[0])) {
			start();
		}
		if ("stop".equals(reqTopic.getResources()[0])) {
			start();
		}
	}
}
