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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;

import org.osgi.service.io.ConnectionFactory;

import com.intel.bluetooth.MicroeditionConnector;

/**
 * Bluetooth Serial Port Profile Connection Factory Implementation
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class ConnectionFactoryImpl implements ConnectionFactory {

	/**
	 * Create a new <code>Connection</code> object for a comm specified URI.
	 *
	 * @param name
	 *            The full URI passed to the <code>ConnectorService.open</code>
	 *            method
	 * @param mode
	 *            The mode parameter passed to the
	 *            <code>ConnectorService.open</code> method
	 * @param timeouts
	 *            The timeouts parameter passed to the
	 *            <code>ConnectorService.open</code> method
	 * @return A new <code>javax.microedition.io.Connection</code> object.
	 * @throws IOException
	 *             If a <code>javax.microedition.io.Connection</code> object can
	 *             not not be created.
	 */
	@Override
	public Connection createConnection(String name, int mode, boolean timeouts)
			throws IOException {
		name = checkNotNull(name, "Connection URI must not be null");
		mode = checkNotNull(mode, "Connection Mode must not be null");

		return new WrappedConnection(
				(StreamConnection) MicroeditionConnector.open(name, mode,
						timeouts));

	}

}
