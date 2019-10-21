package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author yyc
 * @date 2019/9/12 8:45
 * 插入日志到数据库
 */
@Entity
@Data
public class SysLogEntity implements Serializable {
	@Id
	private String id= UUID.randomUUID().toString();
	//private String userId;  //操作员id
	private String userAction; //用户操作
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
	private Date createTime=new Date();
	private String host;
	private String actionDescription;



	public String getActionDescription() {
		return actionDescription;
	}
	public void setActionDescription(String actionDescription) {
		this.actionDescription = actionDescription;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserAction() {
		return userAction;
	}
	public void setUserAction(String userAction) {
		this.userAction = userAction;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
