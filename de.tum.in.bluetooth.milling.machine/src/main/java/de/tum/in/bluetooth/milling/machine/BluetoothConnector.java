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
package de.tum.in.bluetooth.milling.machine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.ServiceRecord;
import javax.microedition.io.StreamConnection;

import org.osgi.service.io.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Used to establish connection between the paired bluetooth device and RPi
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class BluetoothConnector {

	/**
	 * Builder class to set Service Records and Connector Service
	 */
	public static class Builder {

		/**
		 * builder to build the object
		 */
		public BluetoothConnector build() {
			return new BluetoothConnector();
		}

		/**
		 * Setter to set OSGi Connector Service
		 */
		public Builder setConnectorService(final ConnectorService connectorService) {
			s_connectorService = connectorService;
			return this;
		}

		/**
		 * Setter to set bluetooth Service record for paired device
		 */
		public Builder setServiceRecord(final ServiceRecord record) {
			s_serviceRecord = record;
			return this;
		}

	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothConnector.class);

	/**
	 * OSGi Connector Service
	 */
	private static ConnectorService s_connectorService;

	/**
	 * Paired Bluetooth Device Serial Port Profile Service
	 */
	private static ServiceRecord s_serviceRecord;

	/**
	 * Input Connection Stream for the paired bluetooth device
	 */
	private InputStream m_inputStream;

	/**
	 * Output Connection Stream for the paired bluetooth device
	 */
	private OutputStream m_outputStream;

	/**
	 * Stream connection between paired bluetooth device and RPi
	 */
	private StreamConnection m_streamConnection;

	/* Constructor */
	private BluetoothConnector() {
	}

	/**
	 * Used to establish connection between the paired bluetooth device and RPi
	 */
	public void connect() {
		LOGGER.info("Bluetooth Connection initiating for ... " + s_serviceRecord.getHostDevice().getBluetoothAddress());

		final String connectionURL = s_serviceRecord.getConnectionURL(0, false);
		try {
			LOGGER.info("Connecting to..." + s_serviceRecord.getHostDevice().getBluetoothAddress());
			this.m_streamConnection = (StreamConnection) s_connectorService.open(connectionURL, ConnectorService.READ,
					true);
		} catch (final IOException e) {
			LOGGER.error("Not able to connect to the remote device", Throwables.getStackTraceAsString(e));
		}

		LOGGER.info("Connection Established with " + s_serviceRecord.getHostDevice().getBluetoothAddress());
		try {
			LOGGER.info("Getting IO Streams for " + s_serviceRecord.getHostDevice().getBluetoothAddress());
			this.m_inputStream = this.m_streamConnection.openInputStream();
			this.m_outputStream = this.m_streamConnection.openOutputStream();
		} catch (final IOException e) {
			LOGGER.error("Unable to retrieve stream connection for remote device", Throwables.getStackTraceAsString(e));
		}
	}

	/**
	 * Getter to retrieve the established input connection
	 */
	public InputStream getInputStream() {
		return this.m_inputStream;
	}

	/**
	 * Getter to retrieve the established output connection
	 */
	public OutputStream getOutputStream() {
		return this.m_outputStream;
	}

}
