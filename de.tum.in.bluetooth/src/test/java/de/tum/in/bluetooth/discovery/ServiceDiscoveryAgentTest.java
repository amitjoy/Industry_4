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
import javax.bluetooth.ServiceRecord;

import org.junit.After;
import org.junit.Test;

/**
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class ServiceDiscoveryAgentTest {

	@After
	public void tearDown() {
		Env.disableTestEnvironment();
	}

	@Test
	public void testRun() {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}

		final BluetoothServiceDiscovery parent = org.easymock.EasyMock
				.createMock(BluetoothServiceDiscovery.class);
		final RemoteDevice remote = new RemoteDeviceStub("000000000001", "test");
		final ServiceDiscoveryAgent agent = new ServiceDiscoveryAgent(parent,
				remote);
		agent.run();
	}

	@Test
	public void testSearch() throws InterruptedException {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}
		Env.enableTestEnvironment();

		final BluetoothServiceDiscovery parent = org.easymock.EasyMock
				.createMock(BluetoothServiceDiscovery.class);
		final RemoteDevice remote = new RemoteDeviceStub("000000000001", "test");
		final ServiceDiscoveryAgent agent = new ServiceDiscoveryAgent(parent,
				remote);

		new Thread(new Runnable() {
			@Override
			public void run() {
				agent.doSearch(null);
			}
		}).start();

		Thread.sleep(100); // Just to be sure, we're waiting.
		final ServiceRecordStub srs = new ServiceRecordStub(remote, "test");
		final ServiceRecordStub srs2 = new ServiceRecordStub(remote, "test-2");
		final ServiceRecordStub srs3 = new ServiceRecordStub(remote, "test-3");
		agent.servicesDiscovered(0, new ServiceRecord[] { srs, srs2, srs3 });

		agent.serviceSearchCompleted(0, 0);
	}

	@Test
	public void testSearchAborted() throws InterruptedException {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}
		Env.enableTestEnvironment();

		final BluetoothServiceDiscovery parent = org.easymock.EasyMock
				.createMock(BluetoothServiceDiscovery.class);
		final RemoteDevice remote = new RemoteDeviceStub("000000000001", "test");
		final ServiceDiscoveryAgent agent = new ServiceDiscoveryAgent(parent,
				remote);

		final Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				agent.doSearch(null);
			}
		});
		t.start();

		Thread.sleep(100); // Just to be sure, we're waiting.
		t.interrupt();

	}

}
