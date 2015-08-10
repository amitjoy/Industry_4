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
	public static class Builder {
		/**
		 * Application Certificate
		 */
		private String m_applicationCertificate;
		/**
		 * Application Name
		 */
		private String m_applicationName;
		/**
		 * Application Uri
		 */
		private String m_applicationUri;
		/**
		 * The provided client action to work
		 */
		private OPCUAClientAction m_clientAction;
		/**
		 * OPC-UA Endpoint URL
		 */
		private String m_endpointUrl;
		/**
		 * Keystore Client Alias
		 */
		private String m_keyStoreClientAlias;
		/**
		 * Keystore Password
		 */
		private String m_keyStorePassword;

		/**
		 * Keystore Server Alias
		 */
		private String m_keyStoreServerAlias;

		/**
		 * Security Policy for the action to perform
		 */
		private SecurityPolicy m_securityPolicy;

		/**
		 * Returns the main Runner
		 */
		public OPCUAClientActionRunner build() {
			return new OPCUAClientActionRunner(this.m_endpointUrl, this.m_securityPolicy, this.m_clientAction,
					this.m_keyStoreServerAlias, this.m_keyStoreClientAlias, this.m_keyStorePassword,
					this.m_applicationName, this.m_applicationUri, this.m_applicationCertificate);
		}

		/**
		 * Setter for Application Certificate
		 */
		public final Builder setApplicationCertificate(final String applicationCertificate) {
			this.m_applicationCertificate = applicationCertificate;
			return this;
		}

		/**
		 * Setter for Application Name
		 */
		public final Builder setApplicationName(final String applicationName) {
			this.m_applicationName = applicationName;
			return this;
		}

		/**
		 * Setter for Application URI
		 */
		public final Builder setApplicationUri(final String applicationUri) {
			this.m_applicationUri = applicationUri;
			return this;
		}

		/**
		 * Setter for Client Action
		 */
		public final Builder setClientAction(final OPCUAClientAction opcuaClientAction) {
			this.m_clientAction = opcuaClientAction;
			return this;
		}

		/**
		 * Setter for Endpoint URL
		 */
		public final Builder setEndpointUrl(final String endpointUrl) {
			this.m_endpointUrl = endpointUrl;
			return this;
		}

		/**
		 * Setter for Keystore Client Alias
		 */
		public final Builder setKeyStoreClientAlias(final String keyStoreClientAlias) {
			this.m_keyStoreClientAlias = keyStoreClientAlias;
			return this;
		}

		/**
		 * Setter for Keystore Password
		 */
		public final Builder setKeyStorePassword(final String keyStorePassword) {
			this.m_keyStorePassword = keyStorePassword;
			return this;
		}

		/**
		 * Setter for Keystore Server Alias
		 */
		public final Builder setKeyStoreServerAlias(final String keyStoreServerAlias) {
			this.m_keyStoreServerAlias = keyStoreServerAlias;
			return this;
		}

		/**
		 * Setter for Security Policy
		 */
		public final Builder setSecurityPolicy(final SecurityPolicy securityPolicy) {
			this.m_securityPolicy = securityPolicy;
			return this;
		}

	}

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OPCUAClientActionRunner.class);

	/**
	 * Application Certificate
	 */
	private final String m_applicationCertificate;

	/**
	 * Application Name
	 */
	private final String m_applicationName;

	/**
	 * Application Uri
	 */
	private final String m_applicationUri;

	/**
	 * The provided client action to work
	 */
	private final OPCUAClientAction m_clientAction;

	/**
	 * OPC-UA Endpoint URL
	 */
	private final String m_endpointUrl;

	/**
	 * The worker thread for the action to perform
	 */
	private final CompletableFuture<OpcUaClient> m_future = new CompletableFuture<>();

	/**
	 * Keystore Client Alias
	 */
	private final String m_keyStoreClientAlias;

	/**
	 * Keystore Password
	 */
	private final String m_keyStorePassword;

	/**
	 * Keystore Server Alias
	 */
	private final String m_keyStoreServerAlias;

	/**
	 * The keystore loader
	 */
	private final KeyStoreLoader m_loader;

	/**
	 * Security Policy for the action to perform
	 */
	private final SecurityPolicy m_securityPolicy;

	/**
	 * Constructor
	 */
	private OPCUAClientActionRunner(final String endpointUrl, final SecurityPolicy securityPolicy,
			final OPCUAClientAction clientAction, final String keystoreServerAlias, final String keystoreClientAlias,
			final String keystorePassword, final String applicationName, final String applicationUri,
			final String applicationCert) {
		this.m_endpointUrl = endpointUrl;
		this.m_securityPolicy = securityPolicy;
		this.m_clientAction = clientAction;
		this.m_applicationName = applicationName;
		this.m_applicationUri = applicationUri;
		this.m_keyStoreClientAlias = keystoreClientAlias;
		this.m_keyStoreServerAlias = keystoreServerAlias;
		this.m_keyStorePassword = keystorePassword;
		this.m_applicationCertificate = applicationCert;
		this.m_loader = new KeyStoreLoader(keystoreClientAlias, keystoreServerAlias, keystorePassword,
				this.m_applicationCertificate);
	}

	/**
	 * Creates the OPC-UA Client reference for the action needed to perform
	 */
	private OpcUaClient createClient() throws Exception {
		final EndpointDescription[] endpoints = UaTcpStackClient.getEndpoints(this.m_endpointUrl).get();

		final EndpointDescription endpoint = Arrays.stream(endpoints)
				.filter(e -> e.getSecurityPolicyUri().equals(this.m_securityPolicy.getSecurityPolicyUri())).findFirst()
				.orElseThrow(() -> new Exception("no desired endpoints returned"));

		LOGGER.info("Using endpoint: " + endpoint.getEndpointUrl() + " with " + this.m_securityPolicy);

		this.m_loader.load();

		final OpcUaClientConfig config = OpcUaClientConfig.builder()
				.setApplicationName(LocalizedText.english(this.m_applicationName))
				.setApplicationUri(this.m_applicationUri).setCertificate(this.m_loader.getClientCertificate())
				.setKeyPair(this.m_loader.getClientKeyPair()).setEndpoint(endpoint).setRequestTimeout(uint(5000))
				.build();

		return new OpcUaClient(config);
	}

	/**
	 * Finalize the required action
	 */
	public void run() {
		this.m_future.whenComplete((c, ex) -> {
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
				this.m_clientAction.run(client, this.m_future);
				this.m_future.get(5, TimeUnit.SECONDS);
			} catch (final Throwable t) {
				LOGGER.error("Error running client example: " + Throwables.getStackTraceAsString(t));
				this.m_future.complete(client);
			}
		} catch (final Throwable t) {
			this.m_future.completeExceptionally(t);
		}
	}
}
