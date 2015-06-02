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
package de.tum.in.bluetooth;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.io.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.bluetooth.connection.ConnectionFactoryImpl;

/**
 * Activator used to register Bluetooth Serial Port Profile Connection factory
 * Implementation
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class Activator implements BundleActivator {

	// Schema provided for connections
	private static final String SCHEMA = "btspp";

	// The ConnectionFactory Service implementation
	private ConnectionFactory m_connectionFactory;

	/**
	 * slf4j Logger
	 */
	private final static Logger LOGGER = LoggerFactory
			.getLogger(Activator.class);

	@Override
	public void start(BundleContext context) throws Exception {
		final Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
		properties.put(ConnectionFactory.IO_SCHEME, new String[] { SCHEMA });
		m_connectionFactory = new ConnectionFactoryImpl();
		context.registerService(ConnectionFactory.class.getName(),
				m_connectionFactory, properties);
		LOGGER.debug("Started Bundle");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		m_connectionFactory = null;
		LOGGER.debug("Stopped Bundle");
	}

}
