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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

/**
 * Java class for DeviceList complex type.
 * 
 * @author AMIT KUMAR MONDAL
 */
public class DeviceList {

	protected String deviceFilter;
	protected List<Device> devices;

	/**
	 * Gets the value of the deviceFilter property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDeviceFilter() {
		return deviceFilter;
	}

	/**
	 * Sets the value of the deviceFilter property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDeviceFilter(String value) {
		this.deviceFilter = value;
	}

	/**
	 * Gets the value of the devices property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDevices().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Device }
	 * 
	 * 
	 */
	public List<Device> getDevices() {
		if (devices == null) {
			devices = new ArrayList<Device>();
		}
		return this.devices;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("device-filter", deviceFilter)
				.add("devices", Lists.newArrayList(devices).toString())
				.toString();
	}

}
