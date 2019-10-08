package com.rengu.project.integrationoperations.entity;

/**
 * author : yaojiahao
 * Date: 2019/7/15 16:03
 **/

public class DeviceWorkFlowCMD{


    private String cmd;
    private String taskFlowNo; // 任务流水号
    /**
     *  雷达分机指令
     */
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
    /**
     * 敌我侦查
     */
    private String messageNumber; // 信息包序号
    private String timeCode; // 时间码（同步时间）
    private String workPattern; // 工作模式(工作模式)
    private String bandwidthChoose; // 带宽选择
    private String PulseChoice; // 秒脉冲选择
    private String selfChoose;  //自检模式选择
    private String pointSelection; //自检频点选择
    private String hierarchicalControl; // 分机控制字
    private String PDW740; // PDW740个数
    private String PDW837; // 837.5PWD个数
    private String PDW1030;  // 1030PD个数
    private String PDW1059; // PDW1059个数
    private String PDW1090; // 1090PWD个数
    private String PDW1464; // 1464PDW个数
    private String PDW1532; // 1532PDW个数
    private String mid740; //740中频个数
    private String mid1030; //1030中频个数
    private String mid1090; //1090中频个数
    private String IfAcquisitionTime; // 中频采集时间
    private String faultDetection; //故障检测门限
    private String networkPacketCounting; //网络包计数

    /**
     * 未用到参数
     */
    private String antennaSelection; // 天线选择
    private String IPReconsitution; // 分机IP重构
    private String IfAcquisitionMode; // 中频采集模式
    private String FPGAReconsitution; // FPGA重构标识
    private String DSPReconsitution; // DSP重构标识

    public String getPDW1059() {
        return PDW1059;
    }

    public void setPDW1059(String PDW1059) {
        this.PDW1059 = PDW1059;
    }

    public String getPDW740() {
        return PDW740;
    }

    public void setPDW740(String PDW740) {
        this.PDW740 = PDW740;
    }

    public String getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(String messageNumber) {
        this.messageNumber = messageNumber;
    }

    public void setTimeCode(String timeCode) {
        this.timeCode = timeCode;
    }

    public void setWorkPattern(String workPattern) {
        this.workPattern = workPattern;
    }

    public void setBandwidthChoose(String bandwidthChoose) {
        this.bandwidthChoose = bandwidthChoose;
    }

    public void setPulseChoice(String pulseChoice) {
        PulseChoice = pulseChoice;
    }

    public void setAntennaSelection(String antennaSelection) {
        this.antennaSelection = antennaSelection;
    }

    public void setIPReconsitution(String IPReconsitution) {
        this.IPReconsitution = IPReconsitution;
    }

    public void setIfAcquisitionMode(String ifAcquisitionMode) {
        IfAcquisitionMode = ifAcquisitionMode;
    }

    public void setIfAcquisitionTime(String ifAcquisitionTime) {
        IfAcquisitionTime = ifAcquisitionTime;
    }

    public void setFPGAReconsitution(String FPGAReconsitution) {
        this.FPGAReconsitution = FPGAReconsitution;
    }

    public void setDSPReconsitution(String DSPReconsitution) {
        this.DSPReconsitution = DSPReconsitution;
    }

    public void setPDW1030(String PDW1030) {
        this.PDW1030 = PDW1030;
    }

    public void setPDW1090(String PDW1090) {
        this.PDW1090 = PDW1090;
    }

    public void setPDW837(String PDW837) {
        this.PDW837 = PDW837;
    }

    public void setPDW1464(String PDW1464) {
        this.PDW1464 = PDW1464;
    }

    public void setPDW1532(String PDW1532) {
        this.PDW1532 = PDW1532;
    }

    public void setHierarchicalControl(String hierarchicalControl) {
        this.hierarchicalControl = hierarchicalControl;
    }

    public String getTimeCode() {
        return timeCode;
    }

    public String getWorkPattern() {
        return workPattern;
    }

    public String getBandwidthChoose() {
        return bandwidthChoose;
    }

    public String getPulseChoice() {
        return PulseChoice;
    }

    public String getAntennaSelection() {
        return antennaSelection;
    }

    public String getIPReconsitution() {
        return IPReconsitution;
    }

    public String getIfAcquisitionMode() {
        return IfAcquisitionMode;
    }

    public String getIfAcquisitionTime() {
        return IfAcquisitionTime;
    }

    public String getFPGAReconsitution() {
        return FPGAReconsitution;
    }

    public String getDSPReconsitution() {
        return DSPReconsitution;
    }

    public String getPDW1030() {
        return PDW1030;
    }

    public String getPDW1090() {
        return PDW1090;
    }

    public String getPDW837() {
        return PDW837;
    }

    public String getPDW1464() {
        return PDW1464;
    }

    public String getPDW1532() {
        return PDW1532;
    }

    public String getHierarchicalControl() {
        return hierarchicalControl;
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

    public String getCmd() {
        return cmd;
    }

    public String getTaskFlowNo() {
        return taskFlowNo;
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
}
