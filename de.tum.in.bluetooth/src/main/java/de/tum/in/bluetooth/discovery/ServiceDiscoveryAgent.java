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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Discovery Agent searching services for one specific device. If a matching
 * service is found, we publishes an ??
 * 
 * @author AMIT KUMAR MONDAL
 */
public class ServiceDiscoveryAgent implements DiscoveryListener, Runnable {

	// TO-DO Validation of SDP
	// private static UUID[] searchUuidSet = { UUIDs.PUBLIC_BROWSE_GROUP };
	private static UUID[] searchUuidSet = { UUIDs.RFCOMM };

	private static int[] attrIDs = new int[] { ServiceConstants.SERVICE_NAME };

	private final BluetoothServiceDiscovery m_parent;

	private final RemoteDevice m_device;

	private String m_name;

	private boolean m_searchInProgress = false;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ServiceDiscoveryAgent.class);

	private final List<ServiceRecord> m_discoveredServices = Lists
			.newArrayList();

	public ServiceDiscoveryAgent(
			BluetoothServiceDiscovery bluetoothServiceDiscovery,
			RemoteDevice device) {
		m_parent = bluetoothServiceDiscovery;
		m_device = device;
		try {
			m_name = m_device.getFriendlyName(false);
		} catch (final IOException e) {
			m_name = m_device.getBluetoothAddress();
		}
	}

	private LocalDevice initialize() {
		LocalDevice local = null;
		try {
			local = LocalDevice.getLocalDevice();
			LOGGER.info("Address: " + local.getBluetoothAddress());
			LOGGER.info("Name: " + local.getFriendlyName());
		} catch (final BluetoothStateException e) {
			LOGGER.error("Bluetooth Adapter not started.");
		}

		return local;

	}

	@Override
	public void run() {
		try {
			LOGGER.info("Search services on " + m_device.getBluetoothAddress()
					+ " " + m_name);

			final LocalDevice local = initialize();
			if (!LocalDevice.isPowerOn() || local == null) {
				LOGGER.error("Bluetooth adapter not ready, aborting service discovery");
				m_parent.discovered(m_device, null);
				return;
			}

			doSearch(local);
		} catch (final Throwable e) {
			LOGGER.error("Unexpected exception during service inquiry",
					Throwables.getStackTraceAsString(e));
		}
	}

	void doSearch(LocalDevice local) {
		synchronized (this) {
			m_searchInProgress = true;
			try {

				if (Env.isTestEnvironmentEnabled()) {
					LOGGER.warn("=== TEST ENVIRONMENT ENABLED ===");
				} else {
					final int trans = local.getDiscoveryAgent().searchServices(
							attrIDs, searchUuidSet, m_device, this);
					LOGGER.info("Service Search {} started", trans);
				}

				wait();
			} catch (final InterruptedException e) {
				if (m_searchInProgress) {
					// we're stopping, aborting discovery.
					m_searchInProgress = false;
					LOGGER.warn("Interrupting bluetooth service discovery - interruption");
				} else {
					// Search done !
					LOGGER.info("Bluetooth discovery for " + m_name
							+ " completed !");
				}
			} catch (final BluetoothStateException e) {
				// well ... bad choice. Bluetooth driver not ready
				// Just abort.
				LOGGER.error("Cannot search for bluetooth services",
						Throwables.getStackTraceAsString(e));
				m_parent.discovered(m_device, null);
				return;
			}
			LOGGER.info("Bluetooth discovery for " + m_name
					+ " is now completed - injecting "
					+ m_discoveredServices.size() + " discovered services ");
			m_parent.discovered(m_device, m_discoveredServices);
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

		LOGGER.info("Matching service found - " + servRecord.length);
		m_discoveredServices.addAll(Arrays.asList(servRecord));
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		synchronized (this) {
			LOGGER.info("Service search completed for device "
					+ m_device.getBluetoothAddress());
			m_searchInProgress = false;
			notifyAll();
		}
	}

	@Override
	public void inquiryCompleted(int discType) {
		// Not used here.
	}

}