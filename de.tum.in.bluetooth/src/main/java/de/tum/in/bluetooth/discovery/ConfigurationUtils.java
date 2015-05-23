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
package de.tum.in.bluetooth.discovery;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class ConfigurationUtils {

	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(Class<T> docClass, InputStream inputStream)
			throws JAXBException {
		final String packageName = docClass.getPackage().getName();
		final JAXBContext jc = JAXBContext.newInstance(packageName,
				BluetoothDeviceDiscovery.class.getClassLoader());
		final Unmarshaller u = jc.createUnmarshaller();
		final JAXBElement<T> doc = (JAXBElement<T>) u.unmarshal(inputStream);
		return doc.getValue();
	}

}
