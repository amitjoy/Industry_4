package de.tum.in.bluetooth;

public interface BluetoothController {

	final class BluetoothException extends Exception {

		private static final String MESSAGE = "Bluetooth Initiation Failed";

		public BluetoothException() {
			super(MESSAGE);
		}
	}

	public void start();

	public void stop();

	public String getBluetoothStack();

	public boolean isBluetoothDeviceTurnedOn();

	public boolean isBluetoothStackSupported();

}
