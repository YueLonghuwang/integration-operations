package com.rengu.project.integrationoperations.entity;

import java.io.Serializable;

public class EnemyAndUsCMD implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 敌我侦查
	 */
	private String messageNumber; // 信息包序号
	private String timeCode; // 时间码（同步时间）
	private String workPattern1; // 工作模式(工作模式)
	private String bandwidthChoose; // 带宽选择
	private String pulseChoice; // 脉冲选择
	private String selfChoose; // 自检模式选择
	private String pointSelection; // 自检频点选择
	// private String synchronizationTime; //同步时间
	private String hierarchicalControl; // 分机控制字
	private String pdw740; // pdw740个数
	private String pdw837; // 837.5PWD个数
	private String pdw1030; // 1030PD个数
	private String pdw1059; // pdw1059个数
	private String pdw1090; // 1090PWD个数
	private String pdw1464; // 1464pdw个数
	private String pdw1532; // 1532pdw个数
	private String mid740; // 740中频个数
	private String mid1030; // 1030中频个数
	private String mid1090; // 1090中频个数
	private String ifAcquisitionTime; // 中频采集时间
	private String faultDetection; // 故障检测门限
	private String networkPacketCounting; // 网络包计数

	public EnemyAndUsCMD() {
		super();
	}

	public String getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(String messageNumber) {
		this.messageNumber = messageNumber;
	}

	public String getTimeCode() {
		return timeCode;
	}

	public void setTimeCode(String timeCode) {
		this.timeCode = timeCode;
	}

	public String getWorkPattern1() {
		return workPattern1;
	}

	public void setWorkPattern1(String workPattern1) {
		this.workPattern1 = workPattern1;
	}

	public String getBandwidthChoose() {
		return bandwidthChoose;
	}

	public void setBandwidthChoose(String bandwidthChoose) {
		this.bandwidthChoose = bandwidthChoose;
	}

	public String getPulseChoice() {
		return pulseChoice;
	}

	public void setPulseChoice(String pulseChoice) {
		this.pulseChoice = pulseChoice;
	}

	public String getSelfChoose() {
		return selfChoose;
	}

	public void setSelfChoose(String selfChoose) {
		this.selfChoose = selfChoose;
	}

	public String getPointSelection() {
		return pointSelection;
	}

	public void setPointSelection(String pointSelection) {
		this.pointSelection = pointSelection;
	}

	public String getHierarchicalControl() {
		return hierarchicalControl;
	}

	public void setHierarchicalControl(String hierarchicalControl) {
		this.hierarchicalControl = hierarchicalControl;
	}

	public String getPdw740() {
		return pdw740;
	}

	public void setPdw740(String pdw740) {
		this.pdw740 = pdw740;
	}

	public String getPdw837() {
		return pdw837;
	}

	public void setPdw837(String pdw837) {
		this.pdw837 = pdw837;
	}

	public String getPdw1030() {
		return pdw1030;
	}

	public void setPdw1030(String pdw1030) {
		this.pdw1030 = pdw1030;
	}

	public String getPdw1059() {
		return pdw1059;
	}

	public void setPdw1059(String pdw1059) {
		this.pdw1059 = pdw1059;
	}

	public String getPdw1090() {
		return pdw1090;
	}

	public void setPdw1090(String pdw1090) {
		this.pdw1090 = pdw1090;
	}

	public String getPdw1464() {
		return pdw1464;
	}

	public void setPdw1464(String pdw1464) {
		this.pdw1464 = pdw1464;
	}

	public String getPdw1532() {
		return pdw1532;
	}

	public void setPdw1532(String pdw1532) {
		this.pdw1532 = pdw1532;
	}

	public String getMid740() {
		return mid740;
	}

	public void setMid740(String mid740) {
		this.mid740 = mid740;
	}

	public String getMid1030() {
		return mid1030;
	}

	public void setMid1030(String mid1030) {
		this.mid1030 = mid1030;
	}

	public String getMid1090() {
		return mid1090;
	}

	public void setMid1090(String mid1090) {
		this.mid1090 = mid1090;
	}

	public String getIfAcquisitionTime() {
		return ifAcquisitionTime;
	}

	public void setIfAcquisitionTime(String ifAcquisitionTime) {
		this.ifAcquisitionTime = ifAcquisitionTime;
	}

	public String getFaultDetection() {
		return faultDetection;
	}

	public void setFaultDetection(String faultDetection) {
		this.faultDetection = faultDetection;
	}

	public String getNetworkPacketCounting() {
		return networkPacketCounting;
	}

	public void setNetworkPacketCounting(String networkPacketCounting) {
		this.networkPacketCounting = networkPacketCounting;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
