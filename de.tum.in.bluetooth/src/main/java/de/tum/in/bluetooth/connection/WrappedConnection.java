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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Used to wrap Bluetooth Serial Profile Connection stream
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class WrappedConnection implements StreamConnection {

	public static void main(String[] args) {
		final WrappedConnection connection = new WrappedConnection(null);
		System.out.println(connection instanceof StreamConnection);
		System.out.println();
	}

	/**
	 * slf4j Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(WrappedConnection.class);
	private final StreamConnection m_connection;

	private DataInputStream m_dataInputStream;
	private InputStream m_inputStream;
	private DataOutputStream m_dataOutputStream;
	private OutputStream m_outputStream;

	/** Constructor */
	public WrappedConnection(StreamConnection connection) {
		LOGGER.debug("Constructing wrapped connection");
		this.m_connection = connection;
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {

		try {
			LOGGER.debug("Closing connection");
			m_connection.close();
			if (m_dataInputStream != null) {
				LOGGER.debug("Closing dataInputStream");
				m_dataInputStream.close();
			}
			if (m_inputStream != null) {
				LOGGER.debug("Closing inputStream");
				m_inputStream.close();
			}
		} catch (final IOException e) {
			LOGGER.error("Failed to close connection",
					Throwables.getStackTraceAsString(e));
			throw e;
		}

	}

	/** {@inheritDoc} */
	@Override
	public DataInputStream openDataInputStream() throws IOException {
		LOGGER.info("Opening DataInputStream connection");
		try {
			m_dataInputStream = m_connection.openDataInputStream();
			return m_dataInputStream;
		} catch (final IOException e) {
			LOGGER.error("Failed to open connection",
					Throwables.getStackTraceAsString(e));
			throw e;
		}
	}

	/** {@inheritDoc} */
	@Override
	public InputStream openInputStream() throws IOException {
		LOGGER.debug("Opening InputStream connection");
		try {
			m_inputStream = m_connection.openInputStream();
			return m_inputStream;
		} catch (final IOException e) {
			LOGGER.error("Failed to open connection",
					Throwables.getStackTraceAsString(e));
			throw e;

		}
	}

	@Override
	public DataOutputStream openDataOutputStream() throws IOException {
		LOGGER.debug("Opening DataOutputStream connection");
		try {
			m_dataOutputStream = m_connection.openDataOutputStream();
			return m_dataOutputStream;
		} catch (final IOException e) {
			LOGGER.error("Failed to open connection",
					Throwables.getStackTraceAsString(e));
			throw e;

		}
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		LOGGER.debug("Opening OutputStream connection");
		try {
			m_outputStream = m_connection.openOutputStream();
			return m_outputStream;
		} catch (final IOException e) {
			LOGGER.error("Failed to open connection",
					Throwables.getStackTraceAsString(e));
			throw e;

		}
	}

}
