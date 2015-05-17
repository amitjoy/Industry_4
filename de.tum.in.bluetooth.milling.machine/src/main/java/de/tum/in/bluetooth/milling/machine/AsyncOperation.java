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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import javax.bluetooth.RemoteDevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import de.tum.in.bluetooth.milling.machine.data.RealtimeData;

/**
 * Asynchronous Operation to save the data to a cache
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public final class AsyncOperation implements AsyncFunction<String, String> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MyFutureCallback.class);

	/**
	 * Holds List of results retrieved by all the paired devices (used as cache
	 * storage)
	 */
	private static ConcurrentMap<String, RealtimeData> m_realTimeData;

	/**
	 * Cache Initial Capacity
	 */
	private static final int CACHE_INITAL_CAPACITY = 50000;

	/**
	 * The Thread Pool to run the function inside
	 */
	private final ListeningExecutorService poolToRunFunctionIn;

	/**
	 * Constructor
	 */
	public AsyncOperation(ListeningExecutorService poolToRunFunctionIn) {
		this.poolToRunFunctionIn = poolToRunFunctionIn;
		this.m_realTimeData = new MapMaker().concurrencyLevel(2).weakValues()
				.initialCapacity(CACHE_INITAL_CAPACITY).makeMap();
	}

	/** {@inheritDoc} */
	@Override
	public ListenableFuture<String> apply(String input) throws Exception {
		return poolToRunFunctionIn.submit(new FunctionWorker(input));
	}

	/**
	 * 'worker' for the AsyncFunction
	 */
	private static final class FunctionWorker implements Callable<String> {
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
			// TO-DO Add async operation to save to cache
			return input;
		}
	}

	/**
	 * Used to wrap data for the predefined format needed
	 * 
	 * @param bluetoothAddress
	 *            the bluetooth address of the {@link RemoteDevice}
	 * @param realtimeData
	 *            The data retrieved from the input stream
	 * @return
	 */
	private static <T extends RealtimeData> T wrapData(String bluetoothAddress,
			String realtimeData) {
		// for temporary purposes, it is hardcoded to be a default predefined
		// format
		final RealtimeData data = new RealtimeData(bluetoothAddress,
				realtimeData);
		return ((T) data);
	}

}
