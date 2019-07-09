package com.rengu.project.integrationoperations.entity;


/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 12:54
 */
public class SystemControlCMD {
    private String workPattern;  //  工作模式 0:自检 1:频域搜索 2:驻留 3:空域搜索 4:中频采集 5:敌我1030 6:敌我1090 其他:无效
    private String workCycle; //  工作周期 单位50ms
    private String workCycleAmount; //  工作周期数,默认为1
    private String beginFrequency; //  起始频率 1MHz

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

    public void setEndFrequency(String endFrequency) {
        this.endFrequency = endFrequency;
    }

    public void setSteppedFrequency(String steppedFrequency) {
        this.steppedFrequency = steppedFrequency;
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

    private String endFrequency;  //  终止频率 1MHz
    private String steppedFrequency; // 频率步进 单位MHZ

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

    public String getEndFrequency() {
        return endFrequency;
    }

    public String getSteppedFrequency() {
        return steppedFrequency;
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

    private String chooseBandwidth;  // 带宽选择
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
    private String guidanceSwitch;  // 脉内引导批次号开关
    private String guidance;  //  脉内引导批次
    private String faultDetect; //  故障检测门限
//    private String timingCode;  // 定时时间码
//    private String onceExecuteCMDTimeNeeded;  // 单次执行指令集所需时间
}
