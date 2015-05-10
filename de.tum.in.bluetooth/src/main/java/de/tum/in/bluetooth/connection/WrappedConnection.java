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
package de.tum.in.bluetooth.connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.InputConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to wrap Bluetooth Serial Profile Connection stream
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class WrappedConnection implements InputConnection {
	/**
	 * slf4j Logger
	 */
	private final Logger m_logger = LoggerFactory
			.getLogger(WrappedConnection.class);
	private final InputConnection m_connection;

	private DataInputStream m_dataInputStream;
	private InputStream m_inputStream;

	/** Constructor */
	public WrappedConnection(InputConnection connection) {
		m_logger.debug("Constructing wrapped connection");
		this.m_connection = connection;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {

		try {
			m_logger.debug("Closing connection");
			m_connection.close();
			if (m_dataInputStream != null) {
				m_logger.debug("Closing dataInputStream");
				m_dataInputStream.close();
			}
			if (m_inputStream != null) {
				m_logger.debug("Closing inputStream");
				m_inputStream.close();
			}
		} catch (final IOException e) {
			m_logger.error("Failed to close connection", e);
			throw e;
		}

	}

	/** {@inheritDoc} */
	@Override
	public DataInputStream openDataInputStream() throws IOException {
		m_logger.info("Opening DataInputStream connection");
		try {
			m_dataInputStream = m_connection.openDataInputStream();
			return m_dataInputStream;
		} catch (final IOException e) {
			m_logger.error("Failed to open connection", e);
			throw e;
		}
	}

	/** {@inheritDoc} */
	@Override
	public InputStream openInputStream() throws IOException {
		m_logger.debug("Opening InputStream connection");
		try {
			m_inputStream = m_connection.openInputStream();
			return m_inputStream;
		} catch (final IOException e) {
			m_logger.error("Failed to open connection", e);
			throw e;

		}

	}

}
