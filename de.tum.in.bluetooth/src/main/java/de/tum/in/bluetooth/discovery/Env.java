package de.tum.in.bluetooth.discovery;

/**
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
