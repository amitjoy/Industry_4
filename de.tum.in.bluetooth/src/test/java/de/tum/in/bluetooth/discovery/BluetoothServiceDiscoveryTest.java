package de.tum.in.bluetooth.discovery;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import de.tum.in.bluetooth.devices.Device;

/**
 * Bluetooth Service Discovery Test Cases
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class BluetoothServiceDiscoveryTest {

	@Test
	public void testBluetoothServiceDiscovery() throws InterruptedException {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}

		final BundleContext context = EasyMock.createMock(BundleContext.class);
		final BluetoothServiceDiscovery bsd = new BluetoothServiceDiscovery(
				context);

		final RemoteDevice unamed = new RemoteDeviceStub("000000000001", null);
		final RemoteDevice named = new RemoteDeviceStub("000000000002", "test");

		bsd.bindRemoteDevice(unamed);
		bsd.bindRemoteDevice(named);

		bsd.unbindRemoteDevice(unamed);

		Thread.sleep(5000);

		bsd.stop();
	}

	@Test
	public void testDiscoveredServices() throws InterruptedException {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}

		final BundleContextStub context = new BundleContextStub();
		final BluetoothServiceDiscovery bsd = new BluetoothServiceDiscovery(
				context);

		final RemoteDevice device = new RemoteDeviceStub("000000000001", "test");
		final ServiceRecordStub srs1 = new ServiceRecordStub(device, "test");

		bsd.discovered(device, Arrays.asList(new ServiceRecord[] { srs1 }));

		Assert.assertEquals(1, context.getServices().size());

		bsd.stop();
	}

	@Test
	public void testRetry() throws Exception {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}

		final BundleContextStub context = new BundleContextStub();
		final BluetoothServiceDiscovery osd = new BluetoothServiceDiscovery(
				context);
		osd.setDeviceFile(new File("src/test/resources/devices.xml"));
		final RemoteDevice device = new RemoteDeviceStub("000000000001",
				"TDU_1111111");

		osd.discovered(device, Arrays.asList(new ServiceRecord[] {}));

		Thread.sleep(60000);

		Assert.assertEquals(0, context.getServices().size());

		osd.stop();
	}

	@Test
	public void testRegexForAuthentication() throws IOException {
		if (!LocalDevice.isPowerOn()) {
			System.err.println("Bluetooth Adapter required");
			return;
		}

		final BundleContextStub context = new BundleContextStub();
		final BluetoothServiceDiscovery osd = new BluetoothServiceDiscovery(
				context);
		osd.setDeviceFile(new File("src/test/resources/devices.xml"));

		RemoteDevice device = new RemoteDeviceStub("000000000003",
				"TDU_00000000");
		Device dev = osd.findDeviceFromFleet(device);
		Assert.assertNotNull(dev);

		device = new RemoteDeviceStub("1000E8C18C85", null);
		dev = osd.findDeviceFromFleet(device);
		Assert.assertNotNull(dev);

		device = new RemoteDeviceStub("000000000003", "xxx");
		dev = osd.findDeviceFromFleet(device);
		Assert.assertNull(dev);
	}

}
