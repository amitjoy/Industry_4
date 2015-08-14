/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
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
package de.tum.in.bluetooth.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Creates a bluetooth server instance
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class SPPServer {

	private static StreamConnection connection = null;

	private static StreamConnectionNotifier streamConnNotifier = null;

	private static PrintWriter writer = null;

	private static void init() throws IOException {
		final UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
		final String connectionString = "btspp://localhost:" + uuid + ";name=Bluetooth-Milling-Machine-Simulation";

		streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

		System.out.println("Milling Machine Started. Waiting for clients to connect...");
		connection = streamConnNotifier.acceptAndOpen();

		final RemoteDevice device = RemoteDevice.getRemoteDevice(connection);

		System.out.println("Remote device address: " + device.getBluetoothAddress());
		System.out.println("Remote device name: " + device.getFriendlyName(true));

		final OutputStream outStream = connection.openOutputStream();
		writer = new PrintWriter(new OutputStreamWriter(outStream));
	}

	public static void main(final String[] args) throws IOException {
		final LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		init();
		while (!Thread.currentThread().isInterrupted()) {
			sendResponse();
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private static void sendResponse() throws IOException {
		final String data = RandomStringUtils.randomAlphanumeric(5);
		System.out.println("Broadcasting data (" + data + ")");
		writer.write(data + "\r\n");
	}

}
