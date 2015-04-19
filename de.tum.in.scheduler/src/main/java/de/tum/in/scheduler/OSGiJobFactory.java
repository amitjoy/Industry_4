package de.tum.in.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

public class OSGiJobFactory implements JobFactory {
    
    private final Map<Class<? extends Job>, Job> m_jobs = new ConcurrentHashMap<>();
    
    public void jobAdded(Job job) {
        m_jobs.put(job.getClass(), job);
    }
    
    public void jobRemoved(Job job) {
        m_jobs.remove(job.getClass());
    }

    @Override
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        final JobDetail jobDetail = bundle.getJobDetail();
        final Class<? extends Job> jobClass = jobDetail.getJobClass();
        return m_jobs.get(jobClass);
    }

}
