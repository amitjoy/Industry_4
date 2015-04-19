package de.tum.in.bluetooth.discovery;

import java.io.IOException;

import javax.bluetooth.DataElement;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class ServiceRecordStub implements ServiceRecord {

	RemoteDevice remote;

	String name;

	public ServiceRecordStub(RemoteDevice device, String name) {
		remote = device;
		this.name = name;
	}

	@Override
	public int[] getAttributeIDs() {
		return ServiceDiscoveryAgent.attrIDs;
	}

	@Override
	public DataElement getAttributeValue(int arg0) {
		return new DataElement(DataElement.STRING, name);
	}

	@Override
	public String getConnectionURL(int arg0, boolean arg1) {
		return "obex://stub-" + remote.getBluetoothAddress() + "/" + name;
	}

	@Override
	public RemoteDevice getHostDevice() {
		return remote;
	}

	@Override
	public boolean populateRecord(int[] arg0) throws IOException {
		return false;
	}

	@Override
	public boolean setAttributeValue(int arg0, DataElement arg1) {
		return false;
	}

	@Override
	public void setDeviceServiceClasses(int arg0) {

	}

}
