package de.tum.in.heater;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudClientListener;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.watchdog.CriticalComponent;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = false)
@Service(value = { Heater.class })
public class Heater implements ConfigurableComponent, CloudClientListener,
		CriticalComponent {

	private static final Logger s_logger = LoggerFactory
			.getLogger(Heater.class);

	// Cloud Application identifier
	private static final String APP_ID = "heater";

	// Application priority for publishing data
	private static final int PRIORITY = 5;

	// Publishing Property Names
	private static final String MODE_PROP_NAME = "mode";
	private static final String MODE_PROP_PROGRAM = "Program";
	private static final String MODE_PROP_MANUAL = "Manual";
	private static final String MODE_PROP_VACATION = "Vacation";

	private static final String PROGRAM_SETPOINT_NAME = "program.setPoint";
	private static final String MANUAL_SETPOINT_NAME = "manual.setPoint";

	private static final String TEMP_INITIAL_PROP_NAME = "temperature.initial";
	private static final String TEMP_INCREMENT_PROP_NAME = "temperature.increment";

	private static final String PUBLISH_RATE_PROP_NAME = "publish.rate";
	private static final String PUBLISH_TOPIC_PROP_NAME = "publish.semanticTopic";
	private static final String PUBLISH_QOS_PROP_NAME = "publish.qos";
	private static final String PUBLISH_RETAIN_PROP_NAME = "publish.retain";

	private String topic;

	@Reference
	private CloudService m_cloudService;

	private CloudClient m_cloudClient;

	private final ScheduledExecutorService m_worker;
	private ScheduledFuture<?> m_handle;

	private float m_temperature;
	private Map<String, Object> m_properties;
	private final Random m_random;

	public Heater() {
		super();
		m_random = new Random();
		m_worker = Executors.newSingleThreadScheduledExecutor();
	}

	public void setCloudService(CloudService cloudService) {
		if (m_cloudService == null)
			m_cloudService = cloudService;
	}

	public void unsetCloudService(CloudService cloudService) {
		if (m_cloudService == cloudService)
			m_cloudService = null;
	}

	@Activate
	protected void activate(Map<String, Object> properties) {
		s_logger.info("Activating Heater...");

		m_properties = properties;
		for (final String s : properties.keySet()) {
			s_logger.info("Activate - " + s + ": " + properties.get(s));
		}

		// get the mqtt client for this application
		try {

			// Acquire a Cloud Application Client for this Application
			s_logger.info("Getting CloudClient for {}...", APP_ID);
			m_cloudClient = m_cloudService.newCloudClient(APP_ID);
			m_cloudClient.addCloudClientListener(this);

			// Don't subscribe because these are handled by the default
			// subscriptions and we don't want to get messages twice
			doUpdate(false);
		} catch (final Exception e) {
			s_logger.error("Error during component activation", e);
			throw new ComponentException(e);
		}
		s_logger.info("Activating Heater... Done.");
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		s_logger.debug("Deactivating Heater...");

		// shutting down the worker and cleaning up the properties
		m_worker.shutdown();

		// Releasing the CloudApplicationClient
		s_logger.info("Releasing CloudApplicationClient for {}...", APP_ID);
		m_cloudClient.release();

		s_logger.debug("Deactivating Heater... Done.");
	}

	public void updated(Map<String, Object> properties) {
		s_logger.info("Updated Heater...");

		// store the properties received
		m_properties = properties;
		for (final String s : properties.keySet()) {
			s_logger.info("Update - " + s + ": " + properties.get(s));
		}

		// try to kick off a new job
		doUpdate(true);
		s_logger.info("Updated Heater... Done.");
	}

	/**
	 * Called after a new set of properties has been configured on the service
	 */
	private void doUpdate(boolean onUpdate) {
		// cancel a current worker handle if one if active
		if (m_handle != null) {
			m_handle.cancel(true);
		}

		if (!m_properties.containsKey(TEMP_INITIAL_PROP_NAME)
				|| !m_properties.containsKey(PUBLISH_RATE_PROP_NAME)) {
			s_logger.info("Update Heater - Ignore as properties do not contain TEMP_INITIAL_PROP_NAME and PUBLISH_RATE_PROP_NAME.");
			return;
		}

		// reset the temperature to the initial value
		if (!onUpdate) {
			m_temperature = (Float) m_properties.get(TEMP_INITIAL_PROP_NAME);
		}

		// schedule a new worker based on the properties of the service
		final int pubrate = (Integer) m_properties.get(PUBLISH_RATE_PROP_NAME);
		m_handle = m_worker.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName(getClass().getSimpleName());
				doPublish();
			}
		}, 0, pubrate, TimeUnit.SECONDS);
	}

	/**
	 * Called at the configured rate to publish the next temperature
	 * measurement.
	 */
	private void doPublish() {
		// fetch the publishing configuration from the publishing properties
		final String topic = (String) m_properties.get(PUBLISH_TOPIC_PROP_NAME);
		final Integer qos = (Integer) m_properties.get(PUBLISH_QOS_PROP_NAME);
		final Boolean retain = (Boolean) m_properties
				.get(PUBLISH_RETAIN_PROP_NAME);
		final String mode = (String) m_properties.get(MODE_PROP_NAME);

		// Increment the simulated temperature value
		float setPoint = 0;
		final float tempIncr = (Float) m_properties
				.get(TEMP_INCREMENT_PROP_NAME);
		if (MODE_PROP_PROGRAM.equals(mode)) {
			setPoint = (Float) m_properties.get(PROGRAM_SETPOINT_NAME);
		} else if (MODE_PROP_MANUAL.equals(mode)) {
			setPoint = (Float) m_properties.get(MANUAL_SETPOINT_NAME);
		} else if (MODE_PROP_VACATION.equals(mode)) {
			setPoint = 6.0F;
		}
		if (m_temperature + tempIncr < setPoint) {
			m_temperature += tempIncr;
		} else {
			m_temperature -= 4 * tempIncr;
		}

		// Allocate a new payload
		final KuraPayload payload = new KuraPayload();

		// Timestamp the message
		payload.setTimestamp(new Date());

		// Add the temperature as a metric to the payload
		payload.addMetric("temperatureInternal", m_temperature);
		payload.addMetric("temperatureExternal", 5.0F);
		payload.addMetric("temperatureExhaust", 30.0F);

		final int code = m_random.nextInt();
		if ((m_random.nextInt() % 5) == 0) {
			payload.addMetric("errorCode", code);
		} else {
			payload.addMetric("errorCode", 0);
		}

		// Publish the message
		try {
			m_cloudClient.controlPublish(topic, payload, qos, retain, PRIORITY);
			s_logger.info("Published to {} message: {}", topic, payload);
		} catch (final Exception e) {
			s_logger.error("Cannot publish topic: " + topic, e);
		}
	}

	// ----------------------------------------------------------------
	//
	// Cloud Application Callback Methods
	//
	// ----------------------------------------------------------------

	@Override
	public void onControlMessageArrived(String deviceId, String appTopic,
			KuraPayload msg, int qos, boolean retain) {
	}

	@Override
	public void onMessageArrived(String deviceId, String appTopic,
			KuraPayload msg, int qos, boolean retain) {
	}

	@Override
	public void onConnectionLost() {
	}

	@Override
	public void onConnectionEstablished() {
	}

	@Override
	public void onMessageConfirmed(int paramInt, String paramString) {
	}

	@Override
	public void onMessagePublished(int paramInt, String paramString) {
	}

	// ----------------------------------------------------------------
	//
	// WatchDog Callback Methods
	//
	// ----------------------------------------------------------------

	@Override
	public String getCriticalComponentName() {
		return APP_ID;
	}

	@Override
	public int getCriticalComponentTimeout() {
		return 10;
	}
}