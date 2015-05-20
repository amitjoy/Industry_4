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

import com.google.common.base.MoreObjects;

/**
 * The realtime data format must align with this
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public final class RealtimeData {

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

	/**
	 * Extra Information if needed can be provided
	 */
	private final Object m_extraBody;

	/* Constructor */
	private RealtimeData(String deviceAddress, Object value, String timeStamp,
			Object extraBody) {
		m_deviceAddress = deviceAddress;
		m_value = value;
		m_timestamp = timeStamp;
		m_extraBody = extraBody;
	}

	/**
	 * Builder class to set optional values
	 * 
	 * @author AMIT KUMAR MONDAL
	 *
	 */
	public static class Builder {

		/**
		 * The bluetooth address of the device
		 */
		private String m_deviceAddress;

		/**
		 * The value retrieved
		 */
		private Object m_value;

		/**
		 * The timestamp of the data
		 */
		private String m_timestamp;

		/**
		 * Extra Information
		 */
		private Object m_extraBody;

		/**
		 * Setter for device address
		 */
		public Builder setDeviceAddress(String deviceAddress) {
			m_deviceAddress = deviceAddress;
			return this;
		}

		/**
		 * Setter for realtime data
		 */
		public Builder setValue(Object value) {
			m_value = value;
			return this;
		}

		/**
		 * Setter for timestamp
		 */
		public Builder setTimestamp(String timestamp) {
			m_timestamp = timestamp;
			return this;
		}

		/**
		 * Setter for extra body
		 */
		public Builder setExtraBody(Object extraBody) {
			m_extraBody = extraBody;
			return this;
		}

		/**
		 * Final Building of the object
		 */
		public RealtimeData build() {
			return new RealtimeData(m_deviceAddress, m_value, m_timestamp,
					m_extraBody);
		}

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

	/**
	 * The getter for the extra body
	 */
	public Object getExtraBody() {
		return m_extraBody;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(m_extraBody)
				.addValue(m_value).addValue(m_deviceAddress)
				.addValue(m_timestamp).toString();
	}
}
