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

import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Callback to be used when {@link ListenableFuture} would be processed
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public final class MyFutureCallback implements FutureCallback<String> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MyFutureCallback.class);

	/**
	 * Cloud Application Client
	 */
	private final CloudClient m_cloudApplicationClient;

	/**
	 * The Bluetooth address of the milling machine
	 */
	private final String m_remoteDeviceAddress;

	/**
	 * Publish Message QoS
	 */
	private final int m_dfltPubQos;

	/**
	 * Publish Message Retain Value
	 */
	boolean m_dfltRetain;

	/**
	 * Publish Message Priority
	 */
	int m_dfltPriority;

	/**
	 * Constructor
	 */
	public MyFutureCallback(CloudClient cloudApplicationClient,
			String remoteDeviceAddress, int dfltPubQos, boolean dfltRetain,
			int dfltPriority) {
		this.m_cloudApplicationClient = cloudApplicationClient;
		this.m_remoteDeviceAddress = remoteDeviceAddress;
		this.m_dfltPubQos = dfltPubQos;
		this.m_dfltRetain = dfltRetain;
	}

	/** {@inheritDoc} */
	@Override
	public void onSuccess(String result) {
		try {
			// will publish data to
			// $EDC/app_id/client_id/milling_machine/{some_bluetooth_address}
			m_cloudApplicationClient.controlPublish("milling_machine",
					m_remoteDeviceAddress, result.getBytes(), m_dfltPubQos,
					m_dfltRetain, m_dfltPriority);
		} catch (final KuraException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onFailure(Throwable t) {
		Throwables.propagate(t);
	}
}