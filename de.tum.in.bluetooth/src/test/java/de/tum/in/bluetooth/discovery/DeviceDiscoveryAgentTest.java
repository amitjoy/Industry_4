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

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;

import org.junit.After;
import org.junit.Test;

import de.tum.in.bluetooth.discovery.BluetoothDeviceDiscovery.DiscoveryMode;
import de.tum.in.bluetooth.discovery.DeviceDiscoveryAgent.DeviceDiscoveryListener;

/**
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class DeviceDiscoveryAgentTest {

	@After
	public void tearDown() {
		Env.disableTestEnvironment();
	}

	@Test
	public void testRun() throws InterruptedException {
		final BluetoothDeviceDiscovery parent = org.easymock.EasyMock
				.createMock(BluetoothDeviceDiscovery.class);
		final DeviceDiscoveryAgent agent = new DeviceDiscoveryAgent(parent,
				DiscoveryMode.GIAC, false);

		new Thread(new Runnable() {
			@Override
			public void run() {
				agent.run();
			}
		}).start();

		Thread.sleep(5000);
		if (agent.getDeviceDiscoveryListener() != null) {
			agent.getDeviceDiscoveryListener().inquiryCompleted(0);
		}

	}

	@Test
	public void testInquiry() throws InterruptedException {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}
		Env.enableTestEnvironment();

		final BluetoothDeviceDiscovery parent = org.easymock.EasyMock
				.createMock(BluetoothDeviceDiscovery.class);
		final DeviceDiscoveryAgent agent = new DeviceDiscoveryAgent(parent,
				DiscoveryMode.GIAC, false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				agent.doInquiry(null);
			}
		}).start();
		Thread.sleep(100); // Just to be sure, we're waiting.

		final DeviceDiscoveryListener listener = agent
				.getDeviceDiscoveryListener();

		final RemoteDevice unamed = new RemoteDeviceStub("000000000001", null);
		listener.deviceDiscovered(unamed, null);

		final RemoteDevice named = new RemoteDeviceStub("000000000002", "test");
		listener.deviceDiscovered(named, null);

		listener.inquiryCompleted(0);
	}

}
