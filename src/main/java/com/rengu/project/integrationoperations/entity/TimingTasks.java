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
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobGroup() {
		return jobGroup;
	}
	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}


}
