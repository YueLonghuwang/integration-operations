package com.rengu.project.integrationoperations.entity;

import lombok.Data;

@Data
public class LabelPackageInfo {
    private byte[] GPSData; // GPS数据
    private byte sendNodeNum;  //  发方节点号
    private byte receiveNodeNum;  //  收方节点号
    private short feedbackCmdSerialNum; //  反馈指令序号
    private String receiveCmdState; //  指令接收状态
    private short workNum; // 工作编号
    private short frontEndWorkT;  //  前端工作温度
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

