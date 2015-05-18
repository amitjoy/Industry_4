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
package de.tum.in.data.format;

/**
 * Marker Interface. All the cache data format must implement this interface
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public abstract class RealtimeData {

	/**
	 * The bluetooth address of the device which spawned the result
	 */
	private final String m_deviceAddress;

	/**
	 * The value retrieved during communication
	 */
	private final Object m_value;

	/**
	 * The timestamp of the data
	 */
	private final String m_timestamp;

	/* Constructor */
	public RealtimeData(String deviceAddress, Object value, String timeStamp) {
		m_deviceAddress = deviceAddress;
		m_value = value;
		m_timestamp = timeStamp;
	}

	/**
	 * The Getter for bluetooth address
	 */
	public String getDeviceAddress() {
		return m_deviceAddress;
	}

	/**
	 * The getter for the value
	 */
	public Object getValue() {
		return m_value;
	}

	/**
	 * The getter for the timestamp
	 */
	public String getTimestamp() {
		return m_timestamp;
	}
}
