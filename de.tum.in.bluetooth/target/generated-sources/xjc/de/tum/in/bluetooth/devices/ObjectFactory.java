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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-833 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.14 at 09:19:47 PM CEST 
//


package de.tum.in.bluetooth.devices;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.tum.in.bluetooth.devices package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Devices_QNAME = new QName("http://org.ow2.chameleon.bluetooth/devices/", "devices");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.tum.in.bluetooth.devices
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeviceList }
     * 
     */
    public DeviceList createDeviceList() {
        return new DeviceList();
    }

    /**
     * Create an instance of {@link Device }
     * 
     */
    public Device createDevice() {
        return new Device();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeviceList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://org.ow2.chameleon.bluetooth/devices/", name = "devices")
    public JAXBElement<DeviceList> createDevices(DeviceList value) {
        return new JAXBElement<DeviceList>(_Devices_QNAME, DeviceList.class, null, value);
    }

}
