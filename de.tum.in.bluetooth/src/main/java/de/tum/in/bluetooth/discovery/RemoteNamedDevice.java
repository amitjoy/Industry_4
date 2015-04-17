package de.tum.in.bluetooth.discovery;

import java.io.IOException;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connection;

/**
 * A class extending RemoteDevice but setting the friendly name.
 */
public class RemoteNamedDevice extends RemoteDevice {

	public final String friendlyName;

	public final RemoteDevice device;

	protected RemoteNamedDevice(RemoteDevice device, String name) {
		super(device.getBluetoothAddress());
		this.device = device;
		this.friendlyName = name;
	}

	@Override
	public String getFriendlyName(boolean alwaysAsk) throws IOException {
		return friendlyName;
	}

	@Override
	public boolean isTrustedDevice() {
		return device.isTrustedDevice();
	}

	@Override
	public boolean authenticate() throws IOException {
		return device.authenticate();
	}

	@Override
	public boolean authorize(Connection conn) throws IOException {
		return device.authorize(conn);
	}

	@Override
	public boolean encrypt(Connection conn, boolean on) throws IOException {
		return device.encrypt(conn, on);
	}

	@Override
	public boolean isAuthenticated() {
		return device.isAuthenticated();
	}

	@Override
	public boolean isAuthorized(Connection conn) throws IOException {
		return device.isAuthorized(conn);
	}

	@Override
	public boolean isEncrypted() {
		return device.isEncrypted();
	}

	@Override
	public boolean equals(Object obj) {
		return device.equals(obj);
	}

	@Override
	public int hashCode() {
		return device.hashCode();
	}
}
