package com.rengu.project.integrationoperations.entity;

import java.io.Serializable;

public class RadarDeviceCMD implements Serializable {
	/**
	 * 雷达分机指令
	 */
	private String pulse; // 内外秒脉冲选择
	private String extensionControlCharacter; // 分机控制字
	private String threshold; // 检测门限调节
	private String overallpulse; // 需要上传的全脉冲数
	private String minamplitude; // 最小幅度
	private String maxamplitude; // 最大幅度
	private String minPulsewidth; // 最小脉宽
	private String maxPulsewidth; // 最大脉宽
	private String filterMaximumFrequency; // 筛选最大频率
	private String filterMinimumFrequency; // 筛选最小频率
	private String shieldingMaximumFrequency; // 屏蔽最大频率
	private String shieldingMinimumFrequency; // 屏蔽最小频率
	private String defalutUpdate; // 默认值更新标记

	public String getPulse() {
		return pulse;
	}

	public void setPulse(String pulse) {
		this.pulse = pulse;
	}

	public String getExtensionControlCharacter() {
		return extensionControlCharacter;
	}

	public void setExtensionControlCharacter(String extensionControlCharacter) {
		this.extensionControlCharacter = extensionControlCharacter;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public String getOverallpulse() {
		return overallpulse;
	}

	public void setOverallpulse(String overallpulse) {
		this.overallpulse = overallpulse;
	}

	public String getMinamplitude() {
		return minamplitude;
	}

	public void setMinamplitude(String minamplitude) {
		this.minamplitude = minamplitude;
	}

	public String getMaxamplitude() {
		return maxamplitude;
	}

	public void setMaxamplitude(String maxamplitude) {
		this.maxamplitude = maxamplitude;
	}

	public String getMinPulsewidth() {
		return minPulsewidth;
	}

	public void setMinPulsewidth(String minPulsewidth) {
		this.minPulsewidth = minPulsewidth;
	}

	public String getMaxPulsewidth() {
		return maxPulsewidth;
	}

	public void setMaxPulsewidth(String maxPulsewidth) {
		this.maxPulsewidth = maxPulsewidth;
	}

	public String getFilterMaximumFrequency() {
		return filterMaximumFrequency;
	}

	public void setFilterMaximumFrequency(String filterMaximumFrequency) {
		this.filterMaximumFrequency = filterMaximumFrequency;
	}

	public String getFilterMinimumFrequency() {
		return filterMinimumFrequency;
	}

	public void setFilterMinimumFrequency(String filterMinimumFrequency) {
		this.filterMinimumFrequency = filterMinimumFrequency;
	}

	public String getShieldingMaximumFrequency() {
		return shieldingMaximumFrequency;
	}

	public void setShieldingMaximumFrequency(String shieldingMaximumFrequency) {
		this.shieldingMaximumFrequency = shieldingMaximumFrequency;
	}

	public String getShieldingMinimumFrequency() {
		return shieldingMinimumFrequency;
	}

	public void setShieldingMinimumFrequency(String shieldingMinimumFrequency) {
		this.shieldingMinimumFrequency = shieldingMinimumFrequency;
	}

	public String getDefalutUpdate() {
		return defalutUpdate;
	}

	public void setDefalutUpdate(String defalutUpdate) {
		this.defalutUpdate = defalutUpdate;
	}
}
