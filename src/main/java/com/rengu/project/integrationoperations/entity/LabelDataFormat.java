package com.rengu.project.integrationoperations.entity;

import lombok.Data;

@Data
public class LabelDataFormat {
    private String sendNodeNum;  //  发方节点号
    private String receiveNodeNum;  //  收方节点号
    private String receiveCmdCount;  //  指令接收计数
    private String receiveCmdState; //  指令接收状态
    private String equipmentSerialNum;  // 设备编号
    private String frontEndWorkT;  //  前端工作温度
    private String synthesizeOneWorkT; //  综合处理1工作温度
    private String synthesizeTwoWorkT;  //  综合处理2工作温度
    private String extensionTwoWorkT;  // 分机2工作温度
    private String extensionThreeWorkT; //  分机3工作温度
    private String extensionFourWorkT;  //  分机4工作温度
//            private String extensionFiveWorkT;  //  分机5工作温度
//            private String extensionSixWorkT;  //  分机6工作温度
//            private String overallPulseUploadingNum1030;  //  1030全脉冲上传个数
//            private String friendOrFoeRecognitionNum1030; //  1030敌我识别个数
//            private String MFNum1030; //  1030中频个数
//            private String overallPulseNum1090;  //  1090全脉冲个数
//            private String friendOrFoeRecognitionNum1090; //  1090敌我识别个数
//            private String MFNum1090; //  1090中频个数
//            private String overallPulseNum740;  //  740全脉冲上传个数
//            private String friendOrFoeRecognitionNum1464;  // 1464敌我识别个数
//            private String MFNum1464;  //  1464中频个数
//            private String friendOrFoeRecognitionNum1532;  // 1532敌我识别个数
//            private String MFNum1532;  // 1532中频个数
//            private String overallPulseNum1464;  //  1464全脉冲个数
//            private String overallPulseNum1532;  //  1532全脉冲个数
//            private String MFM51030;  //  1030M5中频
//            private String MFM1090;  //  1090M中频
    private String extensionCount;  //  分机计数
    private String masterControlMalfunctionState; // 主控故障状态
//            private String insideStateIP; // 内部指令IP地址相同
//            private String upperComputerSystemControlNum; //  上位机系统控制指令端口号
    private String reconstructionStateFPGA; // FPGA重构状态
    private String reconstructionStateDSP;  //  DSP重构状态
//            private String frontEndState;  // 前端反馈信息
//            private String keyStateInfo;  //  关键状态信息
}
