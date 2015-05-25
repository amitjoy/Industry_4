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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.xml.bind.JAXBException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import de.tum.in.bluetooth.devices.Device;
import de.tum.in.bluetooth.devices.DeviceList;

/**
 * Component publishing {@link ServiceRecord} for all bluetooth services. This
 * component consumes {@link RemoteDevice} services i.e all discovered bluetooth
 * devices and gets all the bluetooth service profiles of each and every
 * discovered device. For each bluetooth service profile, it publishes a
 * {@link ServiceRecord}.
 * 
 * @author AMIT KUMAR MONDAL
 */
@Component(immediate = true)
public class BluetoothServiceDiscovery {

	static final int[] ATTRIBUTES = null;

	private static final int SERVICE_NAME_ATTRIBUTE = 0x0100;

	/**
	 * Bundle Context.
	 */
	private BundleContext m_context;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BluetoothServiceDiscovery.class);

	/**
	 * Map storing the currently registered ServiceRecord(with their
	 * ServiceRegistration) by RemoteDevice
	 */
	private final Map<RemoteDevice, Map<ServiceRecord, ServiceRegistration>> m_servicesRecord = Maps
			.newHashMap();

	/**
	 * Set of devices loaded from the <tt>devices.xml</tt> file. This file
	 * contains the authentication information for the device.
	 */
	@Reference(bind = "bindDeviceListService", unbind = "unbindDeviceListService")
	private volatile DeviceList m_fleet;

	/**
	 * List of device under attempts.
	 */
	private final Map<RemoteDevice, Integer> m_attempts = Maps.newHashMap();

	/**
	 * Default Constructor Required for DS.
	 */
	public BluetoothServiceDiscovery() {
	}

	/**
	 * Creates a {@link BluetoothServiceDiscovery}. Mainly used for testing
	 * environment.
	 *
	 * @param context
	 *            the Bundle context
	 */
	public BluetoothServiceDiscovery(BundleContext context) {
		m_context = context;
		LOGGER.info("Bluetooth Tracker Started");
	}

	/**
	 * Device Configuration List Service Binding Callback
	 */
	public synchronized void bindDeviceListService(DeviceList deviceList) {
		if (m_fleet == null) {
			m_fleet = deviceList;
		}
	}

	/**
	 * Device Configuration List Service Service Callback while deregistering
	 */
	public synchronized void unbindDeviceListService(DeviceList deviceList) {
		if (m_fleet == deviceList)
			m_fleet = null;
	}

	/**
	 * Callback during registration of this DS Service Component
	 * 
	 * @param context
	 *            The injected reference for this DS Service Component
	 */
	@Activate
	protected synchronized void activate(ComponentContext context) {
		LOGGER.info("Activating Bluetooth Service Discovery....");
		m_context = context.getBundleContext();
		LOGGER.info("Activating Bluetooth Service Discovery....Done");
	}

	/**
	 * Used for testing
	 */
	public void setDeviceFile(File file) throws IOException {
		if (!file.exists()) {
			m_fleet = null;
			LOGGER.warn("No devices.xml file found, ignoring authentication");
		} else {
			try {
				final FileInputStream fis = new FileInputStream(file);
				m_fleet = ConfigurationUtils.unmarshal(DeviceList.class, fis);
				LOGGER.info(m_fleet.getDevices().size()
						+ " devices loaded from devices.xml");
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
	 * Stops the discovery. All published services are withdrawn.
	 */
	@Deactivate
	public synchronized void stop() {
		LOGGER.info("Dectivating Bluetooth Service Discovery....");
		unregisterAll();
		m_attempts.clear();
		LOGGER.info("Deactivating Bluetooth Service Discovery....");
	}

	private synchronized void unregisterAll() {
		for (final RemoteDevice remoteDevice : m_servicesRecord.keySet()) {
			unregister(remoteDevice);
		}
	}

	private synchronized void unregister(RemoteDevice remote) {
		LOGGER.info("Deregistering Service Records....");
		final Map<ServiceRecord, ServiceRegistration> services = m_servicesRecord
				.remove(remote);
		if (services == null) {
			return;
		}
		for (final ServiceRegistration<?> sr : services.values()) {
			sr.unregister();
		}
		LOGGER.info("Deregistering Service Records....Done");
	}

	/**
	 * Is used to register {@link ServiceRecord} as an OSGi Service for each and
	 * every {@link RemoteDevice} found as OSGi Service in the Service Registry.
	 * 
	 * @param remote
	 *            The Bluetooth Device found
	 * @param serviceRecord
	 * @param device
	 * @param url
	 */
	private synchronized void register(RemoteDevice remote,
			ServiceRecord serviceRecord, Device device, String url) {
		LOGGER.info("Registering Service Records....");

		if (!m_servicesRecord.containsKey(remote)) {
			m_servicesRecord.put(remote,
					new HashMap<ServiceRecord, ServiceRegistration>());
		}

		final Dictionary<String, Object> props = new Hashtable<String, Object>();
		props.put("device.id", remote.getBluetoothAddress());
		final int[] attributeIDs = serviceRecord.getAttributeIDs();

		if (attributeIDs != null && attributeIDs.length > 0) {
			final Map<Integer, DataElement> attrs = new HashMap<Integer, DataElement>();
			for (final int attrID : attributeIDs) {
				attrs.put(attrID, serviceRecord.getAttributeValue(attrID));
			}
			props.put("service.attributes", attrs);
		}

		props.put("service.url", url);

		if (device != null) {
			props.put("fleet.device", device);
		}

		final ServiceRegistration<?> sr = m_context.registerService(
				ServiceRecord.class.getName(), serviceRecord, props);
		m_servicesRecord.get(remote).put(serviceRecord, sr);

		LOGGER.info("Registering Service Records....Done");
	}

	/**
	 * A new {@link RemoteDevice} is available. Checks if it implements OBEX, if
	 * so publish the service
	 *
	 * @param device
	 *            the device
	 */
	public synchronized void bindRemoteDevice(RemoteDevice device) {
		LOGGER.info("Binding Remote Device...." + device);
		try {
			// We can't run searches concurrently.
			final ServiceDiscoveryAgent agent = new ServiceDiscoveryAgent(this,
					device);
			BluetoothThreadManager.submit(agent);
		} catch (final Exception e) {
			LOGGER.error(
					"Cannot discover services from "
							+ device.getBluetoothAddress(),
					Throwables.getStackTraceAsString(e));
		}
		LOGGER.info("Binding Remote Device....Done" + device);
	}

	/**
	 * A {@link RemoteDevice} disappears. If an OBEX service was published for
	 * this device, the service is unpublished.
	 *
	 * @param device
	 *            the device
	 */
	public synchronized void unbindRemoteDevice(RemoteDevice device) {
		LOGGER.info("Unbinding Remote Device...." + device);
		unregister(device);
		LOGGER.info("Unbinding Remote Device....Done" + device);
	}

	/**
	 * Callback receiving the set of discovered service from the given
	 * {@link RemoteDevice}.
	 *
	 * @param remote
	 *            the RemoteDevice
	 * @param discoveredServices
	 *            the list of ServiceRecord
	 */
	public void discovered(final RemoteDevice remote,
			List<ServiceRecord> discoveredServices) {
		if (discoveredServices == null || discoveredServices.isEmpty()) {
			unregister(remote);

			if (retry(remote)) {
				LOGGER.info("Retrying service discovery for device "
						+ remote.getBluetoothAddress() + " - "
						+ m_attempts.get(remote));
				incrementAttempt(remote);
				final ServiceDiscoveryAgent agent = new ServiceDiscoveryAgent(
						this, remote);
				BluetoothThreadManager.submit(agent);
			} else {
				// We don't retry, either retry is false or we reached the
				// number of attempts.
				m_attempts.remove(remote);
			}
			return;
		}
		LOGGER.info("Agent has discovered " + discoveredServices.size()
				+ " services from " + remote.getBluetoothAddress() + ".");

		// Service discovery successful, we reset the number of attempts.
		m_attempts.remove(remote);
		final Device device = findDeviceFromFleet(remote);

		for (final ServiceRecord record : discoveredServices) {
			String url;
			if (device == null) {
				url = record.getConnectionURL(
						ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
			} else {
				url = record.getConnectionURL(
						ServiceRecord.AUTHENTICATE_NOENCRYPT, false);
			}

			if (url == null) {
				LOGGER.warn("Can't compute the service url for device "
						+ remote.getBluetoothAddress()
						+ " - Ignoring service record");
			} else {
				final DataElement serviceName = record
						.getAttributeValue(SERVICE_NAME_ATTRIBUTE);
				if (serviceName != null) {
					LOGGER.info("Service " + serviceName.getValue() + " found "
							+ url);
				} else {
					LOGGER.info("Service found " + url);
				}
				register(remote, record, device, url);
			}
		}

	}

	private void incrementAttempt(final RemoteDevice remote) {
		LOGGER.info("Attempting to retry..Retry On "
				+ remote.getBluetoothAddress());
		Integer attempt = m_attempts.get(remote);
		if (attempt == null) {
			attempt = 1;
			m_attempts.put(remote, attempt);
		} else {
			m_attempts.put(remote, ++attempt);
		}
	}

	private boolean retry(final RemoteDevice remote) {
		LOGGER.info("Retrying for service discovery attempt..." + remote);
		final Device device = findDeviceFromFleet(remote);
		if (device == null) {
			return true;
		}

		Integer numberOfTries = m_attempts.get(remote);
		if (numberOfTries == null) {
			numberOfTries = 0;
		}

		final BigInteger mr = device.getMaxRetry();
		int max = 1;
		if (mr != null && mr.intValue() != 0) {
			max = mr.intValue();
		}

		LOGGER.info("Retrying for service discovery attempt...Done" + remote);

		return (device.isRetry() && max >= numberOfTries);
	}

	Device findDeviceFromFleet(RemoteDevice remote) {
		if (m_fleet != null) {
			final String address = remote.getBluetoothAddress();
			String sn = null;
			try {
				sn = remote.getFriendlyName(false);
			} catch (final IOException e) {
				// ignore the exception. Just warn it.
				LOGGER.warn(Throwables.getStackTraceAsString(e));
			}

			for (int i = 0; i < m_fleet.getDevices().size(); i++) {
				final Device d = m_fleet.getDevices().get(i);
				final String regex = d.getId(); // id can be regex.
				if (Pattern.matches(regex, address)
						|| (sn != null && Pattern.matches(regex, sn))) {
					return d;
				}
			}
		}
		return null;
	}

}
