package de.tum.in.python.bluetooth.milling.machine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.kura.KuraErrorCode;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.core.util.ProcessUtil;
import org.eclipse.kura.core.util.SafeProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * Used to execute python commands for Milling Machine Communication
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class CommandUtil {

	/**
	 * Represents the python POSIX command utility
	 */
	private static final String CMD_PYTHON = "python";

	/**
	 * Represents the python command line argument
	 */
	private static final String CMD_PYTHON_ARG = "gw";

	/**
	 * Home Folder Location
	 */
	private static final String HOME_LOCATION = "/home/pi/TUM/";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtil.class);

	/**
	 * Represents location for all the ZWave Related Commands
	 */
	private static final String PYTHON_CODE_LOCATION = HOME_LOCATION + "bt.py";

	/**
	 * Starts the communication with the provided bluetooth mac address milling
	 * machine
	 */
	public static void initCommunication(final String macAddress) {
		LOGGER.info("Starting Python Bluetooth Milling Machine Communication...");

		SafeProcess process = null;
		BufferedReader br = null;
		final String[] command = { CMD_PYTHON, PYTHON_CODE_LOCATION, CMD_PYTHON_ARG, macAddress };

		try {
			process = ProcessUtil.exec(command);
			br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.contains("command not found")) {
					LOGGER.error("Resetting Command Not Found");
					throw new KuraException(KuraErrorCode.OPERATION_NOT_SUPPORTED);
				}
			}

			LOGGER.info("Starting Python Bluetooth Milling Machine Communication...Done");
		} catch (final Exception e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		} finally {
			try {
				LOGGER.debug("Closing Buffered Reader and destroying Process", process);
				br.close();
				process.destroy();
			} catch (final IOException e) {
				LOGGER.error("Error closing read buffer", Throwables.getStackTraceAsString(e));
			}
		}
	}

}