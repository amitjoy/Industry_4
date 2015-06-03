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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import de.tum.in.bluetooth.discovery.BluetoothDeviceDiscovery.DiscoveryMode;

/**
 * Agent used to search for bluetooth devices
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class DeviceDiscoveryAgent implements Runnable {

	public class DeviceDiscoveryListener implements DiscoveryListener {

		private final Set<RemoteDevice> m_discoveredDevices = new HashSet<RemoteDevice>();

		private final DiscoveryAgent m_agent;

		/**
		 * Map maintaining the current service discovery on discovered device.
		 * The Key is the transaction id and the value is the tested device The
		 * entry is removed when the service discovery ends.
		 */
		private final Map<Integer, RemoteDevice> m_serviceDiscoveryInProgress = new HashMap<Integer, RemoteDevice>();

		private boolean m_inquiryCompleted;

		public DeviceDiscoveryListener(final DiscoveryAgent agent) {
			m_agent = agent;
		}

		@Override
		public void deviceDiscovered(final RemoteDevice remote, final DeviceClass clazz) {
			synchronized (this) {
				try {
					LOGGER.info("Device discovered : " + remote.getBluetoothAddress() + " "
							+ remote.getFriendlyName(false));
					if (m_onlineCheckOnDiscovery) {
						// On windows, even lost device may be re-discovered
						// once they are paired.
						// We need a way to check their presence => This is a
						// bug in the Windows stack:
						// http://code.google.com/p/bluecove/issues/detail?id=51
						// Paired devices are kept forever.
						LOGGER.info("Start service discovery on : " + remote.getBluetoothAddress()
								+ " to ensure availability");
						final int transId = m_agent.searchServices(null, new UUID[] { new UUID(0x0001) }, remote, this);
						m_serviceDiscoveryInProgress.put(transId, remote);
					} else {
						// We add the device.
						LOGGER.info("Device discovery completed successfully, injecting device (no online check)");
						m_discoveredDevices.add(remote);
					}
				} catch (final Throwable e) {
					LOGGER.error("Something really bad happened during the device discovery", e);
				}
			}
		}

		public Set<RemoteDevice> getDiscoveredDevices() {
			return m_discoveredDevices;
		}

		@Override
		public void inquiryCompleted(final int result) {
			LOGGER.info("Inquiry completed : " + result);
			if ((result == INQUIRY_ERROR) || (result == INQUIRY_TERMINATED)) {
				LOGGER.info("The inquiry was not successfully completed");
				m_discoveredDevices.clear();
			}
			m_inquiryCompleted = true;
			// In the online check is disable the map will never be populated,
			// so it's empty.
			if (m_serviceDiscoveryInProgress.isEmpty()) {
				synchronized (m_lock) {
					LOGGER.info("Device inquiry and online check done, releasing lock");
					m_lock.notifyAll();
				}
			} else {
				LOGGER.info("Waiting for " + m_serviceDiscoveryInProgress.size() + " service discovery to complete");
			}
		}

		@Override
		public void servicesDiscovered(final int arg0, final ServiceRecord[] arg1) {
			// Not uses here.
			// We ignore the found services.
		}

		/**
		 * This callback will be called only if the online check is enabled. The
		 * purpose of this method is to just check that a device is still
		 * around.
		 *
		 * @param transID
		 *            the transaction id
		 * @param respCode
		 *            the response code
		 */
		@Override
		public void serviceSearchCompleted(final int transID, final int respCode) {
			RemoteDevice remote = null; // Stack confinement.
			synchronized (this) {
				remote = m_serviceDiscoveryInProgress.remove(transID);
				if (remote == null) {
					LOGGER.warn("No remote device associated with the transaction id : " + transID);
					return;
				}
			}

			LOGGER.info("Service search completed for " + remote.getBluetoothAddress() + " with result : " + respCode);
			if ((respCode == DiscoveryListener.SERVICE_SEARCH_COMPLETED)
					|| (respCode == DiscoveryListener.SERVICE_SEARCH_NO_RECORDS)) {
				LOGGER.info("Service discovery completed successfully, injecting device");
				m_discoveredDevices.add(remote);
			} else if (respCode == DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE) {
				LOGGER.warn("Device " + remote + " not reachable");
			} else {
				LOGGER.warn("Device " + remote + " has not terminated successfully: " + respCode);
			}

			if (m_inquiryCompleted && m_serviceDiscoveryInProgress.isEmpty()) {
				synchronized (m_lock) {
					LOGGER.info("Device inquiry and online check done, releasing lock");
					m_lock.notifyAll();
				}
			} else {
				LOGGER.info("Waiting for " + m_serviceDiscoveryInProgress.size() + " service discovery to complete "
						+ "(device inquiry completed: " + m_inquiryCompleted + ")");
			}
		}
	}

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDiscoveryAgent.class);

	private final Object m_lock = new Object();

	private final DiscoveryMode m_mode;

	private final boolean m_onlineCheckOnDiscovery;

	private final BluetoothDeviceDiscovery m_parent;

	private DeviceDiscoveryListener m_listener;

	DeviceDiscoveryAgent(final BluetoothDeviceDiscovery parent, final DiscoveryMode mode,
			final boolean onlineCheckOnDiscovery) {
		m_mode = mode;
		m_parent = parent;
		m_onlineCheckOnDiscovery = onlineCheckOnDiscovery;
	}

	void doInquiry(final LocalDevice local) {
		try {
			LOGGER.info("Starting device inquiry...");

			if (!Env.isTestEnvironmentEnabled()) {
				final DiscoveryAgent agent = local.getDiscoveryAgent();
				m_listener = new DeviceDiscoveryListener(agent);
				agent.startInquiry(getDiscoveryMode(), m_listener);
			} else {
				LOGGER.warn("=== TEST ENVIRONMENT ENABLED ===");
				m_listener = new DeviceDiscoveryListener(null);
			}

			// Wait until the inquiry is done.
			try {
				synchronized (m_lock) {
					// TODO Define a timeout.
					m_lock.wait();
				}
			} catch (final InterruptedException e) {
				// Ignore but warning
				LOGGER.warn(Throwables.getStackTraceAsString(e));
			}
			final Set<RemoteDevice> discoveredDevices = m_listener.getDiscoveredDevices();
			LOGGER.info("Injecting found devices " + discoveredDevices + " to the parent");
			m_parent.discovered(discoveredDevices);
		} catch (final BluetoothStateException e1) {
			LOGGER.error("Device discovery aborted", e1);
			m_parent.discovered(null);
		} finally {
			m_listener = null;
		}

	}

	/**
	 * For testing purpose <b>only</b>.
	 *
	 * @return
	 */
	DeviceDiscoveryListener getDeviceDiscoveryListener() {
		return m_listener;
	}

	/**
	 * Computes the discovery Id according to the configured discovery mode.
	 *
	 * @return the discovery id.
	 */
	private int getDiscoveryMode() {
		if (m_mode == null) {
			return DiscoveryAgent.GIAC;
		}
		switch (m_mode) {
		case GIAC:
			return DiscoveryAgent.GIAC;
		case LIAC:
			return DiscoveryAgent.LIAC;
		}
		return 0; // Cannot happen.
	}

	private LocalDevice initialize() {
		LocalDevice local;
		try {
			local = LocalDevice.getLocalDevice();
			LOGGER.info("Initialize : " + "Address: " + local.getBluetoothAddress() + " ; " + "Name: "
					+ local.getFriendlyName());
		} catch (final BluetoothStateException e) {
			LOGGER.error("Bluetooth Adapter not started.");
			return null;
		}
		return local;

	}

	@Override
	public void run() {
		try {
			LOGGER.info("Running Device Discovery Agent");
			final LocalDevice local = initialize();
			if (!LocalDevice.isPowerOn() || (local == null)) {
				LOGGER.info("Device discovery aborted - cannot get the local device");
				m_parent.discovered(null);
				return;
			}

			doInquiry(local);
		} catch (final Throwable e) {
			LOGGER.error("Unexpected exception during device inquiry", e);
		}
	}

}
