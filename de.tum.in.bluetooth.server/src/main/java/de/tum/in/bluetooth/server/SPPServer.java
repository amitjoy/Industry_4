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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.intel.bluetooth.RemoteDeviceHelper;

/**
 * Creates a bluetooth server instance
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class SPPServer {

	private static final String RESPONSE = "Greetings from serverland";

	public static void main(final String[] args) throws IOException {
		// display local device address and name
		final LocalDevice localDevice = LocalDevice.getLocalDevice();
		System.out.println("Address: " + localDevice.getBluetoothAddress());
		System.out.println("Name: " + localDevice.getFriendlyName());

		final SPPServer sampleSPPServer = new SPPServer();
		while (true) {
			sampleSPPServer.startServer();
		}

	}

	// start SPP server
	private void startServer() throws IOException {
		// Create a UUID for SPP
		final UUID uuid = new UUID("0000110100001000800000805F9B34FB", false);
		// Create the service URL
		final String connectionString = "btspp://localhost:" + uuid + ";name=Bluetooth Milling Machine Simulation";

		// open server URL
		final StreamConnectionNotifier streamConnNotifier = (StreamConnectionNotifier) Connector.open(connectionString);

		// Wait for client connection
		System.out.println("\nServer Started. Waiting for clients to connect...");
		final StreamConnection connection = streamConnNotifier.acceptAndOpen();

		// Authenticate the Remote Device with dummy PIN
		final RemoteDevice device = RemoteDevice.getRemoteDevice(connection);
		if (!RemoteDeviceHelper.isAuthenticated(device)) {
			if (!RemoteDeviceHelper.authenticate(device, "1111")) {
				return;
			}
		}
		System.out.println("Remote device address: " + device.getBluetoothAddress());
		System.out.println("Remote device name: " + device.getFriendlyName(true));

		// read string from SPP client
		final InputStream inStream = connection.openInputStream();
		final BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
		final String lineRead = bReader.readLine();
		System.out.println("Message from remote device: " + lineRead);

		// send response to SPP client
		final OutputStream outStream = connection.openOutputStream();
		final PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outStream));
		System.out.println("Sending response to remote device (" + RESPONSE + ")");
		pWriter.write(RESPONSE + "\r\n");
		pWriter.flush();

		pWriter.close();

		streamConnNotifier.close();
	}

}
