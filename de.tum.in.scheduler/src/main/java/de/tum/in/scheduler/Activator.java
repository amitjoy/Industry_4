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
package de.tum.in.scheduler;

import java.util.Properties;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;

public class Activator extends DependencyActivatorBase {

	@Override
	public void init(BundleContext context, DependencyManager manager)
			throws Exception {
		final OSGiJobFactory jobFactory = new OSGiJobFactory();

		manager.add(createComponent()
				.setInterface(Scheduler.class.getName(), null)
				.setImplementation(getSchedulerInstance(jobFactory))
				.add(createServiceDependency().setService(Job.class)
						.setAutoConfig(false)
						.setCallbacks(jobFactory, "jobAdded", "jobRemoved"))
				.setCallbacks("osgiInit", "osgiStart", "osgiStop",
						"osgiDestroy"));

		manager.add(createComponent()
				.setImplementation(WhiteboardJobServiceImpl.class)
				.add(createServiceDependency().setService(Scheduler.class)
						.setRequired(true))
				.add(createServiceDependency().setService(LogService.class)
						.setRequired(false))
				.add(createServiceDependency().setService(Job.class)
						.setRequired(false)
						.setCallbacks("jobAdded", "jobRemoved")));
	}

	@Override
	public void destroy(BundleContext context, DependencyManager manager)
			throws Exception {
	}

	public Scheduler getSchedulerInstance(JobFactory jobFactory)
			throws SchedulerException {
		final Properties properties = new Properties();

		properties.put("org.quartz.scheduler.instanceName",
				"DefaultQuartzScheduler");
		properties.put("org.quartz.scheduler.rmi.export", "false");
		properties.put("org.quartz.scheduler.rmi.proxy", "false");

		properties.put("org.quartz.threadPool.class",
				"org.quartz.simpl.SimpleThreadPool");
		properties.put("org.quartz.threadPool.threadCount", "10");
		properties.put("org.quartz.threadPool.threadPriority", "5");
		properties
				.put("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread",
						"true");

		properties.put("org.quartz.jobStore.misfireThreshold", "60000");
		properties.put("org.quartz.jobStore.class",
				"org.quartz.simpl.RAMJobStore");

		properties.put("org.quartz.scheduler.skipUpdateCheck", "true");

		final Scheduler scheduler = new StdSchedulerFactory(properties)
				.getScheduler();
		scheduler.setJobFactory(jobFactory);
		return new WrappedScheduler(scheduler);
	}

}
