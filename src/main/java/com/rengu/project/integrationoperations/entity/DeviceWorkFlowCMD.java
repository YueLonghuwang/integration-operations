package com.rengu.project.integrationoperations.entity;

/**
 * author : yaojiahao
 * Date: 2019/7/15 16:03
 **/

public class DeviceWorkFlowCMD {



    private String cmd;
    private String taskFlowNo; // 任务流水号
    private String pulse;  //  内外秒脉冲选择
    private String extensionControlCharacter;  // 分机控制字
    private String threshold;  //检测门限调节
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
    private String workPattern;  //  工作模式 0:自检 1:频域搜索 2:驻留 3:空域搜索 4:中频采集 5:敌我1030 6:敌我1090 其他:无效
    private String workCycle; //  工作周期 单位50ms
    private String workCycleAmount; //  工作周期数,默认为1
    private String beginFrequency; //  起始频率 1MHz
    private String chooseBandwidth;  // 带宽选择
    public String getTaskFlowNo() {
        return taskFlowNo;
    }
    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
    public void setTaskFlowNo(String taskFlowNo) {
        this.taskFlowNo = taskFlowNo;
    }
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

    public void setWorkPattern(String workPattern) {
        this.workPattern = workPattern;
    }

    public void setWorkCycle(String workCycle) {
        this.workCycle = workCycle;
    }

    public void setWorkCycleAmount(String workCycleAmount) {
        this.workCycleAmount = workCycleAmount;
    }

    public void setBeginFrequency(String beginFrequency) {
        this.beginFrequency = beginFrequency;
    }

    public void setChooseBandwidth(String chooseBandwidth) {
        this.chooseBandwidth = chooseBandwidth;
    }

    public void setChooseAntenna1(String chooseAntenna1) {
        this.chooseAntenna1 = chooseAntenna1;
    }

    public void setChooseAntenna2(String chooseAntenna2) {
        this.chooseAntenna2 = chooseAntenna2;
    }

    public void setSelfInspectionAttenuation(String selfInspectionAttenuation) {
        this.selfInspectionAttenuation = selfInspectionAttenuation;
    }

    public void setGuidanceSwitch(String guidanceSwitch) {
        this.guidanceSwitch = guidanceSwitch;
    }

    public void setGuidance(String guidance) {
        this.guidance = guidance;
    }

    public void setFaultDetect(String faultDetect) {
        this.faultDetect = faultDetect;
    }

    public void setEndFrequency(String endFrequency) {
        this.endFrequency = endFrequency;
    }

    public void setSteppedFrequency(String steppedFrequency) {
        this.steppedFrequency = steppedFrequency;
    }

    private String chooseAntenna1;  //  天线一选择
    private String chooseAntenna2;  //  天线二选择
    //    private String attenuationRF1;  // 射频一衰减GuidanceSwitch
//    private String attenuationRF2;  // 射频二衰减
//    private String balancedAttenuationRF1;  // 射频一长电缆均衡衰减控制
//    private String balancedAttenuationRF2;  // 射频二长电缆均衡衰减控制
//    private String attenuationMF1;  // 中频一衰减
//    private String attenuationMF2;  //  中频二衰减
//    private String attenuationControlWay;  // 衰减码控制方式
    private String selfInspectionAttenuation;  // 自检源衰减

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

    public String getWorkPattern() {
        return workPattern;
    }

    public String getWorkCycle() {
        return workCycle;
    }

    public String getWorkCycleAmount() {
        return workCycleAmount;
    }

    public String getBeginFrequency() {
        return beginFrequency;
    }

    public String getChooseBandwidth() {
        return chooseBandwidth;
    }

    public String getChooseAntenna1() {
        return chooseAntenna1;
    }

    public String getChooseAntenna2() {
        return chooseAntenna2;
    }

    public String getSelfInspectionAttenuation() {
        return selfInspectionAttenuation;
    }

    public String getGuidanceSwitch() {
        return guidanceSwitch;
    }

    public String getGuidance() {
        return guidance;
    }

    public String getFaultDetect() {
        return faultDetect;
    }

    public String getEndFrequency() {
        return endFrequency;
    }

    public String getSteppedFrequency() {
        return steppedFrequency;
    }

    private String guidanceSwitch;  // 脉内引导批次号开关
    private String guidance;  //  脉内引导批次
    private String faultDetect; //  故障检测门限
    private String endFrequency;  //  终止频率 1MHz
    private String steppedFrequency; // 频率步进 单位MHZ
}
