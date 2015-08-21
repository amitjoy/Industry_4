/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tum.in.activity.log;

import java.io.File;
import java.io.IOException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.eclipse.kura.cloud.CloudService;
import org.eclipse.kura.system.SystemService;
import org.osgi.service.component.ComponentContext;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

/**
 * Component publishing all the activity logs stored in the Pi to Splunk (every
 * 5 Hours)
 *
 * @author AMIT KUMAR MONDAL
 */
@Component(name = "de.tum.in.activity.log.publisher")
public class ActivityLogPublisher implements Job {

	/**
	 * Application Identifier
	 */
	private static final String APP_ID = "ACTIVITY-LOG-PUBLISHER";

	/**
	 * Configurable Property to set Activity Events Topic Namespace
	 */
	private static final String EVENT_LOG_TOPIC = "event.log.topic";

	/**
	 * Cronjob Group Id
	 */
	private static final String GROUP_ID = "tum";

	/**
	 * Cronjob Id
	 */
	private static final String JOB_ID = "ActivityLogPublisher";

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ActivityLogPublisher.class);

	/**
	 * Cronjob Trigger Id
	 */
	private static final String TRIGGER_ID = "ActivityLogPublisherTrigger";

	/**
	 * Kura Cloud Service Injection
	 */
	@Reference(bind = "bindCloudService", unbind = "unbindCloudService")
	private volatile CloudService m_cloudService;

	/**
	 * System Service Dependency
	 */
	@Reference(bind = "bindSystemService", unbind = "unbindSystemService")
	private volatile SystemService m_systemService;

	/**
	 * Default Constructor Required for DS.
	 */
	public ActivityLogPublisher() {
	}

	/**
	 * Callback during registration of this DS Service Component
	 *
	 * @param context
	 *            The injected reference for this DS Service Component
	 */
	@Activate
	protected synchronized void activate(final ComponentContext context) {
		LOGGER.info("Activating Bluetooth Service Discovery....");
		this.initJob();
		LOGGER.info("Activating Bluetooth Service Discovery....Done");
	}

	/**
	 * Kura Cloud Service Binding Callback
	 */
	public synchronized void bindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == null) {
			this.m_cloudService = cloudService;
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is registering
	 */
	public synchronized void bindSystemService(final SystemService systemService) {
		if (this.m_systemService == null) {
			this.m_systemService = systemService;
		}
	}

	/**
	 * Callback while this component is getting deregistered
	 *
	 * @param properties
	 *            the service configuration properties
	 */
	@Deactivate
	protected synchronized void deactivate(final ComponentContext context) {
		LOGGER.info("Deactivating Activity Log Publisher....");

		LOGGER.info("Deactivating Activity Log Publisher... Done.");
	}

	/**
	 * Publishes the logs to Splunk
	 */
	private void doPublish() {
		LOGGER.debug("Publishing Activity Log Started.....");
		try {
			final String activityLogTopic = this.m_systemService.getProperties().getProperty(EVENT_LOG_TOPIC);
			final File tumLogFile = new File(IActivityLogService.LOCATION_TUM_LOG);
			Files.newReader(tumLogFile, Charsets.UTF_8).lines().forEach(line -> {
				try {
					this.m_cloudService.newCloudClient(APP_ID).controlPublish("splunk", activityLogTopic,
							line.getBytes(), 0, false, 5);
				} catch (final Exception e) {
					LOGGER.error(Throwables.getStackTraceAsString(e));
				}
			});
			// After publishing clear the log file
			Files.write("", tumLogFile, Charsets.UTF_8);
		} catch (final IOException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
	}

	/** {@inheritDoc}} */
	@Override
	public void execute(final JobExecutionContext executionContext) throws JobExecutionException {
		this.doPublish();
	}

	/**
	 * Initialize the cronjob configuration
	 */
	private void initJob() {
		final JobDetail job = JobBuilder.newJob(this.getClass()).withIdentity(JOB_ID, GROUP_ID).build();

		final Trigger trigger = TriggerBuilder.newTrigger().withIdentity(TRIGGER_ID, GROUP_ID)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(5).repeatForever()).build();

		Scheduler scheduler = null;
		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			scheduler.scheduleJob(job, trigger);
		} catch (final SchedulerException e) {
			LOGGER.error(Throwables.getStackTraceAsString(e));
		}
	}

	/**
	 * Cloud Service Callback while deregistering
	 */
	public synchronized void unbindCloudService(final CloudService cloudService) {
		if (this.m_cloudService == cloudService) {
			this.m_cloudService = null;
		}
	}

	/**
	 * Callback to be used while {@link SystemService} is deregistering
	 */
	public synchronized void unbindSystemService(final SystemService systemService) {
		if (this.m_systemService == systemService) {
			this.m_systemService = null;
		}
	}

}
