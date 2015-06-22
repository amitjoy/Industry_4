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
		 * Extra Information
		 */
		private Object m_extraBody;

		/**
		 * The timestamp of the data
		 */
		private String m_timestamp;

		/**
		 * The value retrieved
		 */
		private Object m_value;

		/**
		 * Final Building of the object
		 */
		public RealtimeData build() {
			return new RealtimeData(this.m_deviceAddress, this.m_value, this.m_timestamp, this.m_extraBody);
		}

		/**
		 * Setter for device address
		 */
		public Builder setDeviceAddress(final String deviceAddress) {
			this.m_deviceAddress = deviceAddress;
			return this;
		}

		/**
		 * Setter for extra body
		 */
		public Builder setExtraBody(final Object extraBody) {
			this.m_extraBody = extraBody;
			return this;
		}

		/**
		 * Setter for timestamp
		 */
		public Builder setTimestamp(final String timestamp) {
			this.m_timestamp = timestamp;
			return this;
		}

		/**
		 * Setter for realtime data
		 */
		public Builder setValue(final Object value) {
			this.m_value = value;
			return this;
		}

	}

	/**
	 * The bluetooth address of the device which spawned the result
	 */
	private final String m_deviceAddress;

	/**
	 * Extra Information if needed can be provided
	 */
	private final Object m_extraBody;

	/**
	 * The timestamp of the data
	 */
	private final String m_timestamp;

	/**
	 * The value retrieved during communication
	 */
	private final Object m_value;

	/* Constructor */
	private RealtimeData(final String deviceAddress, final Object value, final String timeStamp,
			final Object extraBody) {
		this.m_deviceAddress = deviceAddress;
		this.m_value = value;
		this.m_timestamp = timeStamp;
		this.m_extraBody = extraBody;
	}

	/**
	 * The Getter for bluetooth address
	 */
	public String getDeviceAddress() {
		return this.m_deviceAddress;
	}

	/**
	 * The getter for the extra body
	 */
	public Object getExtraBody() {
		return this.m_extraBody;
	}

	/**
	 * The getter for the timestamp
	 */
	public String getTimestamp() {
		return this.m_timestamp;
	}

	/**
	 * The getter for the value
	 */
	public Object getValue() {
		return this.m_value;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).addValue(this.m_extraBody).addValue(this.m_value)
				.addValue(this.m_deviceAddress).addValue(this.m_timestamp).toString();
	}
}
