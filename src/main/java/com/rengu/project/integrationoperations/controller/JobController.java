package com.rengu.project.integrationoperations.controller;


import com.rengu.project.integrationoperations.entity.TimingTasks;
import com.rengu.project.integrationoperations.service.DynamicJobService;
import com.rengu.project.integrationoperations.util.CronDateUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Set;

/**
 * Created by EalenXie on 2018/6/4 16:12
 */
@RestController
@RequestMapping("/job")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
    @Autowired
    private DynamicJobService jobService;

    //初始化启动所有的Job
    @PostConstruct
    public void initialize() {
        try {
            reStartAllJobs();
            logger.info("INIT SUCCESS");
        } catch (SchedulerException e) {
            logger.info("INIT EXCEPTION : " + e.getMessage());
            e.printStackTrace();
        }
    }

    //根据ID重启某个Job
    @RequestMapping("/refresh/{id}")
    public String refresh(@PathVariable String  id) throws SchedulerException {
        String result;
        TimingTasks entity = jobService.getJobEntityById(id);
        if (entity == null) return "error: id is not exist ";
        synchronized (logger) {
            JobKey jobKey = jobService.getJobKey(entity);
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.pauseJob(jobKey);
            scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
            scheduler.deleteJob(jobKey);
            JobDataMap map = jobService.getJobDataMap(entity);
            JobDetail jobDetail = jobService.geJobDetail(jobKey, entity.getDescription(), map);
            if (entity.getState()==1) {
                scheduler.scheduleJob(jobDetail, jobService.getTrigger(entity));
                result = "Refresh Job : " + entity.getJobName() + " success !";
            } else {
                result = "Refresh Job : " + entity.getJobName() + " failed ! , " +
                        "Because the Job status is " + entity.getState();
            }
        }
        return result;
    }


    //重启数据库中所有的Job
    @RequestMapping("/refresh/all")
    public String refreshAll() {
        String result;
        try {
            reStartAllJobs();
            result = "SUCCESS";
        } catch (SchedulerException e) {
            result = "EXCEPTION : " + e.getMessage();
        }
        return "refresh all jobs : " + result;
    }

    /**
     * 重新启动所有的job
     */
    private void reStartAllJobs() throws SchedulerException {
        synchronized (logger) {                                                         //只允许一个线程进入操作
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            Set<JobKey> set = scheduler.getJobKeys(GroupMatcher.anyGroup());
            scheduler.pauseJobs(GroupMatcher.anyGroup());                               //暂停所有JOB
            for (JobKey jobKey : set) {                                                 //删除从数据库中注册的所有JOB
                scheduler.unscheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()));
                scheduler.deleteJob(jobKey);
            }
            for (TimingTasks job : jobService.loadJobs()) {                               //从数据库中注册的所有JOB
                Date setDate = CronDateUtils.getDate(job.getCron());
                if(setDate.before(new Date())){
                    jobService.deleleTaskById(job.getId());
                    continue;
                }

                logger.info("Job register name : {} , group : {} , cron : {}", job.getJobName(), job.getJobGroup(), job.getCron());
                JobDataMap map = jobService.getJobDataMap(job);
                JobKey jobKey = jobService.getJobKey(job);
                JobDetail jobDetail = jobService.geJobDetail(jobKey, job.getDescription(), map);
                if (job.getState()==1) scheduler.scheduleJob(jobDetail, jobService.getTrigger(job));
                else
                    logger.info("Job jump name : {} , Because {} status is {}", job.getJobGroup(), job.getJobName(), job.getState());
            }
        }
    }


}