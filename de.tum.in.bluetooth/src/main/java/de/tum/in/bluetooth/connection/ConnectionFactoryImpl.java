package de.tum.in.bluetooth.connection;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.InputConnection;

import org.osgi.service.io.ConnectionFactory;

import com.intel.bluetooth.MicroeditionConnector;

/**
 * Bluetooth Serial Port Profile Connection Factory Implementation
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class ConnectionFactoryImpl implements ConnectionFactory {

	/**
	 * Create a new <code>Connection</code> object for a comm specified URI.
	 *
	 * @param name
	 *            The full URI passed to the <code>ConnectorService.open</code>
	 *            method
	 * @param mode
	 *            The mode parameter passed to the
	 *            <code>ConnectorService.open</code> method
	 * @param timeouts
	 *            The timeouts parameter passed to the
	 *            <code>ConnectorService.open</code> method
	 * @return A new <code>javax.microedition.io.Connection</code> object.
	 * @throws IOException
	 *             If a <code>javax.microedition.io.Connection</code> object can
	 *             not not be created.
	 */
	@Override
	public Connection createConnection(String name, int mode, boolean timeouts)
			throws IOException {

		return new WrappedConnection(
				(InputConnection) MicroeditionConnector.open(name, mode,
						timeouts));

	}

}
