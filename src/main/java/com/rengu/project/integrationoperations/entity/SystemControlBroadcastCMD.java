package com.rengu.project.integrationoperations.entity;


public class SystemControlBroadcastCMD {
    private short messagePackageNum; // 信息包序号
    private String timeCode;  // 时间码
    private byte workWay; // 工作方式
    private byte bandwidthChoose; // 带宽选择
    private byte workCycleNum; //  工作周期数

    public void setMessagePackageNum(short messagePackageNum) {
        this.messagePackageNum = messagePackageNum;
    }

    public void setTimeCode(String timeCode) {
        this.timeCode = timeCode;
    }

    public void setWorkWay(byte workWay) {
        this.workWay = workWay;
    }

    public void setBandwidthChoose(byte bandwidthChoose) {
        this.bandwidthChoose = bandwidthChoose;
    }

    public void setWorkCycleNum(byte workCycleNum) {
        this.workCycleNum = workCycleNum;
    }

    public void setWorkCycleLength(byte workCycleLength) {
        this.workCycleLength = workCycleLength;
    }

    public void setCenterFrequency(short centerFrequency) {
        this.centerFrequency = centerFrequency;
    }

    public void setDirectionFindingAntennaChoose(byte directionFindingAntennaChoose) {
        this.directionFindingAntennaChoose = directionFindingAntennaChoose;
    }

    public void setScoutAntennaChoose(byte scoutAntennaChoose) {
        this.scoutAntennaChoose = scoutAntennaChoose;
    }

    public void setPulseScreenMinimumFrequency(short pulseScreenMinimumFrequency) {
        this.pulseScreenMinimumFrequency = pulseScreenMinimumFrequency;
    }

    public void setPulseScreenMaximumFrequency(short pulseScreenMaximumFrequency) {
        this.pulseScreenMaximumFrequency = pulseScreenMaximumFrequency;
    }

    public void setPulseScreenMinimumRange(byte pulseScreenMinimumRange) {
        this.pulseScreenMinimumRange = pulseScreenMinimumRange;
    }

    public void setPulseScreenMaximumRange(byte pulseScreenMaximumRange) {
        this.pulseScreenMaximumRange = pulseScreenMaximumRange;
    }

    public void setPulseScreenMinimumPulseWidth(short pulseScreenMinimumPulseWidth) {
        this.pulseScreenMinimumPulseWidth = pulseScreenMinimumPulseWidth;
    }

    public void setPulseScreenMaximumPulseWidth(short pulseScreenMaximumPulseWidth) {
        this.pulseScreenMaximumPulseWidth = pulseScreenMaximumPulseWidth;
    }

    public void setRouteShield(byte[] routeShield) {
        this.routeShield = routeShield;
    }

    public void setWithinThePulseGuidanceSwitch(byte withinThePulseGuidanceSwitch) {
        this.withinThePulseGuidanceSwitch = withinThePulseGuidanceSwitch;
    }

    public void setWithinThePulseGuidance(byte withinThePulseGuidance) {
        this.withinThePulseGuidance = withinThePulseGuidance;
    }

    public void setUploadFullPulseNum(short uploadFullPulseNum) {
        UploadFullPulseNum = uploadFullPulseNum;
    }

    public void setExtensionControl(String extensionControl) {
        this.extensionControl = extensionControl;
    }

    public void setEquipmentSerialNum(String equipmentSerialNum) {
        this.equipmentSerialNum = equipmentSerialNum;
    }

    public void setDetectionThresholdAdjustment(byte detectionThresholdAdjustment) {
        this.detectionThresholdAdjustment = detectionThresholdAdjustment;
    }

    private byte workCycleLength; // 工作周期长度
    private short centerFrequency; // 中心频率

    public short getMessagePackageNum() {
        return messagePackageNum;
    }

    public String getTimeCode() {
        return timeCode;
    }

    public byte getWorkWay() {
        return workWay;
    }

    public byte getBandwidthChoose() {
        return bandwidthChoose;
    }

    public byte getWorkCycleNum() {
        return workCycleNum;
    }

    public byte getWorkCycleLength() {
        return workCycleLength;
    }

    public short getCenterFrequency() {
        return centerFrequency;
    }

    public byte getDirectionFindingAntennaChoose() {
        return directionFindingAntennaChoose;
    }

    public byte getScoutAntennaChoose() {
        return scoutAntennaChoose;
    }

    public short getPulseScreenMinimumFrequency() {
        return pulseScreenMinimumFrequency;
    }

    public short getPulseScreenMaximumFrequency() {
        return pulseScreenMaximumFrequency;
    }

    public byte getPulseScreenMinimumRange() {
        return pulseScreenMinimumRange;
    }

    public byte getPulseScreenMaximumRange() {
        return pulseScreenMaximumRange;
    }

    public short getPulseScreenMinimumPulseWidth() {
        return pulseScreenMinimumPulseWidth;
    }

    public short getPulseScreenMaximumPulseWidth() {
        return pulseScreenMaximumPulseWidth;
    }

    public byte[] getRouteShield() {
        return routeShield;
    }

    public byte getWithinThePulseGuidanceSwitch() {
        return withinThePulseGuidanceSwitch;
    }

    public byte getWithinThePulseGuidance() {
        return withinThePulseGuidance;
    }

    public short getUploadFullPulseNum() {
        return UploadFullPulseNum;
    }

    public String getExtensionControl() {
        return extensionControl;
    }

    public String getEquipmentSerialNum() {
        return equipmentSerialNum;
    }

    public byte getDetectionThresholdAdjustment() {
        return detectionThresholdAdjustment;
    }

    private byte directionFindingAntennaChoose; // 测向天线选择
    private byte scoutAntennaChoose; // 侦察天线选择
    private short pulseScreenMinimumFrequency; // 脉冲筛选最小频率
    private short pulseScreenMaximumFrequency; // 脉冲筛选最大频率
    private byte pulseScreenMinimumRange; // 脉冲筛选最小幅度
    private byte pulseScreenMaximumRange;// 脉冲筛选最大幅度
    private short pulseScreenMinimumPulseWidth; // 脉冲筛选最小脉宽
    private short pulseScreenMaximumPulseWidth; // 脉冲筛选最大脉宽
    private byte[] routeShield; // 信道屏蔽
    private byte withinThePulseGuidanceSwitch; // 脉内引导批次号开关
    private byte withinThePulseGuidance; // 脉内引导批次号
    private short UploadFullPulseNum; // 需要上传的全脉冲数
    private String extensionControl; // 分机控制
    private String equipmentSerialNum;  // 设备编号
    private byte detectionThresholdAdjustment; // 检测门限调节
}
