package com.rengu.project.integrationoperations.entity;

public class ReparedHeartBeat {
	private String before;// 记录上次数据
	private String host; //设备ip

	public ReparedHeartBeat(String before,String host) {
		super();
		this.before = before;
		this.host = host;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public ReparedHeartBeat() {
		super();
	}

	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}


}
