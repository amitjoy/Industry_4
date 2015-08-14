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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.bluetooth.ServiceRecord;
import javax.microedition.io.StreamConnection;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.message.KuraPayload;
import org.osgi.service.io.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.intel.bluetooth.MicroeditionConnector;

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
		 * Setter to set bluetooth Service record for paired device
		 */
		public Builder setCloudClient(final CloudClient client) {
			s_cloudClient = client;
			return this;
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

		/**
		 * Setter to set Bluetooth Real-time topic
		 */
		public Builder setTopic(final String topic) {
			s_topic = topic;
			return this;
		}

	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BluetoothConnector.class);

	/**
	 * Paired Bluetooth Device Serial Port Profile Service
	 */
	private static CloudClient s_cloudClient;

	/**
	 * OSGi Connector Service
	 */
	@SuppressWarnings("unused")
	private static ConnectorService s_connectorService;

	/**
	 * Paired Bluetooth Device Serial Port Profile Service
	 */
	private static ServiceRecord s_serviceRecord;

	/**
	 * Bluetooth Real-time Topic
	 */
	private static String s_topic;

	/**
	 * Buffer Reader for the paired bluetooth device
	 */
	private BufferedReader m_bufferedReader;

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

	/**
	 * The response from the input stream
	 */
	private String response;

	/* Constructor */
	private BluetoothConnector() {
	}

	/**
	 * Used to establish connection between the paired bluetooth device and RPi
	 */
	public void connect() {
		LOGGER.info("Bluetooth Connection initiating for ... " + s_serviceRecord.getHostDevice().getBluetoothAddress());

		final String connectionURL = s_serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
		try {
			LOGGER.info("Connecting to..." + s_serviceRecord.getHostDevice().getBluetoothAddress()
					+ " with connection url " + connectionURL);
			this.m_streamConnection = (StreamConnection) MicroeditionConnector.open(connectionURL,
					ConnectorService.READ_WRITE, false);
			LOGGER.info("Successfully Connected to " + s_serviceRecord.getHostDevice().getBluetoothAddress()
					+ " with stream " + this.m_streamConnection);
		} catch (final IOException e) {
			LOGGER.error("Not able to connect to the remote device. " + Throwables.getStackTraceAsString(e));
		}
		LOGGER.info("Connection Established with " + s_serviceRecord.getHostDevice().getBluetoothAddress());
		try {
			LOGGER.info("Getting IO Streams for " + s_serviceRecord.getHostDevice().getBluetoothAddress());
			this.doRead();
			this.doPublish();
			LOGGER.debug(
					"Streams Returned-> InputStream: " + this.m_inputStream + " OutputStream: " + this.m_outputStream);
		} catch (final Exception e) {
			LOGGER.error(
					"Unable to retrieve stream connection for remote device" + Throwables.getStackTraceAsString(e));
		}
	}

	/**
	 * Publish the data to message broker
	 */
	private void doPublish() throws KuraException {
		LOGGER.debug("Publishing Bluetooth Data.....");
		final KuraPayload payload = new KuraPayload();
		payload.addMetric("result", this.response);
		// publishing for mobile client
		LOGGER.debug("Publishing Bluetooth Data.....to Mobile Clients");
		s_cloudClient.controlPublish("milling_machine", payload, 0, false, 5);
		// publishing for Splunk
		LOGGER.debug("Publishing Bluetooth Data.....to Splunk");
		s_cloudClient.publish(s_topic, this.response.getBytes(), 0, false, 5);
		LOGGER.debug("Publishing Bluetooth Data.....Done");
	}

	/**
	 * Reading data from the stream
	 */
	private void doRead() {
		try {
			this.m_inputStream = this.m_streamConnection.openInputStream();
			LOGGER.debug("Input Stream: " + this.m_inputStream);
			this.m_bufferedReader = new BufferedReader(new InputStreamReader(this.m_inputStream));
			LOGGER.info("Buffered Reader: " + this.m_bufferedReader);
			this.response = this.m_bufferedReader.readLine();
			LOGGER.debug("Bluetooth Data Received: " + this.response);
		} catch (final Exception e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		} finally {
			try {
				if ((this.m_inputStream != null) && (this.m_bufferedReader != null)) {
					this.m_inputStream.close();
					this.m_bufferedReader.close();
				}
			} catch (final Exception e) {
				LOGGER.error("Error closing input stream");
			}
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
