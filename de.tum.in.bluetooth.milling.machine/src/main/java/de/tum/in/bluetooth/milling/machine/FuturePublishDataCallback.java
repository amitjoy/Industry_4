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
import org.eclipse.kura.message.KuraPayload;
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
public final class FuturePublishDataCallback implements FutureCallback<String> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FuturePublishDataCallback.class);

	/**
	 * Cloud Application Client
	 */
	private final CloudClient m_cloudApplicationClient;

	/**
	 * Publish Message Priority
	 */
	int m_dfltPriority;

	/**
	 * Publish Message QoS
	 */
	private final int m_dfltPubQos;

	/**
	 * Publish Message Retain Value
	 */
	boolean m_dfltRetain;

	/**
	 * The topic to publish for logging
	 */
	private final String m_topic;

	/**
	 * Constructor
	 */
	public FuturePublishDataCallback(final CloudClient cloudApplicationClient, final String topic, final int dfltPubQos,
			final boolean dfltRetain, final int dfltPriority) {
		this.m_cloudApplicationClient = cloudApplicationClient;
		this.m_topic = topic;
		this.m_dfltPubQos = dfltPubQos;
		this.m_dfltRetain = dfltRetain;
	}

	/** {@inheritDoc} */
	@Override
	public void onFailure(final Throwable t) {
		Throwables.propagate(t);
	}

	/** {@inheritDoc} */
	@Override
	public void onSuccess(final String result) {
		try {
			// will publish data to
			// $EDC/account_name/device_id/MILLING-V1/milling_machine for Mobile
			// Clients
			final KuraPayload kuraPayload = new KuraPayload();
			kuraPayload.addMetric("result", result);

			this.m_cloudApplicationClient.controlPublish("milling_machine", kuraPayload, this.m_dfltPubQos,
					this.m_dfltRetain, this.m_dfltPriority);

			// will publish data for Splunk Logging
			this.m_cloudApplicationClient.publish(this.m_topic, result.toString().getBytes(), 1, true,
					this.m_dfltPriority);
		} catch (final KuraException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
	}
}