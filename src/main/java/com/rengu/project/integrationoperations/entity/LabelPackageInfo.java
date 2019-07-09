package com.rengu.project.integrationoperations.entity;


public class LabelPackageInfo {
    private byte[] GPSData; // GPS数据
    private byte sendNodeNum;  //  发方节点号
    private byte receiveNodeNum;  //  收方节点号
    private short feedbackCmdSerialNum; //  反馈指令序号
    private String receiveCmdState; //  指令接收状态
    private short workNum; // 工作编号

    public void setGPSData(byte[] GPSData) {
        this.GPSData = GPSData;
    }

    public void setSendNodeNum(byte sendNodeNum) {
        this.sendNodeNum = sendNodeNum;
    }

    public void setReceiveNodeNum(byte receiveNodeNum) {
        this.receiveNodeNum = receiveNodeNum;
    }

    public void setFeedbackCmdSerialNum(short feedbackCmdSerialNum) {
        this.feedbackCmdSerialNum = feedbackCmdSerialNum;
    }

    public void setReceiveCmdState(String receiveCmdState) {
        this.receiveCmdState = receiveCmdState;
    }

    public void setWorkNum(short workNum) {
        this.workNum = workNum;
    }

    public void setFrontEndWorkT(short frontEndWorkT) {
        this.frontEndWorkT = frontEndWorkT;
    }

    public void setExtensionWorkT(short extensionWorkT) {
        this.extensionWorkT = extensionWorkT;
    }

    public void setExtensionWorkState(String extensionWorkState) {
        this.extensionWorkState = extensionWorkState;
    }

    public void setOverallPulseCount(int overallPulseCount) {
        this.overallPulseCount = overallPulseCount;
    }

    public void setRadiationSourcePacketStatistics(int radiationSourcePacketStatistics) {
        this.radiationSourcePacketStatistics = radiationSourcePacketStatistics;
    }

    public void setIfDataStatistics(int ifDataStatistics) {
        this.ifDataStatistics = ifDataStatistics;
    }

    public void setEquipmentNum(byte equipmentNum) {
        this.equipmentNum = equipmentNum;
    }

    public void setLongCableBalancedAttenuationControlOne(byte longCableBalancedAttenuationControlOne) {
        LongCableBalancedAttenuationControlOne = longCableBalancedAttenuationControlOne;
    }

    public void setLongCableBalancedAttenuationControlTwo(byte longCableBalancedAttenuationControlTwo) {
        LongCableBalancedAttenuationControlTwo = longCableBalancedAttenuationControlTwo;
    }

    public void setIFAttenuationOne(byte IFAttenuationOne) {
        this.IFAttenuationOne = IFAttenuationOne;
    }

    public void setIFAttenuationTwo(byte IFAttenuationTwo) {
        this.IFAttenuationTwo = IFAttenuationTwo;
    }

    public void setFrontEndState(byte[] frontEndState) {
        this.frontEndState = frontEndState;
    }

    public void setKeyState(byte[] keyState) {
        this.keyState = keyState;
    }

    public void setStandbyApplication(byte[] standbyApplication) {
        this.standbyApplication = standbyApplication;
    }

    public void setSystemControlBroadcastCMD(SystemControlBroadcastCMD systemControlBroadcastCMD) {
        this.systemControlBroadcastCMD = systemControlBroadcastCMD;
    }

    private short frontEndWorkT;  //  前端工作温度

    public byte[] getGPSData() {
        return GPSData;
    }

    public byte getSendNodeNum() {
        return sendNodeNum;
    }

    public byte getReceiveNodeNum() {
        return receiveNodeNum;
    }

    public short getFeedbackCmdSerialNum() {
        return feedbackCmdSerialNum;
    }

    public String getReceiveCmdState() {
        return receiveCmdState;
    }

    public short getWorkNum() {
        return workNum;
    }

    public short getFrontEndWorkT() {
        return frontEndWorkT;
    }

    public short getExtensionWorkT() {
        return extensionWorkT;
    }

    public String getExtensionWorkState() {
        return extensionWorkState;
    }

    public int getOverallPulseCount() {
        return overallPulseCount;
    }

    public int getRadiationSourcePacketStatistics() {
        return radiationSourcePacketStatistics;
    }

    public int getIfDataStatistics() {
        return ifDataStatistics;
    }

    public byte getEquipmentNum() {
        return equipmentNum;
    }

    public byte getLongCableBalancedAttenuationControlOne() {
        return LongCableBalancedAttenuationControlOne;
    }

    public byte getLongCableBalancedAttenuationControlTwo() {
        return LongCableBalancedAttenuationControlTwo;
    }

    public byte getIFAttenuationOne() {
        return IFAttenuationOne;
    }

    public byte getIFAttenuationTwo() {
        return IFAttenuationTwo;
    }

    public byte[] getFrontEndState() {
        return frontEndState;
    }

    public byte[] getKeyState() {
        return keyState;
    }

    public byte[] getStandbyApplication() {
        return standbyApplication;
    }

    public SystemControlBroadcastCMD getSystemControlBroadcastCMD() {
        return systemControlBroadcastCMD;
    }

    private short extensionWorkT;  // 分机工作温度
    private String extensionWorkState; // 分机工作状态
    private int overallPulseCount; // 全脉冲个数统计
    private int radiationSourcePacketStatistics;  // 辐射源数据包统计
    private int ifDataStatistics;  // 中频数据统计
    private byte equipmentNum; // 设备编号
    private byte LongCableBalancedAttenuationControlOne; // 长电缆均衡衰减控制1
    private byte LongCableBalancedAttenuationControlTwo; // 长电缆均衡衰减控制2
    private byte IFAttenuationOne; // 测向1中频衰减
    private byte IFAttenuationTwo;  // 测向2中频衰减
    private byte[] frontEndState;  // 前端状态反馈
    private byte[] keyState; // 关键状态备份
    private byte[] standbyApplication;
    private SystemControlBroadcastCMD systemControlBroadcastCMD;
}

