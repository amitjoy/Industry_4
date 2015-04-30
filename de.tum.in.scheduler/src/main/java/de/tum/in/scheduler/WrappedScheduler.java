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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.Calendar;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.UnableToInterruptJobException;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;

public class WrappedScheduler implements Scheduler {

	private final Scheduler m_delegate;

	public WrappedScheduler(Scheduler delegate) {
		m_delegate = delegate;
	}

	@Override
	public void addCalendar(String arg0, Calendar arg1, boolean arg2,
			boolean arg3) throws SchedulerException {
		m_delegate.addCalendar(arg0, arg1, arg2, arg3);
	}

	@Override
	public void addJob(JobDetail arg0, boolean arg1) throws SchedulerException {
		m_delegate.addJob(arg0, arg1);
	}

	@Override
	public boolean checkExists(JobKey arg0) throws SchedulerException {
		return m_delegate.checkExists(arg0);
	}

	@Override
	public boolean checkExists(TriggerKey arg0) throws SchedulerException {
		return m_delegate.checkExists(arg0);
	}

	@Override
	public void clear() throws SchedulerException {
		m_delegate.clear();
	}

	@Override
	public boolean deleteCalendar(String arg0) throws SchedulerException {
		return m_delegate.deleteCalendar(arg0);
	}

	@Override
	public boolean deleteJob(JobKey arg0) throws SchedulerException {
		return m_delegate.deleteJob(arg0);
	}

	@Override
	public boolean deleteJobs(List<JobKey> arg0) throws SchedulerException {
		return m_delegate.deleteJobs(arg0);
	}

	@Override
	public Calendar getCalendar(String arg0) throws SchedulerException {
		return m_delegate.getCalendar(arg0);
	}

	@Override
	public List<String> getCalendarNames() throws SchedulerException {
		return m_delegate.getCalendarNames();
	}

	@Override
	public SchedulerContext getContext() throws SchedulerException {
		return m_delegate.getContext();
	}

	@Override
	public List<JobExecutionContext> getCurrentlyExecutingJobs()
			throws SchedulerException {
		return m_delegate.getCurrentlyExecutingJobs();
	}

	@Override
	public JobDetail getJobDetail(JobKey arg0) throws SchedulerException {
		return m_delegate.getJobDetail(arg0);
	}

	@Override
	public List<String> getJobGroupNames() throws SchedulerException {
		return m_delegate.getJobGroupNames();
	}

	@Override
	public Set<JobKey> getJobKeys(GroupMatcher<JobKey> arg0)
			throws SchedulerException {
		return m_delegate.getJobKeys(arg0);
	}

	@Override
	public ListenerManager getListenerManager() throws SchedulerException {
		return m_delegate.getListenerManager();
	}

	@Override
	public SchedulerMetaData getMetaData() throws SchedulerException {
		return m_delegate.getMetaData();
	}

	@Override
	public Set<String> getPausedTriggerGroups() throws SchedulerException {
		return m_delegate.getPausedTriggerGroups();
	}

	@Override
	public String getSchedulerInstanceId() throws SchedulerException {
		return m_delegate.getSchedulerInstanceId();
	}

	@Override
	public String getSchedulerName() throws SchedulerException {
		return m_delegate.getSchedulerName();
	}

	@Override
	public Trigger getTrigger(TriggerKey arg0) throws SchedulerException {
		return m_delegate.getTrigger(arg0);
	}

	@Override
	public List<String> getTriggerGroupNames() throws SchedulerException {
		return m_delegate.getTriggerGroupNames();
	}

	@Override
	public Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> arg0)
			throws SchedulerException {
		return m_delegate.getTriggerKeys(arg0);
	}

	@Override
	public TriggerState getTriggerState(TriggerKey arg0)
			throws SchedulerException {
		return m_delegate.getTriggerState(arg0);
	}

	@Override
	public List<? extends Trigger> getTriggersOfJob(JobKey arg0)
			throws SchedulerException {
		return m_delegate.getTriggersOfJob(arg0);
	}

	@Override
	public boolean interrupt(JobKey arg0) throws UnableToInterruptJobException {
		return m_delegate.interrupt(arg0);
	}

	@Override
	public boolean interrupt(String arg0) throws UnableToInterruptJobException {
		return m_delegate.interrupt(arg0);
	}

	@Override
	public boolean isInStandbyMode() throws SchedulerException {
		return m_delegate.isInStandbyMode();
	}

	@Override
	public boolean isShutdown() throws SchedulerException {
		return m_delegate.isShutdown();
	}

	@Override
	public boolean isStarted() throws SchedulerException {
		return m_delegate.isStarted();
	}

	@Override
	public void pauseAll() throws SchedulerException {
		m_delegate.pauseAll();
	}

	@Override
	public void pauseJob(JobKey arg0) throws SchedulerException {
		m_delegate.pauseJob(arg0);
	}

	@Override
	public void pauseJobs(GroupMatcher<JobKey> arg0) throws SchedulerException {
		m_delegate.pauseJobs(arg0);
	}

	@Override
	public void pauseTrigger(TriggerKey arg0) throws SchedulerException {
		m_delegate.pauseTrigger(arg0);
	}

	@Override
	public void pauseTriggers(GroupMatcher<TriggerKey> arg0)
			throws SchedulerException {
		m_delegate.pauseTriggers(arg0);
	}

	@Override
	public Date rescheduleJob(TriggerKey arg0, Trigger arg1)
			throws SchedulerException {
		return m_delegate.rescheduleJob(arg0, arg1);
	}

	@Override
	public void resumeAll() throws SchedulerException {
		m_delegate.resumeAll();
	}

	@Override
	public void resumeJob(JobKey arg0) throws SchedulerException {
		m_delegate.resumeJob(arg0);
	}

	@Override
	public void resumeJobs(GroupMatcher<JobKey> arg0) throws SchedulerException {
		m_delegate.resumeJobs(arg0);
	}

	@Override
	public void resumeTrigger(TriggerKey arg0) throws SchedulerException {
		m_delegate.resumeTrigger(arg0);
	}

	@Override
	public void resumeTriggers(GroupMatcher<TriggerKey> arg0)
			throws SchedulerException {
		m_delegate.resumeTriggers(arg0);
	}

	@Override
	public Date scheduleJob(JobDetail arg0, Trigger arg1)
			throws SchedulerException {
		return m_delegate.scheduleJob(arg0, arg1);
	}

	@Override
	public Date scheduleJob(Trigger arg0) throws SchedulerException {
		return m_delegate.scheduleJob(arg0);
	}

	@Override
	public void setJobFactory(JobFactory arg0) throws SchedulerException {
		m_delegate.setJobFactory(arg0);
	}

	@Override
	public void triggerJob(JobKey arg0, JobDataMap arg1)
			throws SchedulerException {
		m_delegate.triggerJob(arg0, arg1);
	}

	@Override
	public void triggerJob(JobKey arg0) throws SchedulerException {
		m_delegate.triggerJob(arg0);
	}

	@Override
	public boolean unscheduleJob(TriggerKey arg0) throws SchedulerException {
		return m_delegate.unscheduleJob(arg0);
	}

	@Override
	public boolean unscheduleJobs(List<TriggerKey> arg0)
			throws SchedulerException {
		return m_delegate.unscheduleJobs(arg0);
	}

	@Override
	public void shutdown() throws SchedulerException {
		throw new SchedulerException("You're not allowed to invoke shutdown()");
	}

	@Override
	public void shutdown(boolean arg0) throws SchedulerException {
		throw new SchedulerException("You're not allowed to invoke shutdown()");
	}

	@Override
	public void standby() throws SchedulerException {
		throw new SchedulerException("You're not allowed to invoke standby()");
	}

	@Override
	public void start() throws SchedulerException {
		throw new SchedulerException("You're not allowed to invoke start()");
	}

	@Override
	public void startDelayed(int arg0) throws SchedulerException {
		throw new SchedulerException(
				"You're not allowed to invoke startDelayed()");
	}

	void osgiStart() throws SchedulerException {
		m_delegate.start();
	}

	void osgiStop() throws SchedulerException {
		m_delegate.standby();
	}

	void osgiDestroy() throws SchedulerException {
		m_delegate.shutdown();
	}

	@Override
	public void addJob(JobDetail arg0, boolean arg1, boolean arg2)
			throws SchedulerException {
		m_delegate.addJob(arg0, arg1, arg2);

	}

	@Override
	public void scheduleJob(JobDetail arg0, Set<? extends Trigger> arg1,
			boolean arg2) throws SchedulerException {
		m_delegate.scheduleJob(arg0, arg1, arg2);

	}

	@Override
	public void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> arg0,
			boolean arg1) throws SchedulerException {
		m_delegate.scheduleJobs(arg0, arg1);

	}

}
