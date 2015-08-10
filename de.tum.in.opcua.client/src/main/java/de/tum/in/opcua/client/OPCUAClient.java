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

import com.google.common.collect.Lists;

/**
 * This bundle is responsible for communicating with the OPC-UA Server
 *
 * @author AMIT KUMAR MONDAL
 *
 */
@Component(immediate = true, name = "de.tum.in.opcua.client")
@Service(value = { OPCUAClient.class })
public class OPCUAClient implements ConfigurableComponent {

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
	private static final Logger LOGGER = LoggerFactory.getLogger(OPCUAClient.class);

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
	 * OPC-UA Client Service Injection
	 */
	@Reference(bind = "bindOpcUa", unbind = "unbindOpcUa", cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE)
	private volatile OPCUAClientAction m_opcuaClientAction;

	/**
	 * Holds List of {@link OPCUAClientAction}
	 */
	private final List<OPCUAClientAction> m_opcuaClientActions = Lists.newCopyOnWriteArrayList();

	/**
	 * Map to store list of configurations
	 */
	private Map<String, Object> m_properties;

	/* Constructor */
	public OPCUAClient() {
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

		LOGGER.info("Activating OPC-UA Component... Done.");

	}

	/**
	 * Callback to be used while {@link OPCUAClientAction} is registering
	 */
	public synchronized void bindOpcUa(final OPCUAClientAction opcuaClientAction) {
		if (!this.m_opcuaClientActions.contains(opcuaClientAction)) {
			this.m_opcuaClientActions.add(opcuaClientAction);
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
	}

	/**
	 * Callback to be used while {@link OPCUAClientAction} is deregistering
	 */
	public synchronized void unbindOpcUa(final OPCUAClientAction opcuaClientAction) {
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
		properties.keySet().forEach(s -> LOGGER.info("Update - " + s + ": " + properties.get(s)));

		LOGGER.info("Updated OPC-UA Component... Done.");
	}

}