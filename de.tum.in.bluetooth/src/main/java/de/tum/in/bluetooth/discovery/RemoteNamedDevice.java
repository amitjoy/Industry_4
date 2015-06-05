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

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connection;

/**
 * A class extending {@link RemoteDevice} but setting the friendly name.
 *
 * @See {@link RemoteDevice}
 * @author AMIT KUMAR MONDAL
 */
public class RemoteNamedDevice extends RemoteDevice {

	public final RemoteDevice device;

	public final String friendlyName;

	protected RemoteNamedDevice(final RemoteDevice device, final String name) {
		super(device.getBluetoothAddress());
		this.device = device;
		this.friendlyName = name;
	}

	@Override
	public boolean authenticate() throws IOException {
		return this.device.authenticate();
	}

	@Override
	public boolean authorize(final Connection conn) throws IOException {
		return this.device.authorize(conn);
	}

	@Override
	public boolean encrypt(final Connection conn, final boolean on) throws IOException {
		return this.device.encrypt(conn, on);
	}

	@Override
	public boolean equals(final Object obj) {
		return this.device.equals(obj);
	}

	@Override
	public String getFriendlyName(final boolean alwaysAsk) throws IOException {
		return this.friendlyName;
	}

	@Override
	public int hashCode() {
		return this.device.hashCode();
	}

	@Override
	public boolean isAuthenticated() {
		return this.device.isAuthenticated();
	}

	@Override
	public boolean isAuthorized(final Connection conn) throws IOException {
		return this.device.isAuthorized(conn);
	}

	@Override
	public boolean isEncrypted() {
		return this.device.isEncrypted();
	}

	@Override
	public boolean isTrustedDevice() {
		return this.device.isTrustedDevice();
	}
}
