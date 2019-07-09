package com.rengu.project.integrationoperations.entity;


/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 12:56
 */
public class ExtensionControlCMD {
    private String pulse;  //  内外秒脉冲选择
    private String extensionControlCharacter;  // 分机控制字
    private String threshold;  //检测门限调节

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public void setExtensionControlCharacter(String extensionControlCharacter) {
        this.extensionControlCharacter = extensionControlCharacter;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public void setOverallpulse(String overallpulse) {
        this.overallpulse = overallpulse;
    }

    public void setMinamplitude(String minamplitude) {
        this.minamplitude = minamplitude;
    }

    public void setMaxamplitude(String maxamplitude) {
        this.maxamplitude = maxamplitude;
    }

    public void setMinPulsewidth(String minPulsewidth) {
        this.minPulsewidth = minPulsewidth;
    }

    public void setMaxPulsewidth(String maxPulsewidth) {
        this.maxPulsewidth = maxPulsewidth;
    }

    public void setFilterMaximumFrequency(String filterMaximumFrequency) {
        this.filterMaximumFrequency = filterMaximumFrequency;
    }

    public void setFilterMinimumFrequency(String filterMinimumFrequency) {
        this.filterMinimumFrequency = filterMinimumFrequency;
    }

    public void setShieldingMaximumFrequency(String shieldingMaximumFrequency) {
        this.shieldingMaximumFrequency = shieldingMaximumFrequency;
    }

    public void setShieldingMinimumFrequency(String shieldingMinimumFrequency) {
        this.shieldingMinimumFrequency = shieldingMinimumFrequency;
    }

    public void setDefalutUpdate(String defalutUpdate) {
        this.defalutUpdate = defalutUpdate;
    }

    public String getPulse() {
        return pulse;
    }

    public String getExtensionControlCharacter() {
        return extensionControlCharacter;
    }

    public String getThreshold() {
        return threshold;
    }

    public String getOverallpulse() {
        return overallpulse;
    }

    public String getMinamplitude() {
        return minamplitude;
    }

    public String getMaxamplitude() {
        return maxamplitude;
    }

    public String getMinPulsewidth() {
        return minPulsewidth;
    }

    public String getMaxPulsewidth() {
        return maxPulsewidth;
    }

    public String getFilterMaximumFrequency() {
        return filterMaximumFrequency;
    }

    public String getFilterMinimumFrequency() {
        return filterMinimumFrequency;
    }

    public String getShieldingMaximumFrequency() {
        return shieldingMaximumFrequency;
    }

    public String getShieldingMinimumFrequency() {
        return shieldingMinimumFrequency;
    }

    public String getDefalutUpdate() {
        return defalutUpdate;
    }

    private String overallpulse;  //  需要上传的全脉冲数
    private String minamplitude; // 最小幅度
    private String maxamplitude;  // 最大幅度
    private String minPulsewidth; // 最小脉宽
    private String maxPulsewidth; // 最大脉宽
    private String filterMaximumFrequency; // 筛选最大频率
    private String filterMinimumFrequency; // 筛选最小频率
    private String shieldingMaximumFrequency; // 屏蔽最大频率
    private String shieldingMinimumFrequency; // 屏蔽最小频率
    private String defalutUpdate;  // 默认值更新标记
}
