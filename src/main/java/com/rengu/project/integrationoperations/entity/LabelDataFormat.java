package com.rengu.project.integrationoperations.entity;


public class LabelDataFormat {
    private byte systemWorkState;  //  系统工作状态
    private byte receiveCmdCount;  //  指令接收计数
    private String extensionCount;  // 分机计数
    private short frontEndWorkT;  //  前端工作温度
    private short mainWorkT; //  主控工作温度
    private short detectionWorkT;  //  检测工作温度
    private short extensionTwoWorkT;  // 分机2工作温度
    private short extensionThreeWorkT; //  分机3工作温度
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
    public void setSystemWorkState(byte systemWorkState) {
        this.systemWorkState = systemWorkState;
    }

    public void setReceiveCmdCount(byte receiveCmdCount) {
        this.receiveCmdCount = receiveCmdCount;
    }

    public void setExtensionCount(String extensionCount) {
        this.extensionCount = extensionCount;
    }

    public void setFrontEndWorkT(short frontEndWorkT) {
        this.frontEndWorkT = frontEndWorkT;
    }

    public void setMainWorkT(short mainWorkT) {
        this.mainWorkT = mainWorkT;
    }

    public void setDetectionWorkT(short detectionWorkT) {
        this.detectionWorkT = detectionWorkT;
    }

    public void setExtensionTwoWorkT(short extensionTwoWorkT) {
        this.extensionTwoWorkT = extensionTwoWorkT;
    }

    public void setExtensionThreeWorkT(short extensionThreeWorkT) {
        this.extensionThreeWorkT = extensionThreeWorkT;
    }

    public void setExtensionFourWorkT(short extensionFourWorkT) {
        this.extensionFourWorkT = extensionFourWorkT;
    }

    public void setExtensionFiveWorkT(short extensionFiveWorkT) {
        this.extensionFiveWorkT = extensionFiveWorkT;
    }

    public void setExtensionSixWorkT(short extensionSixWorkT) {
        this.extensionSixWorkT = extensionSixWorkT;
    }

    public void setPDW740(int PDW740) {
        this.PDW740 = PDW740;
    }

    public void setPDW837_5(int PDW837_5) {
        this.PDW837_5 = PDW837_5;
    }

    public void setPDW1030(int PDW1030) {
        this.PDW1030 = PDW1030;
    }

    public void setPDW1059(int PDW1059) {
        this.PDW1059 = PDW1059;
    }

    public void setPDW1090(int PDW1090) {
        this.PDW1090 = PDW1090;
    }

    public void setPDW1464(int PDW1464) {
        this.PDW1464 = PDW1464;
    }

    public void setPDW1532(int PDW1532) {
        this.PDW1532 = PDW1532;
    }

    public void setIFF740(int IFF740) {
        this.IFF740 = IFF740;
    }

    public void setIFF837_5(int IFF837_5) {
        this.IFF837_5 = IFF837_5;
    }

    public void setIFF1030(int IFF1030) {
        this.IFF1030 = IFF1030;
    }

    public void setIFF1090(int IFF1090) {
        this.IFF1090 = IFF1090;
    }

    public void setIFF1464(int IFF1464) {
        this.IFF1464 = IFF1464;
    }

    public void setIFF1532(int IFF1532) {
        this.IFF1532 = IFF1532;
    }

    public void setIF(byte[] IF) {
        this.IF = IF;
    }

    public void setM51030(int m51030) {
        M51030 = m51030;
    }

    public void setM51090(int m51090) {
        M51090 = m51090;
    }

    public void setM5MF1030(int m5MF1030) {
        M5MF1030 = m5MF1030;
    }

    public void setM5MF1030S(int m5MF1030S) {
        M5MF1030S = m5MF1030S;
    }

    public void setPowerState(byte[] powerState) {
        this.powerState = powerState;
    }

    public void setMainFPGA1Versions(byte mainFPGA1Versions) {
        this.mainFPGA1Versions = mainFPGA1Versions;
    }

    public void setMainFPGA2Versions(byte mainFPGA2Versions) {
        this.mainFPGA2Versions = mainFPGA2Versions;
    }

    public void setMainDSPVersions(byte mainDSPVersions) {
        this.mainDSPVersions = mainDSPVersions;
    }

    public void setDetectionTwoFPGA1(byte detectionTwoFPGA1) {
        this.detectionTwoFPGA1 = detectionTwoFPGA1;
    }

    public void setDetectionTwoFPGA2(byte detectionTwoFPGA2) {
        this.detectionTwoFPGA2 = detectionTwoFPGA2;
    }

    public void setDetectionTwoDSP(byte detectionTwoDSP) {
        this.detectionTwoDSP = detectionTwoDSP;
    }

    public void setVersions(byte[] versions) {
        this.versions = versions;
    }

    public void setMainIPAddress(int mainIPAddress) {
        this.mainIPAddress = mainIPAddress;
    }

    public void setDetectionIPAddress(int detectionIPAddress) {
        this.detectionIPAddress = detectionIPAddress;
    }

    public void setGPRS2IPAddress(int GPRS2IPAddress) {
        this.GPRS2IPAddress = GPRS2IPAddress;
    }

    public void setGPRS3IPAddress(int GPRS3IPAddress) {
        this.GPRS3IPAddress = GPRS3IPAddress;
    }

    public void setUserCMDPort(short userCMDPort) {
        this.userCMDPort = userCMDPort;
    }

    public void setInteriorCMDPort(short interiorCMDPort) {
        this.interiorCMDPort = interiorCMDPort;
    }

    public void setBackup1(int backup1) {
        this.backup1 = backup1;
    }

    public void setMainControlAddress(byte[] mainControlAddress) {
        this.mainControlAddress = mainControlAddress;
    }

    public void setDetectionMACAddress(byte[] detectionMACAddress) {
        this.detectionMACAddress = detectionMACAddress;
    }

    public void setGPRSTwoMACAddress(byte[] GPRSTwoMACAddress) {
        this.GPRSTwoMACAddress = GPRSTwoMACAddress;
    }

    public void setGPRSThreeMACAddress(byte[] GPRSThreeMACAddress) {
        this.GPRSThreeMACAddress = GPRSThreeMACAddress;
    }

    public void setBackup2(byte[] backup2) {
        this.backup2 = backup2;
    }

    public void setUpperIP(int upperIP) {
        UpperIP = upperIP;
    }

    public void setBackup3(byte[] backup3) {
        this.backup3 = backup3;
    }

    public void setTagPort(short tagPort) {
        this.tagPort = tagPort;
    }

    public void setDataPort1(short dataPort1) {
        this.dataPort1 = dataPort1;
    }

    public void setDataPort2(short dataPort2) {
        this.dataPort2 = dataPort2;
    }

    public void setDataPort3(short dataPort3) {
        this.dataPort3 = dataPort3;
    }

    public void setInteriorStateIP(int interiorStateIP) {
        this.interiorStateIP = interiorStateIP;
    }

    public void setInteriorStatePortIP(short interiorStatePortIP) {
        this.interiorStatePortIP = interiorStatePortIP;
    }

    public void setBackup4(short backup4) {
        this.backup4 = backup4;
    }

    public void setMCULoad(String MCULoad) {
        this.MCULoad = MCULoad;
    }

    public void setBackup5(byte[] backup5) {
        this.backup5 = backup5;
    }

    private short extensionFourWorkT;  //  分机4工作温度
    private short extensionFiveWorkT;  //  分机5工作温度

    public byte getSystemWorkState() {
        return systemWorkState;
    }

    public byte getReceiveCmdCount() {
        return receiveCmdCount;
    }

    public String getExtensionCount() {
        return extensionCount;
    }

    public short getFrontEndWorkT() {
        return frontEndWorkT;
    }

    public short getMainWorkT() {
        return mainWorkT;
    }

    public short getDetectionWorkT() {
        return detectionWorkT;
    }

    public short getExtensionTwoWorkT() {
        return extensionTwoWorkT;
    }

    public short getExtensionThreeWorkT() {
        return extensionThreeWorkT;
    }

    public short getExtensionFourWorkT() {
        return extensionFourWorkT;
    }

    public short getExtensionFiveWorkT() {
        return extensionFiveWorkT;
    }

    public short getExtensionSixWorkT() {
        return extensionSixWorkT;
    }

    public int getPDW740() {
        return PDW740;
    }

    public int getPDW837_5() {
        return PDW837_5;
    }

    public int getPDW1030() {
        return PDW1030;
    }

    public int getPDW1059() {
        return PDW1059;
    }

    public int getPDW1090() {
        return PDW1090;
    }

    public int getPDW1464() {
        return PDW1464;
    }

    public int getPDW1532() {
        return PDW1532;
    }

    public int getIFF740() {
        return IFF740;
    }

    public int getIFF837_5() {
        return IFF837_5;
    }

    public int getIFF1030() {
        return IFF1030;
    }

    public int getIFF1090() {
        return IFF1090;
    }

    public int getIFF1464() {
        return IFF1464;
    }

    public int getIFF1532() {
        return IFF1532;
    }

    public byte[] getIF() {
        return IF;
    }

    public int getM51030() {
        return M51030;
    }

    public int getM51090() {
        return M51090;
    }

    public int getM5MF1030() {
        return M5MF1030;
    }

    public int getM5MF1030S() {
        return M5MF1030S;
    }

    public byte[] getPowerState() {
        return powerState;
    }

    public byte getMainFPGA1Versions() {
        return mainFPGA1Versions;
    }

    public byte getMainFPGA2Versions() {
        return mainFPGA2Versions;
    }

    public byte getMainDSPVersions() {
        return mainDSPVersions;
    }

    public byte getDetectionTwoFPGA1() {
        return detectionTwoFPGA1;
    }

    public byte getDetectionTwoFPGA2() {
        return detectionTwoFPGA2;
    }

    public byte getDetectionTwoDSP() {
        return detectionTwoDSP;
    }

    public byte[] getVersions() {
        return versions;
    }

    public int getMainIPAddress() {
        return mainIPAddress;
    }

    public int getDetectionIPAddress() {
        return detectionIPAddress;
    }

    public int getGPRS2IPAddress() {
        return GPRS2IPAddress;
    }

    public int getGPRS3IPAddress() {
        return GPRS3IPAddress;
    }

    public short getUserCMDPort() {
        return userCMDPort;
    }

    public short getInteriorCMDPort() {
        return interiorCMDPort;
    }

    public int getBackup1() {
        return backup1;
    }

    public byte[] getMainControlAddress() {
        return mainControlAddress;
    }

    public byte[] getDetectionMACAddress() {
        return detectionMACAddress;
    }

    public byte[] getGPRSTwoMACAddress() {
        return GPRSTwoMACAddress;
    }

    public byte[] getGPRSThreeMACAddress() {
        return GPRSThreeMACAddress;
    }

    public byte[] getBackup2() {
        return backup2;
    }

    public int getUpperIP() {
        return UpperIP;
    }

    public byte[] getBackup3() {
        return backup3;
    }

    public short getTagPort() {
        return tagPort;
    }

    public short getDataPort1() {
        return dataPort1;
    }

    public short getDataPort2() {
        return dataPort2;
    }

    public short getDataPort3() {
        return dataPort3;
    }

    public int getInteriorStateIP() {
        return interiorStateIP;
    }

    public short getInteriorStatePortIP() {
        return interiorStatePortIP;
    }

    public short getBackup4() {
        return backup4;
    }

    public String getMCULoad() {
        return MCULoad;
    }

    public byte[] getBackup5() {
        return backup5;
    }


}
