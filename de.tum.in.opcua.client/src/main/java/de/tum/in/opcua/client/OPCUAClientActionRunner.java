/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
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
package de.tum.in.opcua.client;

import static com.digitalpetri.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.sdk.client.OpcUaClient;
import com.digitalpetri.opcua.sdk.client.api.config.OpcUaClientConfig;
import com.digitalpetri.opcua.stack.client.UaTcpStackClient;
import com.digitalpetri.opcua.stack.core.Stack;
import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.digitalpetri.opcua.stack.core.types.builtin.LocalizedText;
import com.digitalpetri.opcua.stack.core.types.structured.EndpointDescription;
import com.google.common.base.Throwables;

import de.tum.in.opcua.client.util.KeyStoreLoader;

/**
 * Consumes {@link OPCUAClientAction} OSGi Services
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class OPCUAClientActionRunner {
	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OPCUAClientActionRunner.class);

	/**
	 * The provided client action to work
	 */
	private final OPCUAClientAction clientAction;

	/**
	 * OPC-UA Endpoint URL
	 */
	private final String endpointUrl;

	/**
	 * The worker thread for the action to perform
	 */
	private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

	/**
	 * The keystore loader
	 */
	private final KeyStoreLoader loader = new KeyStoreLoader();

	/**
	 * Security Policy for the action to perform
	 */
	private final SecurityPolicy securityPolicy;

	/**
	 * Constructor
	 */
	public OPCUAClientActionRunner(final String endpointUrl, final SecurityPolicy securityPolicy,
			final OPCUAClientAction clientExample) {
		this.endpointUrl = endpointUrl;
		this.securityPolicy = securityPolicy;
		this.clientAction = clientExample;
	}

	/**
	 * Creates the OPC-UA Client reference for the action needed to perform
	 */
	private OpcUaClient createClient() throws Exception {
		final EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(this.endpointUrl).get();

		final EndpointDescription endpoint = Arrays.stream(endpoints)
				.filter(e -> e.getSecurityPolicyUri().equals(this.securityPolicy.getSecurityPolicyUri())).findFirst()
				.orElseThrow(() -> new Exception("no desired endpoints returned"));

		LOGGER.info("Using endpoint: " + endpoint.getEndpointUrl() + " with " + this.securityPolicy);

		this.loader.load();

		final OpcUaClientConfig config = OpcUaClientConfig.builder()
				.setApplicationName(LocalizedText.english("digitalpetri opc-ua client"))
				.setApplicationUri("urn:digitalpetri:opcua:client").setCertificate(this.loader.getClientCertificate())
				.setKeyPair(this.loader.getClientKeyPair()).setEndpoint(endpoint).setRequestTimeout(uint(5000)).build();

		return new OpcUaClient(config);
	}

	/**
	 * Finalize the required action
	 */
	public void run() {
		this.future.whenComplete((c, ex) -> {
			if (c != null) {
				try {
					c.disconnect().get(1, TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					LOGGER.error("Error waiting for disconnect: " + Throwables.getStackTraceAsString(e));
				}
			} else {
				LOGGER.error("Error running example: " + Throwables.getStackTraceAsString(ex));
			}

			Stack.releaseSharedResources();
			System.exit(0);
		});

		try {
			final OpcUaClient client = this.createClient();

			try {
				this.clientAction.run(client, this.future);
				this.future.get(5, TimeUnit.SECONDS);
			} catch (final Throwable t) {
				LOGGER.error("Error running client example: " + Throwables.getStackTraceAsString(t));
				this.future.complete(client);
			}
		} catch (final Throwable t) {
			this.future.completeExceptionally(t);
		}
	}
}
