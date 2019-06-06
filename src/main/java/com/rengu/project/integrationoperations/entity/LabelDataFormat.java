package com.rengu.project.integrationoperations.entity;

import lombok.Data;

@Data
public class LabelDataFormat {
    private byte systemWorkState;  //  系统工作状态
    private byte receiveCmdCount;  //  指令接收计数
    private String extensionCount;  // 分机计数
    private short frontEndWorkT;  //  前端工作温度
    private short mainWorkT; //  主控工作温度
    private short detectionWorkT;  //  检测工作温度
    private short extensionTwoWorkT;  // 分机2工作温度
    private short extensionThreeWorkT; //  分机3工作温度
    private short extensionFourWorkT;  //  分机4工作温度
    private short extensionFiveWorkT;  //  分机5工作温度
    private short extensionSixWorkT;  //  分机6工作温度
    private int PDW740;  //  740PDW个数
    private int PDW837_5; //  837.5PDW个数
    private int PDW1030; //  1030PDW个数
    private int PDW1059;  //  1059PDW个数
    private int PDW1090; //  1090PDW
    private int PDW1464; //  1464PDW个数
    private int PDW1532;  //  PDW1532
    private int IFF740;  // 740IFF
    private int IFF837_5;  //  837.5IFF
    private int IFF1030;  // 1030IFF
    private int IFF1090;  // 1090IFF
    private int IFF1464;  // 1464IFF
    private int IFF1532;  // 1532IFF
    private byte[] IF;  //  IF个数
    private int M51030;  // 1030M5个数
    private int M51090; //  1090M5
    private int M5MF1030; //  1030M5中频个数
    private int M5MF1030S; // 重复的1030M5中频个数
    private byte[] powerState; //  电源状态
    private byte mainFPGA1Versions; // 主控FPGA1版本号
    private byte mainFPGA2Versions; //  主控FPGA2版本号
    private byte mainDSPVersions; //  主控DSP版本号
    private byte detectionTwoFPGA1; //  检测 FPGA1版本号
    private byte detectionTwoFPGA2; //  检测 FPGA2版本号
    private byte detectionTwoDSP; //  检测 DSP版本号
    private byte[] versions; // 版本号
    private int mainIPAddress; //  主控IP地址
    private int detectionIPAddress; //  检测IP地址
    private int GPRS2IPAddress; //  数传2IP地址
    private int GPRS3IPAddress; //  数传3IP地址
    private short userCMDPort;  // 用户指令端口号
    private short interiorCMDPort; // 内部指令端口号
    private int backup1;  // 备份
    private byte[] mainControlAddress; // 主控MAC地址
    private byte[] detectionMACAddress; // 检测MAC地址
    private byte[] GPRSTwoMACAddress; // 数传2MAC地址
    private byte[] GPRSThreeMACAddress; // 数传3MAC地址
    private byte[] backup2; // 备份
    private int UpperIP; // 上位机IP
    private byte[] backup3; // 备份
    private short tagPort; // 标签包端口号
    private short dataPort1; // 数据端口号1
    private short dataPort2; // 数据端口号2
    private short dataPort3; // 数据端口号3
    private int interiorStateIP; // 内部状态IP
    private short interiorStatePortIP; // 内部状态端口号
    private short backup4; // 备份
    private String MCULoad;  // MCU加载片区
    private byte[] backup5; // 备份
}
