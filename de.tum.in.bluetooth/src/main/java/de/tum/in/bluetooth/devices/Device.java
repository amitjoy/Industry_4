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
package de.tum.in.bluetooth.devices;

import java.math.BigInteger;

import com.google.common.base.MoreObjects;

public class Device {

	protected String id;
	protected String pin;
	protected String username;
	protected String password;
	protected String realm;
	protected boolean retry;
	protected BigInteger maxRetry;

	/**
	 * Gets the value of the id property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the id property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setId(String value) {
		this.id = value;
	}

	/**
	 * Gets the value of the pin property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPin() {
		return pin;
	}

	/**
	 * Sets the value of the pin property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPin(String value) {
		this.pin = value;
	}

	/**
	 * Gets the value of the username property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the value of the username property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setUsername(String value) {
		this.username = value;
	}

	/**
	 * Gets the value of the password property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the value of the password property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setPassword(String value) {
		this.password = value;
	}

	/**
	 * Gets the value of the realm property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * Sets the value of the realm property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setRealm(String value) {
		this.realm = value;
	}

	/**
	 * Gets the value of the retry property.
	 * 
	 */
	public boolean isRetry() {
		return retry;
	}

	/**
	 * Sets the value of the retry property.
	 * 
	 */
	public void setRetry(boolean value) {
		this.retry = value;
	}

	/**
	 * Gets the value of the maxRetry property.
	 * 
	 * @return possible object is {@link BigInteger }
	 * 
	 */
	public BigInteger getMaxRetry() {
		return maxRetry;
	}

	/**
	 * Sets the value of the maxRetry property.
	 * 
	 * @param value
	 *            allowed object is {@link BigInteger }
	 * 
	 */
	public void setMaxRetry(BigInteger value) {
		this.maxRetry = value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", id)
				.add("username", username).add("password", password)
				.add("pin", pin).add("retry", retry).add("max-retry", maxRetry)
				.toString();
	}

}
