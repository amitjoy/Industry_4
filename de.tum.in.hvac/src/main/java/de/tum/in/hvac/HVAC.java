package de.tum.in.hvac;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.CloudClient;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurableComponent;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.core.util.NetUtil;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = false)
@Service(value = { HVAC.class })
public class HVAC extends Cloudlet implements ConfigurableComponent {

	private static final Logger s_logger = LoggerFactory.getLogger(HVAC.class);

	// Cloud Application identifier
	private static final String APP_ID = "HVAC-V1";

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

	@Reference
	private volatile CloudService m_cloudService;

	@Reference
	private volatile SystemService m_systemService;

	@Reference
	private volatile ConfigurationService m_configurationService;

	private volatile CloudClient m_cloudClient;

	private float m_temperature;
	private Map<String, Object> m_properties;

	public HVAC() {
		super(APP_ID);
	}

	@Override
	public void setCloudService(CloudService cloudService) {
		if (m_cloudService == null) {
			super.setCloudService(m_cloudService = cloudService);
		}
	}

	@Override
	public void unsetCloudService(CloudService cloudService) {
		if (m_cloudService == cloudService)
			super.setCloudService(m_cloudService = null);
	}

	public void setConfigurationService(
			ConfigurationService configurationService) {
		if (m_cloudService == null) {
			m_configurationService = configurationService;
		}
	}

	public void unsetConfigurationService(
			ConfigurationService configurationService) {
		if (m_configurationService == configurationService)
			m_configurationService = null;
	}

	public void setSystemService(SystemService systemService) {
		if (m_systemService == null)
			m_systemService = systemService;
	}

	public void unsetSystemService(SystemService systemService) {
		if (m_systemService == systemService)
			m_systemService = null;
	}

	@Activate
	protected void activate(ComponentContext componentContext,
			Map<String, Object> properties) {
		s_logger.info("Activating HVAC Component...");

		m_properties = properties;
		for (final String s : properties.keySet()) {
			s_logger.info("Activate - " + s + ": " + properties.get(s));
		}

		super.setCloudService(m_cloudService);
		super.activate(componentContext);
		s_logger.info("Activating HVAC... Done.");

		doPublish();
	}

	private void doPublish() {
		final String topic = (String) m_properties.get(PUBLISH_TOPIC_PROP_NAME);
		final String payload = "AMIT KUMAR MONDAL";
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>"
				+ m_systemService.getDeviceName() + ">>>>>>>>>>>>>>"
				+ NetUtil.getPrimaryMacAddress());
		try {
			getCloudApplicationClient().controlPublish("DEVICE", topic,
					payload.getBytes(), DFLT_PUB_QOS, DFLT_RETAIN,
					DFLT_PRIORITY);
		} catch (final KuraException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Deactivate
	protected void deactivate(ComponentContext context) {
		s_logger.debug("Deactivating HVAC...");
		s_logger.info("Releasing CloudApplicationClient for {}...", APP_ID);

		super.deactivate(context);

		s_logger.debug("Deactivating HVAC... Done.");
	}

	public void updated(Map<String, Object> properties) {
		s_logger.info("Updated HVAC...");

		m_properties = properties;
		for (final String s : properties.keySet()) {
			s_logger.info("Update - " + s + ": " + properties.get(s));
		}

		s_logger.info("Updated HVAC... Done.");
	}

	// ----------------------------------------------------------------
	//
	// Remote Management Callback Methods
	//
	// ----------------------------------------------------------------

	@Override
	protected void doGet(CloudletTopic reqTopic, KuraRequestPayload reqPayload,
			KuraResponsePayload respPayload) throws KuraException {
		s_logger.info("HVAC GET handler" + m_configurationService);
		final ComponentConfiguration configuration = m_configurationService
				.getComponentConfiguration("de.tum.in.hvac.HVAC");
		final Iterator<?> entries = configuration.getConfigurationProperties()
				.entrySet().iterator();
		while (entries.hasNext()) {
			final Entry thisEntry = (Entry) entries.next();
			final Object key = thisEntry.getKey();
			final Object value = thisEntry.getValue();
			respPayload.addMetric((String) key, value);
		}
		System.out.println(Arrays.asList(reqTopic.getResources()));
		respPayload.addMetric(MODE_PROP_NAME, m_properties.get(MODE_PROP_NAME));
		respPayload.setResponseCode(KuraResponsePayload.RESPONSE_CODE_OK);
	}

	@Override
	protected void doExec(CloudletTopic reqTopic,
			KuraRequestPayload reqPayload, KuraResponsePayload respPayload)
			throws KuraException {
		s_logger.info("HVAC EXEC handler");
	}
}