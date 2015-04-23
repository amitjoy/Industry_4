package de.tum.in.bluetooth.discovery;

/**
 * System Property Utility classes to determine whether bluetooth application
 * will be used for testing purposes or production purposes
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class Env {

	public static boolean isTestEnvironmentEnabled() {
		return Boolean.getBoolean("bluetooth.test");
	}

	public static void enableTestEnvironment() {
		System.setProperty("bluetooth.test", "true");
	}

	public static void disableTestEnvironment() {
		System.setProperty("bluetooth.test", "false");
	}

}
