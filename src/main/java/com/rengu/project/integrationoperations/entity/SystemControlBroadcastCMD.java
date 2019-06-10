package com.rengu.project.integrationoperations.entity;


import lombok.Data;

@Data
public class SystemControlBroadcastCMD {
    private short messagePackageNum; // 信息包序号
    private String timeCode;  // 时间码
    private byte workWay; // 工作方式
    private byte bandwidthChoose; // 带宽选择
    private byte workCycleNum; //  工作周期数
    private byte workCycleLength; // 工作周期长度
    private short centerFrequency; // 中心频率
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
