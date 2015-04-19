package de.tum.in.scheduler;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import de.tum.in.scheduler.annotations.Cron;
import de.tum.in.scheduler.annotations.Description;
import de.tum.in.scheduler.annotations.ModifiedByCalendar;
import de.tum.in.scheduler.annotations.Priority;
import de.tum.in.scheduler.annotations.RepeatCount;
import de.tum.in.scheduler.annotations.RepeatForever;
import de.tum.in.scheduler.annotations.RepeatInterval;
import de.tum.in.scheduler.annotations.RequestRecovery;
import de.tum.in.scheduler.annotations.timeinterval.DaysOfTheWeek;
import de.tum.in.scheduler.annotations.timeinterval.EndingDailyAt;
import de.tum.in.scheduler.annotations.timeinterval.EveryDay;
import de.tum.in.scheduler.annotations.timeinterval.Interval;
import de.tum.in.scheduler.annotations.timeinterval.MondayThroughFriday;
import de.tum.in.scheduler.annotations.timeinterval.SaturdayAndSunday;
import de.tum.in.scheduler.annotations.timeinterval.StartingDailyAt;

public class WhiteboardJobServiceImpl {

	private volatile Scheduler m_scheduler;

	private volatile LogService m_logService;

	private final Map<Job, JobDetail> m_jobs = new HashMap<>();

	public void jobAdded(ServiceReference<?> ref, Job job) {
		final Class<? extends Job> jobClass = job.getClass();

		final JobBuilder jobBuilder = JobBuilder.newJob();
		final TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder
				.newTrigger();

		if (buildJobAndTrigger(jobBuilder, triggerBuilder, jobClass)) {
			final String group = "bundle" + ref.getBundle().getBundleId();
			final String name = jobClass.getName();
			jobBuilder.withIdentity(name, group);
			jobBuilder.ofType(jobClass);

			final JobDetail jobDetail = jobBuilder.build();
			final Trigger trigger = triggerBuilder.build();
			try {
				synchronized (m_jobs) {
					m_scheduler.scheduleJob(jobDetail, trigger);
					m_jobs.put(job, jobDetail);
				}
			} catch (final SchedulerException e) {
				m_logService.log(LogService.LOG_WARNING,
						"Failed to schedule job", e);
			}
		}
	}

	public void jobRemoved(Job job) {
		synchronized (m_jobs) {
			final JobDetail jobDetail = m_jobs.get(job);
			try {
				m_scheduler.deleteJob(jobDetail.getKey());
				m_jobs.remove(job);
			} catch (final SchedulerException e) {
				m_logService.log(LogService.LOG_WARNING,
						"Failed to delete job", e);
			}
		}
	}

	public void stop() {
		synchronized (m_jobs) {
			for (final JobDetail job : m_jobs.values()) {
				try {
					m_scheduler.deleteJob(job.getKey());
				} catch (final SchedulerException e) {
					m_logService.log(LogService.LOG_WARNING,
							"Failed to delete job", e);
				}
			}
		}
	}

	private boolean buildJobAndTrigger(JobBuilder jobBuilder,
			TriggerBuilder<Trigger> triggerBuilder, AnnotatedElement element) {
		final Description description = element
				.getAnnotation(Description.class);
		if (description != null) {
			jobBuilder.withDescription(description.value());
		}

		final RequestRecovery requestRecovery = element
				.getAnnotation(RequestRecovery.class);
		if (requestRecovery != null) {
			jobBuilder.requestRecovery(requestRecovery.value());
		}

		final ModifiedByCalendar modifiedByCalendar = element
				.getAnnotation(ModifiedByCalendar.class);
		if (modifiedByCalendar != null) {
			triggerBuilder.modifiedByCalendar(modifiedByCalendar.value());
		}

		final Priority priority = element.getAnnotation(Priority.class);
		if (priority != null) {
			triggerBuilder.withPriority(priority.value());
		}

		final ScheduleBuilder<? extends Trigger> schedule = getScheduleFor(element);
		if (schedule != null) {
			triggerBuilder.withSchedule(schedule);
			return true;
		} else {
			return false;
		}
	}

	private ScheduleBuilder<? extends Trigger> getScheduleFor(
			AnnotatedElement element) {
		final Cron cron = element.getAnnotation(Cron.class);
		final RepeatInterval repeatInterval = element
				.getAnnotation(RepeatInterval.class);
		final Interval interval = element.getAnnotation(Interval.class);

		if (cron != null && repeatInterval == null && interval == null) {
			return CronScheduleBuilder.cronSchedule(cron.value());
		} else if (repeatInterval != null && interval == null && cron == null) {
			return getSimpleScheduleFor(element, repeatInterval);
		} else if (interval != null && cron != null && repeatInterval != null) {
			return getDailyTimeIntervalScheduleFor(element, interval);
		} else {
			return null;
		}
	}

	private ScheduleBuilder<? extends Trigger> getSimpleScheduleFor(
			AnnotatedElement element, final RepeatInterval repeatInterval) {
		final SimpleScheduleBuilder schedule = SimpleScheduleBuilder
				.simpleSchedule();
		schedule.withIntervalInMilliseconds(repeatInterval.value()
				* repeatInterval.period());

		final RepeatCount repeatCount = element
				.getAnnotation(RepeatCount.class);
		if (repeatCount != null) {
			schedule.withRepeatCount(repeatCount.value());
		}

		final RepeatForever repeatForever = element
				.getAnnotation(RepeatForever.class);
		if (repeatForever != null) {
			schedule.repeatForever();
		}

		return schedule;
	}

	private ScheduleBuilder<? extends Trigger> getDailyTimeIntervalScheduleFor(
			AnnotatedElement element, final Interval interval) {
		final DailyTimeIntervalScheduleBuilder schedule = DailyTimeIntervalScheduleBuilder
				.dailyTimeIntervalSchedule();
		schedule.withIntervalInSeconds(interval.value() * interval.period());

		final DaysOfTheWeek daysOfTheWeek = element
				.getAnnotation(DaysOfTheWeek.class);
		if (daysOfTheWeek != null) {
			final Set<Integer> days = new HashSet<>();
			for (final int day : daysOfTheWeek.value()) {
				days.add(day);
			}
			schedule.onDaysOfTheWeek(days);
		}

		final EveryDay everyDay = element.getAnnotation(EveryDay.class);
		if (everyDay != null) {
			schedule.onEveryDay();
		}

		final MondayThroughFriday mondayThroughFriday = element
				.getAnnotation(MondayThroughFriday.class);
		if (mondayThroughFriday != null) {
			schedule.onMondayThroughFriday();
		}

		final SaturdayAndSunday saturdayAndSunday = element
				.getAnnotation(SaturdayAndSunday.class);
		if (saturdayAndSunday != null) {
			schedule.onSaturdayAndSunday();
		}

		final StartingDailyAt startingDailyAt = element
				.getAnnotation(StartingDailyAt.class);
		if (startingDailyAt != null) {
			final TimeOfDay timeOfDay = new TimeOfDay(startingDailyAt.hour(),
					startingDailyAt.minute(), startingDailyAt.second());
			schedule.startingDailyAt(timeOfDay);
		}

		final EndingDailyAt endingDailyAt = element
				.getAnnotation(EndingDailyAt.class);
		if (endingDailyAt != null) {
			final TimeOfDay timeOfDay = new TimeOfDay(endingDailyAt.hour(),
					endingDailyAt.minute(), endingDailyAt.second());
			schedule.endingDailyAt(timeOfDay);
		}

		return schedule;
	}

}
