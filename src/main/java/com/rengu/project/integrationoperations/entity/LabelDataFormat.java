package com.rengu.project.integrationoperations.entity;

import lombok.Data;

@Data
public class LabelDataFormat {
    private byte sendNodeNum;  //  发方节点号
    private byte receiveNodeNum;  //  收方节点号
    private short receiveCmdCount;  //  指令接收计数
    private short receiveCmdState; //  指令接收状态
    private short equipmentSerialNum;  // 设备编号
    private short frontEndWorkT;  //  前端工作温度
    private short synthesizeOneWorkT; //  综合处理1工作温度
    private short synthesizeTwoWorkT;  //  综合处理2工作温度
    private short extensionTwoWorkT;  // 分机2工作温度
    private short extensionThreeWorkT; //  分机3工作温度
    private short extensionFourWorkT;  //  分机4工作温度
    private short extensionFiveWorkT;  //  分机5工作温度
    private short extensionSixWorkT;  //  分机6工作温度
    private int overallPulseUploadingNum1030;  //  1030全脉冲上传个数
    private int friendOrFoeRecognitionNum1030; //  1030敌我识别个数
    private int MFNum1030; //  1030中频个数
    private int overallPulseNum1090;  //  1090全脉冲个数
    private int friendOrFoeRecognitionNum1090; //  1090敌我识别个数
    private int MFNum1090; //  1090中频个数
    private int overallPulseNum740;  //  740全脉冲个数
    private int friendOrFoeRecognitionNum1464;  // 1464敌我识别个数
    private int MFNum1464;  //  1464中频个数
    private int friendOrFoeRecognitionNum1532;  // 1532敌我识别个数
    private int MFNum1532;  // 1532中频个数
    private int overallPulseNum1464;  //  1464全脉冲个数
    private int overallPulseNum1532;  //  1532全脉冲个数
    private int MFM51030;  //  1030M5中频
    private int MFM1090;  //  1090M中频
    private String mainControlFPGA1; //  主控FPGA1计数
    private String mainControlFPGA2; //  主控FPGA2计数
    private String mainControlDSP; //  主控DSP计数
    private String detectionOneFPGA1; //  检测1 FPGA1计数
    private String detectionOneFPGA2; //  检测1 FPGA2计数
    private String detectionOneDSP; //   检测1 DSP计数
    private String detectionTwoFPGA1; //  检测2 FPGA1计数
    private String detectionTwoFPGA2; //  检测2 FPGA2计数
    private String detectionTwoDSP; //  检测2 DSP计数
    private String detectionThreeFPGA1; //  检测3 FPGA1计数
    private String detectionThreeFPGA2; //  检测3 FPGA2计数
    private String detectionThreeDSP; //  检测3 DSP计数
    private String detectionFourFPGA1; //  检测4 FPGA1计数
    private String detectionFourFPGA2; //  检测4 FPGA2计数
    private String noteTheNumber;  // 注数标记
    private String externalSecPulseAbnormalSign; // 外部秒脉冲异常标记
    private String pulsePosition;  // 脉冲位置
    private String DDR3_1;
    private String DDR3_2;
    private String DDR2_1;
    private String DDR2_2;
    private String signalDetection1; // 信号检测自检1
    private String signalDetection2; // 信号检测自检2
    private byte extensionMalfunctionState1; // 分机故障状态1
    private byte extensionMalfunctionState2; // 分机故障状态2
//    private byte extensionMalfunctionState3; // 分机故障状态3
    private byte extensionMalfunctionState4; // 分机故障状态4
    private int mainControlIP; // 主控IP地址
    private int GPRSOneIP;  // 数传1IP地址
    private short mainControlDSPPort; // 主控DSP端口号
    private short GPRSOneDSPPort;  // 数传1DSP端口号
    private byte[] mainControlHost; // 主控MAC地址
    private byte[] GPRSOneMACHost; // 数传1MAC地址
    private int mainControlGateway; // 主控网关
    private int GPRSOneGateway; // 数传1网关
    private int mainControlUpperIP; // 主控上位机IP
    private int GPRSOneUpperIP; // 数传1上位机IP
    private int interiorStateIP; // 内部状态IP
    private short UpperSysControlCMDPort; // 上位机系统控制指令端口号
    private short interiorCMDPort; // 内部指令端口
    private int GPRSReconsitutionIP; // 上位机重构IP
    private short GPRSReconsitutionPort; // 上位机重构端口
    private short DSPInteriorCMDPort; // DSP内部指令端口
    private byte[] FPGAReconsitutionState; // FPGA重构状态
    private int DSPReconsitutionState; // DSP重构状态
    private int DSPReconsitutionIdentification; // DSP重构标识
    private int IPReconsitutionIdentification; // IP重构标识
    private byte[] frontEndState;  // 前端状态
    private byte[] keyStateInfo;  //  关键状态信息
}
