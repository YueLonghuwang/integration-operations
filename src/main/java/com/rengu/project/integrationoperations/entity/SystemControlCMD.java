package com.rengu.project.integrationoperations.entity;


/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 12:54
 */
public class SystemControlCMD {
    private String workPattern;  //  工作模式 0:自检 1:频域搜索 2:驻留 3:空域搜索 4:中频采集 5:敌我1030 6:敌我1090 其他:无效
    //private String workCycle; //  工作周期 单位50ms
    private String workPeriod;
    //private String workCycleAmount; //  工作周期数,默认为1
    private String workPeriodNum;
    //private String beginFrequency; //  起始频率 1MHz
    private String initialFrequency;
    //private String chooseBandwidth;  // 带宽选择
    private String bandWidthSelection;
    //private String chooseAntenna1;  //  天线一选择
    private String antennaSelection1;
    //private String chooseAntenna2;  //  天线二选择
    private String antennaSelection2;
    //private String attenuationRF1;  // 射频一衰减GuidanceSwitch
    private String radioFrequencyAttenuation1;
    //private String attenuationRF2;  // 射频二衰减
    private String radioFrequencyAttenuation2;
    //private String balancedAttenuationRF1;  // 射频一长电缆均衡衰减控制
    private String attenuationControl1;
    //private String balancedAttenuationRF2;  // 射频二长电缆均衡衰减控制
    private String attenuationControl2;
    //private String attenuationMF1;  // 中频一衰减
    private String midCut1;
    //private String attenuationMF2;  //  中频二衰减
    private String midCut2;
    //private String attenuationControlWay;  // 衰减码控制方式
    private String attenuationCodeControlMode;
    //private String selfInspectionAttenuation;  // 自检源衰减
    private String selfCheckingSourceAttenuation;
    //private String guidanceSwitch;  // 脉内引导批次号开关
    private String batchNumberSwitch;
    //private String guidance;  //  脉内引导批次
    private String batchNumber;
    //private String faultDetect; //  故障检测门限
    private String faultDetectionThreshold;
    //private String endFrequency;  //  终止频率 1MHz
    private String terminationFrequency;
    //private String steppedFrequency; // 频率步进 单位MHZ
    private String steppedFrequency;
    private String timingTimeCode;  // 定时时间码
    private String timeRequired;  // 单次执行指令集所需时间
    public void setWorkPattern(String workPattern) {
        this.workPattern = workPattern;
    }

    public String getWorkPattern() {
        return workPattern;
    }

    public String getWorkPeriod() {
        return workPeriod;
    }

    public void setWorkPeriod(String workPeriod) {
        this.workPeriod = workPeriod;
    }

    public String getWorkPeriodNum() {
        return workPeriodNum;
    }

    public void setWorkPeriodNum(String workPeriodNum) {
        this.workPeriodNum = workPeriodNum;
    }

    public String getInitialFrequency() {
        return initialFrequency;
    }

    public void setInitialFrequency(String initialFrequency) {
        this.initialFrequency = initialFrequency;
    }

    public String getBandWidthSelection() {
        return bandWidthSelection;
    }

    public void setBandWidthSelection(String bandWidthSelection) {
        this.bandWidthSelection = bandWidthSelection;
    }

    public String getAntennaSelection1() {
        return antennaSelection1;
    }

    public void setAntennaSelection1(String antennaSelection1) {
        this.antennaSelection1 = antennaSelection1;
    }

    public String getAntennaSelection2() {
        return antennaSelection2;
    }

    public void setAntennaSelection2(String antennaSelection2) {
        this.antennaSelection2 = antennaSelection2;
    }

    public String getRadioFrequencyAttenuation1() {
        return radioFrequencyAttenuation1;
    }

    public void setRadioFrequencyAttenuation1(String radioFrequencyAttenuation1) {
        this.radioFrequencyAttenuation1 = radioFrequencyAttenuation1;
    }

    public String getRadioFrequencyAttenuation2() {
        return radioFrequencyAttenuation2;
    }

    public void setRadioFrequencyAttenuation2(String radioFrequencyAttenuation2) {
        this.radioFrequencyAttenuation2 = radioFrequencyAttenuation2;
    }

    public String getAttenuationControl1() {
        return attenuationControl1;
    }

    public void setAttenuationControl1(String attenuationControl1) {
        this.attenuationControl1 = attenuationControl1;
    }

    public String getAttenuationControl2() {
        return attenuationControl2;
    }

    public void setAttenuationControl2(String attenuationControl2) {
        this.attenuationControl2 = attenuationControl2;
    }

    public String getMidCut1() {
        return midCut1;
    }

    public void setMidCut1(String midCut1) {
        this.midCut1 = midCut1;
    }

    public String getMidCut2() {
        return midCut2;
    }

    public void setMidCut2(String midCut2) {
        this.midCut2 = midCut2;
    }

    public String getAttenuationCodeControlMode() {
        return attenuationCodeControlMode;
    }

    public void setAttenuationCodeControlMode(String attenuationCodeControlMode) {
        this.attenuationCodeControlMode = attenuationCodeControlMode;
    }

    public String getSelfCheckingSourceAttenuation() {
        return selfCheckingSourceAttenuation;
    }

    public void setSelfCheckingSourceAttenuation(String selfCheckingSourceAttenuation) {
        this.selfCheckingSourceAttenuation = selfCheckingSourceAttenuation;
    }

    public String getBatchNumberSwitch() {
        return batchNumberSwitch;
    }

    public void setBatchNumberSwitch(String batchNumberSwitch) {
        this.batchNumberSwitch = batchNumberSwitch;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getFaultDetectionThreshold() {
        return faultDetectionThreshold;
    }

    public void setFaultDetectionThreshold(String faultDetectionThreshold) {
        this.faultDetectionThreshold = faultDetectionThreshold;
    }

    public String getTerminationFrequency() {
        return terminationFrequency;
    }

    public void setTerminationFrequency(String terminationFrequency) {
        this.terminationFrequency = terminationFrequency;
    }

    public String getSteppedFrequency() {
        return steppedFrequency;
    }

    public void setSteppedFrequency(String steppedFrequency) {
        this.steppedFrequency = steppedFrequency;
    }

    public String getTimingTimeCode() {
        return timingTimeCode;
    }

    public void setTimingTimeCode(String timingTimeCode) {
        this.timingTimeCode = timingTimeCode;
    }

    public String getTimeRequired() {
        return timeRequired;
    }

    public void setTimeRequired(String timeRequired) {
        this.timeRequired = timeRequired;
    }
}
