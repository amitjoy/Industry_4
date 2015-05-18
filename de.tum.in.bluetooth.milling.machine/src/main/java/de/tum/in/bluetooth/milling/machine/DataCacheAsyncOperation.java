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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Callable;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import de.tum.in.events.EventConstants;

/**
 * Asynchronous Operation to save the data to a cache
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public final class DataCacheAsyncOperation implements
		AsyncFunction<String, String> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DataCacheAsyncOperation.class);

	/**
	 * The Thread Pool to run the function inside
	 */
	private final ListeningExecutorService m_poolToRunFunctionIn;

	/**
	 * OSGi Event Admin Service Reference
	 */
	private final EventAdmin m_eventAdmin;

	/**
	 * OSGi
	 */
	private final String m_deviceAddress;

	/**
	 * Constructor
	 * 
	 * @param eventAdmin
	 * @param remoteDeviceAddress
	 */
	public DataCacheAsyncOperation(
			ListeningExecutorService poolToRunFunctionIn,
			EventAdmin eventAdmin, String remoteDeviceAddress) {
		this.m_poolToRunFunctionIn = poolToRunFunctionIn;
		this.m_eventAdmin = eventAdmin;
		this.m_deviceAddress = remoteDeviceAddress;
	}

	/** {@inheritDoc} */
	@Override
	public ListenableFuture<String> apply(String input) throws Exception {
		return m_poolToRunFunctionIn.submit(new FunctionWorker(input));
	}

	/**
	 * 'worker' for the AsyncFunction
	 */
	private final class FunctionWorker implements Callable<String> {
		/**
		 * The input data
		 */
		private final String input;

		/**
		 * Constructor
		 */
		public FunctionWorker(String input) {
			this.input = input;
		}

		/** {@inheritDoc} */
		@Override
		public String call() throws Exception {
			LOGGER.debug("Asynchronous Operation starting...");

			final Dictionary properties = new Hashtable();
			properties.put("device.id", m_deviceAddress);
			properties.put("timestamp",
					String.valueOf(System.currentTimeMillis()));
			// properties.put("class.name", MillingMachineData.class.getName());

			final Event cacheEvent = new Event(
					EventConstants.MILLING_MACHINE_DATA_CACHE, properties);
			m_eventAdmin.sendEvent(cacheEvent);

			return input;
		}
	}

}
