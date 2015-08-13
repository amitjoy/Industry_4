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
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * Worker Thread to retrieve the data from the stream established between the
 * RPi and Bluetooth Device
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class DataRetrieverWorker implements Callable<String> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DataRetrieverWorker.class);

	/**
	 * The Bluetooth Communication Stream Connection
	 */
	private final BluetoothConnector m_bluetoothConnector;

	/**
	 * Constructor
	 */
	public DataRetrieverWorker(final BluetoothConnector bluetoothConnector) {
		this.m_bluetoothConnector = bluetoothConnector;
	}

	/** {@inheritDoc} */
	@Override
	public String call() throws Exception {
		LOGGER.debug("Data Retrieving from Milling Machines....");
		try {
			final InputStream inputStream = this.m_bluetoothConnector.getInputStream();
			final BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream));
			final String lineRead = bReader.readLine();

			LOGGER.debug("Bluetooth Data Received: " + lineRead);
			return Strings.isNullOrEmpty(lineRead) ? "" : lineRead;
		} catch (final IOException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
		return null;
	}
}
