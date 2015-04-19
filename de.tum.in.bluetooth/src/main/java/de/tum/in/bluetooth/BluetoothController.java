package de.tum.in.bluetooth;

public interface BluetoothController {

	final class BluetoothException extends Exception {

		private static final String MESSAGE = "Bluetooth Initiation Failed";

		public BluetoothException() {
			super(MESSAGE);
		}
	}

	/**
	 * Initializes the Bluetooth Discovery
	 */
	public void start();

	/**
	 * Stops the Bluetooth Discovery
	 */
	public void stop();

	/**
	 * Used to get the current bluetooth device stack
	 * 
	 * @return the name for the bluetooth stack
	 */
	public String getBluetoothStack();

	/**
	 * Used to check whether the local bluetooth device is turned on or not
	 * 
	 * @return true if on else off
	 */
	public boolean isBluetoothDeviceTurnedOn();

	/**
	 * Used to check whether the stack used for bluetooth discovery is supported
	 * or not
	 * 
	 * @return
	 */
	public boolean isBluetoothStackSupported();

}
