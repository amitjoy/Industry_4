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
package de.tum.in.opcua.client.util;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Loads the provided keystore
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class KeyStoreLoader {
	private static final String CLIENT_ALIAS = "client-ai";
	private static final char[] PASSWORD = "password".toCharArray();
	private static final String SERVER_ALIAS = "server-ai";

	private X509Certificate clientCertificate;
	private KeyPair clientKeyPair;

	public X509Certificate getClientCertificate() {
		return this.clientCertificate;
	}

	public KeyPair getClientKeyPair() {
		return this.clientKeyPair;
	}

	public KeyStoreLoader load() throws Exception {
		final KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(this.getClass().getClassLoader().getResourceAsStream("example-certs.pfx"), PASSWORD);

		final Key clientPrivateKey = keyStore.getKey(CLIENT_ALIAS, PASSWORD);
		if (clientPrivateKey instanceof PrivateKey) {
			this.clientCertificate = (X509Certificate) keyStore.getCertificate(CLIENT_ALIAS);
			final PublicKey clientPublicKey = this.clientCertificate.getPublicKey();
			this.clientKeyPair = new KeyPair(clientPublicKey, (PrivateKey) clientPrivateKey);
		}

		return this;
	}
}
