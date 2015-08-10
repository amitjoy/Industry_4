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
package de.tum.in.opcua.client;

import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitalpetri.opcua.stack.core.security.SecurityPolicy;
import com.google.common.collect.Lists;

/**
 * This bundle is responsible for communicating with the OPC-UA Server
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.opcua.client")
@Service(value = { OpcUaClient.class })
public class OpcUaClient implements ConfigurableComponent {

	/**
	 * Configurable property to set client alias for the keystore
	 */
	private static final String KEYSTORE_CLIENT_ALIAS = "keystore.client.alias";

	/**
	 * Configurable Property to set keystore password
	 */
	private static final String KEYSTORE_PASSWORD = "keystore.password";

	/**
	 * Configurable Property to set server alias for the keystore
	 */
	private static final String KEYSTORE_SERVER_ALIAS = "keystore.server.alias";

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaClient.class);

	/**
	 * Configurable Property to set opc-ua application certificate
	 */
	private static final String OPCUA_APPLICATION_CERTIFICATE = "opcua.application.certificate";

	/**
	 * Configurable Property to set opc-ua application name
	 */
	private static final String OPCUA_APPLICATION_NAME = "opcua.application.name";

	/**
	 * Configurable Property to set opc-ua application uri
	 */
	private static final String OPCUA_APPLICATION_URI = "opcua.application.uri";

	/**
	 * Configurable property specifying the Security Policy
	 */
	private static final String OPCUA_SECURITY_POLICY = "opcua.security.policy";

	/**
	 * Placeholder for keystore client alias
	 */
	private String m_keystoreClientAlias;

	/**
	 * Placeholder for keystore password
	 */
	private String m_keystorePassword;

	/**
	 * Placeholder for keystore server alias
	 */
	private String m_keystoreServerAlias;

	/**
	 * Placeholder for opc-ua certificate location
	 */
	private String m_opcuaApplicationCert;

	/**
	 * Placeholder for opc-ua application name
	 */
	private String m_opcuaApplicationName;

	/**
	 * Placeholder for opc-ua application uri
	 */
	private String m_opcuaApplicationUri;

	/**
	 * OPC-UA Client Service Injection
	 */
	@Reference(bind = "bindOpcUa", unbind = "unbindOpcUa", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private volatile OpcUaClientAction m_opcuaClientAction;

	/**
	 * Holds List of {@link OpcUaClientAction}
	 */
	private final List<OpcUaClientAction> m_opcuaClientActions = Lists.newCopyOnWriteArrayList();

	/**
	 * Placeholder for security policy
	 */
	private SecurityPolicy m_opcuaSecurityPolicy;

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/**
	 * Placeholder for security policy
	 */
	private int m_securityPolicy;

	/* Constructor */
	public OpcUaClient() {
	}

	/**
	 * Callback used when this service component is activating
	 */
	@Activate
	protected synchronized void activate(final ComponentContext componentContext,
			final Map<String, Object> properties) {
		LOGGER.info("Activating OPC-UA Component...");

		this.m_properties = properties;
		this.extractConfiguration();
		this.configureSecurityPolicy();

		this.m_opcuaClientActions.forEach(opcuaClientAction -> {
			final OpcUaClientActionRunner clientActionRunner = new OpcUaClientActionRunner.Builder()
					.setApplicationName(this.m_opcuaApplicationName).setApplicationUri(this.m_opcuaApplicationUri)
					.setApplicationCertificate(this.m_opcuaApplicationCert)
					.setKeyStoreClientAlias(this.m_keystoreClientAlias).setKeyStorePassword(this.m_keystorePassword)
					.setKeyStoreServerAlias(this.m_keystoreServerAlias)
					.setEndpointUrl(opcuaClientAction.getEndpointUrl()).setSecurityPolicy(this.m_opcuaSecurityPolicy)
					.build();
			clientActionRunner.run();
		});

		LOGGER.info("Activating OPC-UA Component... Done.");

	}

	/**
	 * Callback to be used while {@link OpcUaClientAction} is registering
	 */
	public synchronized void bindOpcUa(final OpcUaClientAction opcuaClientAction) {
		if (!this.m_opcuaClientActions.contains(opcuaClientAction)) {
			this.m_opcuaClientActions.add(opcuaClientAction);
		}
	}

	/**
	 * Retrieves Proper Security Policy
	 */
	private void configureSecurityPolicy() {
		switch (this.m_securityPolicy) {
		case 0:
			this.m_opcuaSecurityPolicy = SecurityPolicy.None;
			break;
		case 1:
			this.m_opcuaSecurityPolicy = SecurityPolicy.Basic128Rsa15;
			break;
		case 2:
			this.m_opcuaSecurityPolicy = SecurityPolicy.Basic256;
			break;
		case 3:
			this.m_opcuaSecurityPolicy = SecurityPolicy.Basic256Sha256;
			break;
		}

	}

	/**
	 * Callback used when this service component is deactivating
	 */
	@Deactivate
	protected void deactivate(final ComponentContext context) {
		LOGGER.debug("Deactivating OPC-UA Component...");

		LOGGER.debug("Deactivating OPC-UA Component... Done.");
	}

	/**
	 * Used to extract configuration for populating placeholders with respective
	 * values
	 */
	private void extractConfiguration() {
		this.m_keystoreClientAlias = (String) this.m_properties.get(KEYSTORE_CLIENT_ALIAS);
		this.m_keystoreServerAlias = (String) this.m_properties.get(KEYSTORE_SERVER_ALIAS);
		this.m_keystorePassword = (String) this.m_properties.get(KEYSTORE_PASSWORD);
		this.m_opcuaApplicationName = (String) this.m_properties.get(OPCUA_APPLICATION_NAME);
		this.m_opcuaApplicationUri = (String) this.m_properties.get(OPCUA_APPLICATION_URI);
		this.m_opcuaApplicationCert = (String) this.m_properties.get(OPCUA_APPLICATION_CERTIFICATE);
		this.m_securityPolicy = (int) this.m_properties.get(OPCUA_SECURITY_POLICY);
	}

	/**
	 * Callback to be used while {@link OpcUaClientAction} is deregistering
	 */
	public synchronized void unbindOpcUa(final OpcUaClientAction opcuaClientAction) {
		if ((this.m_opcuaClientActions.size() > 0) && this.m_opcuaClientActions.contains(opcuaClientAction)) {
			this.m_opcuaClientActions.remove(opcuaClientAction);
		}
	}

	/**
	 * Used to be called when configurations will get updated
	 */
	public void updated(final Map<String, Object> properties) {
		LOGGER.info("Updated OPC-UA Component...");

		this.m_properties = properties;
		this.extractConfiguration();
		this.configureSecurityPolicy();

		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updated OPC-UA Component... Done.");
	}

}