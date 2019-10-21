package com.rengu.project.integrationoperations.entity;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Author: XYmar
 * Date: 2019/9/12 17:35
 */
@Data
public class SendContent implements Serializable {
	private String time;
	private String timeNow;
	private String sendTime;
	private String timingPattern;
	private String host;
	private String updateAll;
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
}
