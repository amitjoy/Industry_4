package de.tum.in.bluetooth;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.io.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import de.tum.in.bluetooth.connection.ConnectionFactoryImpl;

/**
 * Activator used to register Bluetooth Serial Port Profile Connection factory
 * Implementation
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class Activator implements BundleActivator {

	// Schema provided for connections
	private static final String SCHEMA = "btspp";

	// The ConnectionFactory Service implementation
	private ConnectionFactory m_connectionFactory;

	/**
	 * slf4j Logger
	 */
	private final static Logger m_logger = LoggerFactory
			.getLogger(Activator.class);

	/**
	 * slf4j Marker to keep track of bundle
	 */
	private static final Marker m_bundleMarker = createBundleMarker();

	@Override
	public void start(BundleContext context) throws Exception {
		final Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
		properties.put(ConnectionFactory.IO_SCHEME, new String[] { SCHEMA });
		m_connectionFactory = new ConnectionFactoryImpl();
		context.registerService(ConnectionFactory.class.getName(),
				m_connectionFactory, properties);
		m_logger.debug(m_bundleMarker, "Started Bundle");
	}

	/**
	 * slf4j Marker to keep track of bundle
	 * 
	 */
	private static Marker createBundleMarker() {
		final Marker bundleMarker = MarkerFactory.getMarker(Activator.class
				.getName());
		bundleMarker.add(MarkerFactory.getMarker("IS_BUNDLE"));
		return bundleMarker;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		m_connectionFactory = null;
		m_logger.debug(m_bundleMarker, "Stopped Bundle");
	}

}
