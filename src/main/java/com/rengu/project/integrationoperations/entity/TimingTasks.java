package com.rengu.project.integrationoperations.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

/**
 * 定时任务
 */
@Entity
@Data
public class TimingTasks implements Serializable {
    @Id
    private String id= UUID.randomUUID().toString();
    private String jobName;
    private String jobGroup;
    private String description;
    private String  host;
    private String params;
    private String cron;   //定时时间
    private int state;    //状态
}
