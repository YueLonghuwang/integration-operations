package com.rengu.project.integrationoperations.entity;


import lombok.Data;

@Data
public class SystemControlBroadcastCMD {
    private String timeCode;  // 时间码
    private String workWay; // 工作方式
    private String bandwidthChoose; // 带宽选择
    private String workCycleLength; // 工作周期长度
    private String centerFrequency; // 中心频率
    private String directionFindingAntennaChoose; // 测向天线选择
    private String scoutAntennaChoose; // 侦察天线选择
    private String pulseScreenMinimumFrequency; // 脉冲筛选最小频率
    private String pulseScreenMaximumFrequency; // 脉冲筛选最大频率
    private String pulseScreenMinimumRange; // 脉冲筛选最小幅度
    private String pulseScreenMaximumRange;// 脉冲筛选最大幅度
    private String pulseScreenMinimumPulseWidth; // 脉冲筛选最小脉宽
    private String pulseScreenMaximumPulseWidth; // 脉冲筛选最大脉宽
    private String withinThePulseGuidanceSwitch; // 脉内引导批次号开关
    private String extensionControl; // 分机控制
    private String equipmentSerialNum;  // 设备编号
    private String detectionThresholdAdjustment; // 检测门限调节
}
