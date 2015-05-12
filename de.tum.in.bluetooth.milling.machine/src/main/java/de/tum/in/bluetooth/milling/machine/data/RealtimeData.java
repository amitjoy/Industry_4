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
package de.tum.in.bluetooth.milling.machine.data;

/**
 * Used to wrap realtime data result in a predefined format
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class RealtimeData {

	/**
	 * The bluetooth address of the device which spawned the result
	 */
	private final String m_bluetoothAddress;

	/**
	 * The value retrieved during communication
	 */
	private final String m_value;

	/**
	 * The timestamp of the data
	 */
	private final Long m_timestamp;

	/* Constructor */
	public RealtimeData(String bluetoothAddress, String value) {
		m_bluetoothAddress = bluetoothAddress;
		m_value = value;
		m_timestamp = System.currentTimeMillis();
	}

	/**
	 * The Getter for bluetooth address
	 */
	public String getBluetoothAddress() {
		return m_bluetoothAddress;
	}

	/**
	 * The getter for the value
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * The getter for the timestamp
	 */
	public Long getTimestamp() {
		return m_timestamp;
	}

}
