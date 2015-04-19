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
