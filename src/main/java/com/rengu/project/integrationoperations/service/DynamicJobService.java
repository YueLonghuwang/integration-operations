package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.TimingTasks;
import com.rengu.project.integrationoperations.repository.TimingTaskRepository;
import com.rengu.project.integrationoperations.util.DynamicJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/9/12 10:39
 */
@Service
public class DynamicJobService {
    @Autowired
    private TimingTaskRepository repository;
    //通过Id获取Job
    public TimingTasks getJobEntityById(String id) {
        return repository.findById(id).get();
    }
    //从数据库中加载获取到所有Job
    public List<TimingTasks> loadJobs() {
        List<TimingTasks> list = new ArrayList<>();
        repository.findAll().forEach(list::add);
        return list;
    }
    //获取JobDataMap.(Job参数对象)
    public JobDataMap getJobDataMap(TimingTasks job) {
        JobDataMap map = new JobDataMap();
        map.put("name", job.getJobName());
        map.put("group", job.getJobGroup());
        map.put("host",job.getHost());
        map.put("cronExpression", job.getCron());
        map.put("parameter", job.getParams());
        map.put("JobDescription", job.getDescription());
        map.put("status", job.getState());
        return map;
    }
    //获取JobDetail,JobDetail是任务的定义,而Job是任务的执行逻辑,JobDetail里会引用一个Job Class来定义
    public JobDetail geJobDetail(JobKey jobKey, String description, JobDataMap map) {
        return JobBuilder.newJob(DynamicJob.class)
                .withIdentity(jobKey)
                .withDescription(description)
                .setJobData(map)
                .storeDurably()
                .build();
    }
    //获取Trigger (Job的触发器,执行规则)
    public Trigger getTrigger(TimingTasks job) {
        return TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName(), job.getJobGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCron()))
                .build();
    }
    //获取JobKey,包含Name和Group
    public JobKey getJobKey(TimingTasks job) {
        return JobKey.jobKey(job.getJobName(), job.getJobGroup());
    }

    public void deleleTaskById(String id) {
        repository.deleteById(id);
    }
}
