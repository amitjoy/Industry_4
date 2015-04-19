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

	public static <T> T unmarshal(Class<T> docClass, InputStream inputStream)
			throws JAXBException {
		final String packageName = docClass.getPackage().getName();
		final JAXBContext jc = JAXBContext.newInstance(packageName,
				BluetoothDeviceDiscovery.class.getClassLoader());
		final Unmarshaller u = jc.createUnmarshaller();
		@SuppressWarnings("unchecked")
		final JAXBElement<T> doc = (JAXBElement<T>) u.unmarshal(inputStream);
		return doc.getValue();
	}

}
