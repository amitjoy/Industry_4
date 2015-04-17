package de.tum.in.bluetooth.discovery;

import java.io.IOException;

import javax.bluetooth.RemoteDevice;

public class RemoteDeviceStub extends RemoteDevice {
	public String address;

	public String name;

	protected RemoteDeviceStub(String address, String name) {
		super(address);
		this.address = address;
		this.name = name;
	}

	@Override
	public String getFriendlyName(boolean alwaysAsk) throws IOException {
		return name;
	}

}
