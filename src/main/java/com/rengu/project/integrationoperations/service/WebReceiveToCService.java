package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.util.SocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * 接收c++发送数据，进行解析
 * author : yaojiahao
 * Date: 2019/7/8 11:19
 **/

@Service
@Slf4j
public class WebReceiveToCService {
    private final HostRepository hostRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebReceiveToCService(HostRepository hostRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.hostRepository = hostRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // 储存或更新当前连接服务端的IP地址
    public void allHost(String hosts) {
        List<AllHost> allHostList = hostRepository.findAll();
        int size = hostRepository.findByHostNotLike("无").size();
        for (AllHost allHost : allHostList) {
            if (allHost.getHost().equals("无")) {
                if (!hasHostIP(hosts)) {
                    allHost.setHost(hosts);
                    Map<Object, Object> map = new HashMap<>();
                    map.put("device", size + 1);
                    map.put("message", "一台新的设备已入库");
                    hostRepository.save(allHost);
                    simpMessagingTemplate.convertAndSend("/deviceConnect/send", map);
                    return;
                }
            }
        }
        if (!hasHostIP(hosts)) {
            AllHost allHosts = new AllHost();
            allHosts.setHost(hosts);
            AllHost allHost = hostRepository.findMaxByNum();
            allHosts.setNum(allHost.getNum() + 1);
            hostRepository.save(allHosts);
        }
    }

    // 解析报文固定信息
    @Async
    public Map<String, Number> receiveFixedInformation(ByteBuffer byteBuffer) {
        Map<String, Number> map = new HashMap<>();
        map.put("header", byteBuffer.getInt(0)); // 报文头
        map.put("dataLength", byteBuffer.getInt(4)); // 当前包数据长度
        map.put("targetHost", byteBuffer.getShort(8)); // 目的地址
        map.put("sourceHost", byteBuffer.getShort(10)); // 源地址
        map.put("regionID", byteBuffer.get(12)); // 域ID
        map.put("themeID", byteBuffer.get(13)); // 主题ID
        map.put("messageCategory", byteBuffer.getShort(14)); // 信息类别
        map.put("transmitDate", byteBuffer.getLong(16)); // 发报日期时间
        map.put("serialNumber", byteBuffer.getInt(24)); // 序列号
        map.put("bagTotal", byteBuffer.getInt(28)); // 包总数
        map.put("currentBagNo", byteBuffer.getInt(32)); // 当前包号
        map.put("dataTotalLength", byteBuffer.getInt(36)); // 数据总长度
        map.put("versionNumber", byteBuffer.getShort(40)); // 版本号
        map.put("backups1", byteBuffer.getInt(42)); // 保留字段
        map.put("backups2", byteBuffer.getShort(46)); // 保留字段
        return map;
    }

    /**
     * 区分每一个信息包 设备1
     */
    @Async
    public void receiveSocketHandler1(ByteBuffer byteBuffer, String host) {
        short messageCategorys = byteBuffer.getShort(14);
        int messageCategory = Integer.parseInt(Integer.toHexString(messageCategorys));
        String messageCategoryss = String.valueOf(Integer.parseInt(Integer.toHexString(messageCategorys)));
        switch (messageCategory) {
            case 3001:
                receiveHeartbeatCMD(byteBuffer, host);
                break;
            case 3101:
                uploadHeartBeatMessage(byteBuffer, host);
                break;
            case 3102:
                uploadSelfInspectionResult(byteBuffer, host);
                break;
            case 3105:
                uploadSoftwareVersionMessage(byteBuffer, host);
                break;
            case 3106:
                uploadDeviceNetWorkParamMessage(byteBuffer, host);
                break;
            case 3107:
                uploadRadarSubSystemWorkStatusMessage(byteBuffer, host);
                break;
            case 3178:
                uploadVersionNumberMessage(byteBuffer, host);
                break;
            default:
//                test(byteBuffer, host);
                break;
        }
    }

    /**
     * 区分每一个信息包 设备2
     */
    @Async
    public void receiveSocketHandler2(ByteBuffer byteBuffer, String host) {
        short messageCategorys = byteBuffer.getShort(14);
        int messageCategory = Integer.parseInt(Integer.toHexString(messageCategorys));
        switch (messageCategory) {
            case 3001:
                receiveHeartbeatCMD(byteBuffer, host);
                break;
            case 3101:
                uploadHeartBeatMessage(byteBuffer, host);
                break;
            case 3102:
                uploadSelfInspectionResult(byteBuffer, host);
                break;
            case 3105:
                uploadSoftwareVersionMessage(byteBuffer, host);
                break;
            case 3106:
                uploadDeviceNetWorkParamMessage(byteBuffer, host);
                break;
            case 3107:
                uploadRadarSubSystemWorkStatusMessage(byteBuffer, host);
                break;
            default:
//                test(byteBuffer, host);
                break;
        }
    }

    /**
     * 区分每一个信息包 设备3
     */
    @Async
    public void receiveSocketHandler3(ByteBuffer byteBuffer, String host) {
        short messageCategorys = byteBuffer.getShort(14);
        int messageCategory = Integer.parseInt(Integer.toHexString(messageCategorys));
        switch (messageCategory) {
            case 3001:
                receiveHeartbeatCMD(byteBuffer, host);
                break;
            case 3101:
                uploadHeartBeatMessage(byteBuffer, host);
                break;
            case 3102:
                uploadSelfInspectionResult(byteBuffer, host);
                break;
            case 3105:
                uploadSoftwareVersionMessage(byteBuffer, host);
                break;
            case 3106:
                uploadDeviceNetWorkParamMessage(byteBuffer, host);
                break;
            case 3107:
                uploadRadarSubSystemWorkStatusMessage(byteBuffer, host);
                break;
            default:
//                test(byteBuffer, host);
                break;
        }
    }

    private StringBuilder getBit(byte[] mcuLoad) {
        StringBuilder stringBuilders = new StringBuilder();
        for (byte b : mcuLoad) {
            String extensionCount = Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
            stringBuilders.append(extensionCount.substring(6));
            stringBuilders.append(extensionCount, 4, 6);
            stringBuilders.append(extensionCount, 2, 4);
            stringBuilders.append(extensionCount, 0, 2);
        }
        return stringBuilders;
    }


    /**
     * 解析铁塔敌我报文 (这是旧版本的报文 目前不确定需不需要接收)
     */
    public void reciveAndConvertIronFriendOrFoe(byte[] bytes, String host) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(650);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        // 判断包
        short header = (short) SocketConfig.BinaryToDecimal(byteBuffer.getShort());
        short dataType = byteBuffer.getShort(2);
        int dataLength = byteBuffer.getInt(4);
        byte[] systemBytes = new byte[64];
        byteBuffer.get(systemBytes, 8, 64);
        systemControlBroadcastCMDs(systemBytes);
        byte[] dataGPS = new byte[64];
        byteBuffer.get(dataGPS, 72, 64);
        LabelDataFormat labelDataFormat = new LabelDataFormat();
        labelDataFormat.setSystemWorkState(byteBuffer.get(136));
        labelDataFormat.setReceiveCmdCount(byteBuffer.get(137));
        // 分机计数
        byte[] extensionCountByte = new byte[6];
        byteBuffer.get(extensionCountByte, 138, 6);
        StringBuilder stringBuilder = getBit(extensionCountByte);
        labelDataFormat.setExtensionCount(stringBuilder.toString());
        labelDataFormat.setFrontEndWorkT(byteBuffer.getShort(144));
        labelDataFormat.setMainWorkT(byteBuffer.getShort(146));
        labelDataFormat.setDetectionWorkT(byteBuffer.getShort(148));
        labelDataFormat.setExtensionTwoWorkT(byteBuffer.getShort(150));
        labelDataFormat.setExtensionThreeWorkT(byteBuffer.getShort(152));
        labelDataFormat.setExtensionFourWorkT(byteBuffer.getShort(154));
        labelDataFormat.setExtensionFiveWorkT(byteBuffer.getShort(156));
        labelDataFormat.setExtensionSixWorkT(byteBuffer.getShort(158));
        labelDataFormat.setPDW740(byteBuffer.getInt(160));
        labelDataFormat.setPDW837_5(byteBuffer.getInt(164));
        labelDataFormat.setPDW1030(byteBuffer.getInt(168));
        labelDataFormat.setPDW1059(byteBuffer.getInt(172));
        labelDataFormat.setPDW1090(byteBuffer.getInt(176));
        labelDataFormat.setPDW1464(byteBuffer.getInt(180));
        labelDataFormat.setPDW1532(byteBuffer.getInt(184));
        labelDataFormat.setIFF740(byteBuffer.getInt(188));
        labelDataFormat.setIFF837_5(byteBuffer.getInt(192));
        labelDataFormat.setIFF1030(byteBuffer.getInt(196));
        labelDataFormat.setIFF1090(byteBuffer.getInt(200));
        labelDataFormat.setIFF1464(byteBuffer.getInt(204));
        labelDataFormat.setIFF1532(byteBuffer.getInt(208));
        //  IF个数
        byte[] ifCount = new byte[28];
        byteBuffer.get(ifCount, 212, 28);
        labelDataFormat.setIF(ifCount);
        labelDataFormat.setM51030(byteBuffer.getInt(240));
        labelDataFormat.setM51090(byteBuffer.getInt(244));
        labelDataFormat.setM5MF1030(byteBuffer.getInt(248));
        labelDataFormat.setM5MF1030S(byteBuffer.getInt(252));
        //  电源状态
        byte[] powerState = new byte[154];
        byteBuffer.get(powerState, 256, 154);
        labelDataFormat.setPowerState(powerState);
        labelDataFormat.setMainFPGA1Versions(byteBuffer.get(410));
        labelDataFormat.setMainFPGA2Versions(byteBuffer.get(411));
        labelDataFormat.setMainDSPVersions(byteBuffer.get(412));
        labelDataFormat.setDetectionTwoFPGA1(byteBuffer.get(413));
        labelDataFormat.setDetectionTwoFPGA2(byteBuffer.get(414));
        labelDataFormat.setDetectionTwoDSP(byteBuffer.get(415));
        //  版本号
        byte[] versions = new byte[16];
        byteBuffer.get(versions, 416, 16);
        labelDataFormat.setVersions(versions);
        labelDataFormat.setMainIPAddress(byteBuffer.getInt(432));
        labelDataFormat.setDetectionIPAddress(byteBuffer.getInt(436));
        labelDataFormat.setGPRS2IPAddress(byteBuffer.getInt(440));
        labelDataFormat.setGPRS3IPAddress(byteBuffer.getInt(444));
        labelDataFormat.setUserCMDPort(byteBuffer.getShort(448));
        labelDataFormat.setInteriorCMDPort(byteBuffer.getShort(450));
        labelDataFormat.setBackup1(byteBuffer.getInt(452));
        // 主控MAC地址
        byte[] mainControlAddress = new byte[6];
        byteBuffer.get(mainControlAddress, 456, 6);
        labelDataFormat.setMainControlAddress(mainControlAddress);
        // 检测MAC地址
        byte[] detectionMACAddress = new byte[6];
        byteBuffer.get(detectionMACAddress, 462, 6);
        labelDataFormat.setDetectionMACAddress(detectionMACAddress);
        // 数传2MAC地址
        byte[] GPRSTwoMACAddress = new byte[6];
        byteBuffer.get(GPRSTwoMACAddress, 468, 6);
        labelDataFormat.setGPRSTwoMACAddress(GPRSTwoMACAddress);
        // 数传3MAC地址
        byte[] GPRSThreeMACAddress = new byte[6];
        byteBuffer.get(GPRSThreeMACAddress, 474, 6);
        labelDataFormat.setGPRSThreeMACAddress(GPRSThreeMACAddress);
        // 备份
        byte[] backup2 = new byte[16];
        byteBuffer.get(backup2, 480, 16);
        labelDataFormat.setBackup2(backup2);
        labelDataFormat.setUpperIP(byteBuffer.getInt(496));
        // 备份
        byte[] backup3 = new byte[12];
        byteBuffer.get(backup3, 500, 12);
        labelDataFormat.setBackup3(backup3);
        labelDataFormat.setTagPort(byteBuffer.getShort(512));
        labelDataFormat.setDataPort1(byteBuffer.getShort(514));
        labelDataFormat.setDataPort2(byteBuffer.getShort(516));
        labelDataFormat.setDataPort3(byteBuffer.getShort(518));
        labelDataFormat.setInteriorStateIP(byteBuffer.getInt(520));
        labelDataFormat.setInteriorStatePortIP(byteBuffer.getShort(524));
        labelDataFormat.setBackup4(byteBuffer.getShort(526));
        //  MCU加载片区
        byte[] mcuLoad = new byte[8];
        byteBuffer.get(mcuLoad, 528, 8);
        StringBuilder stringBuilders = getBit(mcuLoad);
        labelDataFormat.setMCULoad(stringBuilders.toString());
        //  备份
        byte[] backup5 = new byte[64];
        byteBuffer.get(backup5, 536, 64);
        labelDataFormat.setBackup5(backup5);
        int end = byteBuffer.getInt(600);
    }

    /**
     * 解析铁塔雷达报文 (这是旧版本的报文 目前不确定需不需要接收)
     */
    public void reciveAndConvertIronRadar(byte[] bytes, String host) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(500);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        // 判断包
        short header = (short) SocketConfig.BinaryToDecimal(byteBuffer.getShort());
        short dataType = byteBuffer.getShort(3);
        int dataLength = byteBuffer.getInt(7);
        LabelPackageInfo labelPackageInfo = new LabelPackageInfo();
        byte[] bytes1 = new byte[64];
        byteBuffer.get(bytes1, 11, 64);
        SystemControlBroadcastCMD systemControlBroadcastCMD = systemControlBroadcastCMDs(bytes1);
        labelPackageInfo.setSystemControlBroadcastCMD(systemControlBroadcastCMD);
        byte[] bytes2 = new byte[64];
        byteBuffer.get(bytes2, 75, 64);
        labelPackageInfo.setGPSData(bytes2);
        labelPackageInfo.setSendNodeNum(byteBuffer.get(139));
        labelPackageInfo.setReceiveNodeNum(byteBuffer.get(140));
        labelPackageInfo.setFeedbackCmdSerialNum(byteBuffer.get(141));
        byte[] bytes3 = new byte[2];
        byteBuffer.get(bytes3, 143, 2);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes3) {
            String tString = Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
            stringBuilder.append(tString);
        }
        labelPackageInfo.setReceiveCmdState(stringBuilder.toString());
        labelPackageInfo.setWorkNum(byteBuffer.getShort(145));
        labelPackageInfo.setFrontEndWorkT(byteBuffer.getShort(147));
        labelPackageInfo.setExtensionWorkT(byteBuffer.getShort(149));
        // 分机工作状态
        byte[] bytes4 = new byte[8];
        StringBuilder stringBuilders = new StringBuilder();
        byteBuffer.get(bytes4, 151, 8);
        for (byte b : bytes4) {
            String tString = Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
            stringBuilders.append(tString);
        }
        labelPackageInfo.setExtensionWorkState(stringBuilders.toString());
        labelPackageInfo.setOverallPulseCount(byteBuffer.getInt(159));
        labelPackageInfo.setRadiationSourcePacketStatistics(byteBuffer.getInt(163));
        labelPackageInfo.setIfDataStatistics(byteBuffer.getInt(167));
        labelPackageInfo.setEquipmentNum(byteBuffer.get(171));
        labelPackageInfo.setLongCableBalancedAttenuationControlOne(byteBuffer.get(179));
        labelPackageInfo.setLongCableBalancedAttenuationControlTwo(byteBuffer.get(180));
        labelPackageInfo.setIFAttenuationOne(byteBuffer.get(181));
        labelPackageInfo.setIFAttenuationTwo(byteBuffer.get(182));
        byte[] bytes5 = new byte[32];
        byteBuffer.get(bytes5, 183, 32);
        labelPackageInfo.setFrontEndState(bytes5);
        byte[] bytes6 = new byte[128];
        byteBuffer.get(bytes6, 215, 128);
        labelPackageInfo.setKeyState(bytes6);
        byte[] bytes7 = new byte[128];
        byteBuffer.get(bytes7, 343, 128);
        labelPackageInfo.setStandbyApplication(bytes7);
        byte[] bytes8 = SocketConfig.hexToByte(SocketConfig.end);
    }

    /**
     * 测试数据
     */
    /*public void test(ByteBuffer byteBuffer1, String host) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteBuffer1.array());
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        int a = byteBuffer.getInt(0);
        byteBuffer.position(4);
        byte[] bytes = new byte[3];
        byteBuffer.get(bytes);
        System.out.println(a);
        for (byte b : bytes) {
            System.out.println(b);
        }
        byteBuffer.clear();
    }*/

    /**
     * 3.4.6.2 心跳指令
     */
    private void receiveHeartbeatCMD(ByteBuffer byteBuffer, String host) {
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        Map<String, Object> map1 = new HashMap<>();
//        map.put("messageLength",byteBuffer.getInt(48));// 信息长度
//        map1.put("taskFlowNo", byteBuffer.getLong(52));// 任务流水号
        map1.put("heartbeat", byteBuffer.get(60));// 心跳
//        map.put("verify", byteBuffer.getInt(61));// 校验和
//        map.put("messageEnd", byteBuffer.getInt(65));// 结尾
        map1.put("host", host);
        simpMessagingTemplate.convertAndSend("/receiveHeartbeatCMD/sendToHeartBeat", new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
    }

    /**
     * 3.4.6.9 上传心跳信息
     */
    private void uploadHeartBeatMessage(ByteBuffer byteBuffer1, String host) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteBuffer1.array());
        Map<String, Number> mapFixation = receiveFixedInformation(byteBuffer);
//        int messageLength = byteBuffer.getInt(48);
//        long taskFlowNo = byteBuffer.getLong(52);
        byte systemWorkStatus = byteBuffer.get(60);
        Map<String, String> map1 = workStatus(systemWorkStatus); // 解析系统工作状态
        byteBuffer.position(61);
        byte[] backups = new byte[3];
        byteBuffer.get(backups);
        int verify = byteBuffer.getInt(64); // 校验和
        int messageEnd = byteBuffer.getInt(68); // 报文尾
        map1.put("host", host);
        // 根据设备发送指定信息
        simpMessagingTemplate.convertAndSend("/uploadHeartBeatMessage/send", new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
    }

    /**
     * 3.4.6.10 上报自检结果
     */
    private void uploadSelfInspectionResult(ByteBuffer byteBuffer, String host) {
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        byte systemWorkStatus = byteBuffer.get(60);
        Map<String, String> map1 = workStatus(systemWorkStatus); // 解析系统工作状态
        int verify = byteBuffer.getInt(64); // 校验和
        int messageEnd = byteBuffer.getInt(68); // 报文尾
        map1.put("host", host);
        // 根据设备发送指定信息
        simpMessagingTemplate.convertAndSend("/uploadSelfInspectionResult/send", new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
    }

    /**
     * 3.4.6.11 上报软件版本信息包 (软件版本信息表256字节待定)
     */
    private void uploadSoftwareVersionMessage(ByteBuffer byteBuffer, String host) {
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        short cmd = byteBuffer.getShort(60);
        // 软件版本信息表 (数据不全 先显示前16个字节)
        byte[] bytes1 = new byte[16];
        byteBuffer.position(62);
        byteBuffer.get(bytes1);
        /*byte[] softwareVersionMessage = new byte[256];
        byteBuffer.get(softwareVersionMessage, 62, 256);*/
        int verify = byteBuffer.getInt(318); // 校验和
        int messageEnd = byteBuffer.getInt(322); // 报文尾
        List<Object> list = new ArrayList<>();
        list.add(host);
        list.add(bytes1);
        // 根据设备发送指定信息
        simpMessagingTemplate.convertAndSend("/uploadSoftwareVersionMessage/send", new ResultEntity(SystemStatusCodeEnum.SUCCESS, list));
    }

    /**
     * 3.4.6.12 上传设备网络参数信息包
     */
    private void uploadDeviceNetWorkParamMessage(ByteBuffer byteBuffer, String host) {
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        SendDeviceNetWorkParam sendDeviceNetWorkParam = new SendDeviceNetWorkParam();
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        short cmd = byteBuffer.getShort(60);
        short networkID = byteBuffer.getShort(62); // 网络终端ID号
        sendDeviceNetWorkParam.setNetworkID(String.valueOf(networkID));
        byteBuffer.position(64);
        byte[] networkIP1s = new byte[4];
        byteBuffer.get(networkIP1s);
        sendDeviceNetWorkParam.setNetworkIP1(getIP(networkIP1s)); // 网络IP地址1
        String mac1 = getMac(byteBuffer.getShort(68), byteBuffer.getInt(70));  // 网络MAC地址1
        sendDeviceNetWorkParam.setNetworkMacIP1(mac1);
        int networkMessage1 = byteBuffer.getInt(74); // 网络端口信息1
        sendDeviceNetWorkParam.setNetworkMessage1(String.valueOf(networkMessage1));
        byteBuffer.position(78);
        byte[] networkIP2s = new byte[4];
        byteBuffer.get(networkIP2s);
        sendDeviceNetWorkParam.setNetworkIP2(getIP(networkIP2s)); // 网络IP地址2
        String mac2 = getMac(byteBuffer.getShort(82), byteBuffer.getInt(86)); // 网络MAC地址2
        sendDeviceNetWorkParam.setNetworkMacIP2(mac2);
        int networkMessage2 = byteBuffer.getInt(88); // 网络端口信息2
        sendDeviceNetWorkParam.setNetworkMessage2(String.valueOf(networkMessage2));

        byteBuffer.position(92);
        byte[] networkIP3s = new byte[4];
        byteBuffer.get(networkIP3s);
        sendDeviceNetWorkParam.setNetworkIP3(getIP(networkIP3s)); // 网络IP地址3

        String mac3 = getMac(byteBuffer.getShort(96), byteBuffer.getInt(98)); // 网络MAC地址3
        sendDeviceNetWorkParam.setNetworkMacIP3(mac3);
        int networkMessage3 = byteBuffer.getInt(102); // 网络端口信息3
        sendDeviceNetWorkParam.setNetworkMessage3(String.valueOf(networkMessage3));

        byteBuffer.position(106);
        byte[] networkIP4s = new byte[4];
        byteBuffer.get(networkIP4s);
        sendDeviceNetWorkParam.setNetworkIP4(getIP(networkIP4s)); // 网络IP地址4

        String mac4 = getMac(byteBuffer.getShort(110), byteBuffer.getInt(112)); // 网络MAC地址4
        sendDeviceNetWorkParam.setNetworkMacIP4(mac4);
        int networkMessage4 = byteBuffer.getInt(116); // 网络端口信息4
        sendDeviceNetWorkParam.setNetworkMessage4(String.valueOf(networkMessage4));

        byteBuffer.position(120);
        byte[] networkIP5s = new byte[4];
        byteBuffer.get(networkIP5s);
        sendDeviceNetWorkParam.setNetworkIP5(getIP(networkIP5s)); // 网络IP地址5

        String mac5 = getMac(byteBuffer.getShort(124), byteBuffer.getInt(126)); // 网络MAC地址5
        sendDeviceNetWorkParam.setNetworkMacIP5(mac5);
        int networkMessage5 = byteBuffer.getInt(130); // 网络端口信息5
        sendDeviceNetWorkParam.setNetworkMessage5(String.valueOf(networkMessage5));

        byteBuffer.position(134);
        byte[] networkIP6s = new byte[4];
        byteBuffer.get(networkIP6s);
        sendDeviceNetWorkParam.setNetworkIP6(getIP(networkIP6s)); // 网络IP地址6
        String mac6 = getMac(byteBuffer.getShort(138), byteBuffer.getInt(140)); // 网络MAC地址6
        sendDeviceNetWorkParam.setNetworkMacIP6(mac6);
        int networkMessage6 = byteBuffer.getInt(144); // 网络端口信息6
        sendDeviceNetWorkParam.setNetworkMessage6(String.valueOf(networkMessage6));
        // 结尾
        int verify = byteBuffer.getInt(148); // 校验和
        int messageEnd = byteBuffer.getInt(152); // 报文尾
        List<Object> list = new ArrayList<>();
        list.add(host);
        list.add(sendDeviceNetWorkParam);
        // 根据设备发送指定信息
        simpMessagingTemplate.convertAndSend("/uploadDeviceNetWorkParamMessage/send", new ResultEntity(SystemStatusCodeEnum.SUCCESS, list));
    }

    /**
     * 3.4.6.13 上报雷达子系统工作状态信息包 雷达子系统状态信息
     */
    @Async
    public void uploadRadarSubSystemWorkStatusMessage(ByteBuffer byteBuffer, String host) {
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        Map<String, String> map1 = new HashMap<>();
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        // 雷达子系统状态信息
        short header = (short) SocketConfig.BinaryToDecimal(byteBuffer.getShort(60));
        Short dataType = byteBuffer.getShort(62); // 数据类型
        int dataLength = byteBuffer.getInt(64); // 数据长度
        byteBuffer.position(68);
        byte[] bytes1 = new byte[64]; // 系统控制信息
        byteBuffer.get(bytes1);
        byteBuffer.position(132);
        byte[] bytes2 = new byte[64]; // GPS数据
        byteBuffer.get(bytes2);
        // 数据信息
        byte faNodeNo = byteBuffer.get(196); // 发方节点号
        map1.put("faNodeNo", String.valueOf(faNodeNo));
        byte souNodeNo = byteBuffer.get(197); // 收方节点号
        map1.put("receiveNodeNo", String.valueOf(souNodeNo));
        short feedbackNo = byteBuffer.getShort(198); // 反馈指令序号
        map1.put("feedbackNo", String.valueOf(feedbackNo));
        short cmdReceiveStatus = byteBuffer.getShort(200); // 指令接收状态
        map1.put("cmdReceiveStatus", String.valueOf(cmdReceiveStatus));
        short taskNo = byteBuffer.getShort(202); //任务编号
        map1.put("taskNo", String.valueOf(taskNo));
        short frontEndWorkTemperature = byteBuffer.getShort(204); // 前端工作温度
        map1.put("frontEndWorkTemperature", String.valueOf(frontEndWorkTemperature));
        short fenjiWorkTemperature = byteBuffer.getShort(206);  // 分机工作温度
        map1.put("fenjiWorkTemperature", String.valueOf(fenjiWorkTemperature));
        long fenJiWorkStatus = byteBuffer.getLong(208);  // 分机工作状态
        String fenJiWorkStatusString = Long.toBinaryString(fenJiWorkStatus); // 将分机工作状态解析为二进制

        StringBuilder stringBuilders = new StringBuilder();
        if (fenJiWorkStatusString.length() < 64) {
            for (int i = 0; i < 64 - fenJiWorkStatusString.length(); i++) {
                stringBuilders.append("0");
            }
        }
        map1.put("fenJiWorkStatus", getWorkStatus(stringBuilders.append(fenJiWorkStatusString)));
        int numCount = byteBuffer.getInt(216); // 全脉冲个数统计
        map1.put("numCount", String.valueOf(numCount));
        int dataPagCount = byteBuffer.getInt(220); // 辐射源数据包统计
        map1.put("dataPagCount", String.valueOf(dataPagCount));
        int zhongDataPagCount = byteBuffer.getInt(224); // 中频数据统计
        map1.put("zhongDataPagCount", String.valueOf(zhongDataPagCount));
        byte deviceNo = byteBuffer.get(228); // 设备编号
        // 解析设备编号
        String deviceNoString = Integer.toBinaryString((deviceNo & 0xFF) + 0x100).substring(1);
        StringBuilder stringBuilder1 = new StringBuilder(deviceNoString);
        stringBuilder1.reverse();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(stringBuilder1, 0, 4);
        int bit7_4 = Integer.parseInt(stringBuilder1.substring(4, 6));
        // bit7-bit4 0：520项目 1：西沙改 2：大车 3：舰载
        if (bit7_4 == 0) {
            stringBuilder.append("0");
        } else if (bit7_4 == 1) {
            stringBuilder.append("1");
        } else if (bit7_4 == 10) {
            stringBuilder.append("2");
        } else if (bit7_4 == 11) {
            // 3在2进制中为11
            stringBuilder.append("3");
        }
        map1.put("deviceNo", stringBuilder.toString());
        byteBuffer.position(229);
        byte[] bytes3 = new byte[7]; // 备份
        byteBuffer.get(bytes3);
        byte b = byteBuffer.get(236); // 6-18GHz 长电缆均1
        map1.put("longCable1", String.valueOf(b));
        byte c = byteBuffer.get(237); // 6-18GHz 长电缆均2
        map1.put("longCable2", String.valueOf(c));
        byte d = byteBuffer.get(238); // 测向1
        map1.put("directionFinding1", String.valueOf(d));
        byte e = byteBuffer.get(239); // 测向2
        map1.put("directionFinding2", String.valueOf(e));
        byteBuffer.position(240);
        byte[] bytes4 = new byte[32];
        byteBuffer.get(bytes4);
        byteBuffer.position(272);
        byte[] bytes5 = new byte[128];
        byteBuffer.get(bytes5);
        byteBuffer.position(400);
        byte[] bytes6 = new byte[128];
        byteBuffer.get(bytes6);
        byteBuffer.getInt(528);
        // 结尾
        int verify = byteBuffer.getInt(532); // 校验和
        int messageEnd = byteBuffer.getInt(536); // 报文尾
        map1.put("host", host);
        simpMessagingTemplate.convertAndSend("/uploadRadarSubSystemWorkStatusMessage/send", new ResultEntity(SystemStatusCodeEnum.SUCCESS, map1));
    }

    /**
     * 软件版本信息表
     */
    private void uploadVersionNumberMessage(ByteBuffer byteBuffer, String host) {
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        SoftwareVersion softwareVersion = new SoftwareVersion();
        int messageLength = byteBuffer.getInt(48);
        long tasklowNo = byteBuffer.getLong(52);
        //雷达加载版本号
        //int lateralFPGA=byteBuffer.getInt(60); //侧向FPGA_A版本号
        softwareVersion.setLateralFPGA(String.valueOf(byteBuffer.getInt(60)));
        softwareVersion.setLateralZ7PS(String.valueOf(byteBuffer.getInt(64)));
        softwareVersion.setCrimesFPGA(String.valueOf(byteBuffer.getInt(68)));
        softwareVersion.setCrimesZ7PS(String.valueOf(byteBuffer.getInt(72)));
        softwareVersion.setSignalFPGA(String.valueOf(byteBuffer.getInt(76)));
        softwareVersion.setSystemFPGA(String.valueOf(byteBuffer.getInt(80)));
        softwareVersion.setLateralFPGB(String.valueOf(byteBuffer.getInt(84)));
        softwareVersion.setLateralZ7PL(String.valueOf(byteBuffer.getInt(88)));
        softwareVersion.setCrimesFPGB(String.valueOf(byteBuffer.getInt(92)));
        softwareVersion.setCrimesZ7PL(String.valueOf(byteBuffer.getInt(96)));
        softwareVersion.setSignalDSP(String.valueOf(byteBuffer.getInt(100)));
        softwareVersion.setSystemDSP(String.valueOf(byteBuffer.getInt(104)));
        //敌我加载版本号
        softwareVersion.setMasterControlFPGA1(String.valueOf(byteBuffer.getInt(108)));
        softwareVersion.setMasterControlDSP(String.valueOf(byteBuffer.getInt(112)));
        softwareVersion.setInspectFPGA2(String.valueOf(byteBuffer.getInt(116)));
        softwareVersion.setMasterControlFPGA2(String.valueOf(byteBuffer.getInt(120)));
        softwareVersion.setInspectFPGA1(String.valueOf(byteBuffer.getInt(124)));
        softwareVersion.setInspectFPGA(String.valueOf(byteBuffer.getInt(128)));
        //结尾
        int verify = byteBuffer.getInt(132); // 校验和
        int messageEnd = byteBuffer.getInt(136); // 报文尾
        List<Object> list = new ArrayList<>();
        list.add(host);
        list.add(softwareVersion);
        simpMessagingTemplate.convertAndSend("/uploadVersionNumberMessage/send", new ResultEntity(SystemStatusCodeEnum.SUCCESS, list));
    }

    /**
     *软件版本更新
     */




    // 解析工作状态  3.4.6.10 上报自检结果   3.4.6.9 上传心跳信息 引用
    private Map<String, String> workStatus(byte systemWorkStatus) {
        // 解析系统工作状态
        String s = Integer.toBinaryString((systemWorkStatus & 0xFF) + 0x100).substring(1);
        String radarSubSystem = s.substring(7); // 雷达子系统
        String friendOrFoe = s.substring(6, 7); // 敌我子系统
        String twicePowerSupply = s.substring(5, 6); //二次电源设备
        String other1 = s.substring(4, 5); // 其他设备1 bit3
        String other2 = s.substring(3, 4); // 其他设备2 bit4
        String other3 = s.substring(2, 3); // 其他设备3 bit5
        String other4 = s.substring(1, 2); // 其他设备4 bit6
        String other5 = s.substring(0, 1); // 其他设备5 bit7
        Map<String, String> map = new HashMap<>();
        map.put("radarSubSystem", radarSubSystem);
        map.put("friendOrFoe", friendOrFoe);
        map.put("twicePowerSupply", twicePowerSupply);
        map.put("other1", other1);
        map.put("other2", other2);
        map.put("other3", other3);
        map.put("other4", other4);
        map.put("other5", other5);
        return map;
    }

    // 解析系统控制信息
    private SystemControlBroadcastCMD systemControlBroadcastCMDs(byte[] bytes) {
        SystemControlBroadcastCMD systemControlBroadcastCMD = new SystemControlBroadcastCMD();
        ByteBuffer byteBuffer = ByteBuffer.allocate(64);
        byteBuffer.put(bytes);
        short header = byteBuffer.getShort();
        systemControlBroadcastCMD.setMessagePackageNum(byteBuffer.getShort(3));
        byte[] bytes1 = new byte[8];
        byteBuffer.get(bytes1, 5, 8);
        String time = new String(bytes1);
        systemControlBroadcastCMD.setTimeCode(time);
        systemControlBroadcastCMD.setWorkWay(byteBuffer.get(13));
        systemControlBroadcastCMD.setBandwidthChoose(byteBuffer.get(14));
        systemControlBroadcastCMD.setWorkCycleNum(byteBuffer.get(15));
        systemControlBroadcastCMD.setWorkCycleLength(byteBuffer.get(16));
        systemControlBroadcastCMD.setCenterFrequency(byteBuffer.getShort(17));
        systemControlBroadcastCMD.setDirectionFindingAntennaChoose(byteBuffer.get(19));
        systemControlBroadcastCMD.setScoutAntennaChoose(byteBuffer.get(20));
        systemControlBroadcastCMD.setPulseScreenMinimumFrequency(byteBuffer.get(27));
        systemControlBroadcastCMD.setPulseScreenMaximumFrequency(byteBuffer.getShort(29));
        systemControlBroadcastCMD.setPulseScreenMinimumRange(byteBuffer.get(31));
        systemControlBroadcastCMD.setPulseScreenMaximumRange(byteBuffer.get(32));
        systemControlBroadcastCMD.setPulseScreenMinimumPulseWidth(byteBuffer.getShort(33));
        systemControlBroadcastCMD.setPulseScreenMaximumPulseWidth(byteBuffer.getShort(35));
        byte[] bytes2 = new byte[16];
        byteBuffer.get(bytes2, 37, 16);
        systemControlBroadcastCMD.setRouteShield(bytes2);
        systemControlBroadcastCMD.setWithinThePulseGuidanceSwitch(byteBuffer.get(53));
        systemControlBroadcastCMD.setWithinThePulseGuidance(byteBuffer.get(54));
        systemControlBroadcastCMD.setUploadFullPulseNum(byteBuffer.getShort(55));
        byte[] bytes3 = new byte[2];
        byteBuffer.get(bytes3, 57, 2);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes3) {
            String tString = Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
            stringBuilder.append(tString);
        }
        systemControlBroadcastCMD.setExtensionControl(stringBuilder.toString());
        String tString = Integer.toBinaryString((byteBuffer.get(59) & 0xFF) + 0x100).substring(1);
        systemControlBroadcastCMD.setEquipmentSerialNum(tString);
        systemControlBroadcastCMD.setDetectionThresholdAdjustment(byteBuffer.get(60));
        return systemControlBroadcastCMD;
    }

    // 查询该IP是否存在
    private boolean hasHostIP(String host) {
        Optional<AllHost> allHost = hostRepository.findByHost(host);
        return allHost.isPresent();
    }

    public List<AllHost> findAll() {
        return hostRepository.findAll();
    }

    // 解析分机工作状态二进制
    private String getWorkStatus(StringBuilder work) {
        // 中频存储组件状态
        StringBuilder stringBuilder = new StringBuilder();
        work.reverse();
        String workStatuss = work.toString();
        // 测向处理组件状态
        stringBuilder.append(workStatuss, 0, 10);
        // 侦察处理组件状态
        stringBuilder.append(workStatuss, 16, 27);
        // 信号分选组件状态 (因为图片中没用bit4，所以暂时不把bit4传给前端)
        stringBuilder.append(workStatuss, 32, 36);
        stringBuilder.append(workStatuss, 37, 42);
//        stringBuilder.append(workStatuss, 32, 42);
        // 系统控制组件状态
        stringBuilder.append(workStatuss, 43, 52);
        // 中频存储组件状态
        stringBuilder.append(workStatuss, 54, 58);
        return stringBuilder.reverse().toString();
    }

    // 解析mac
    private static String getMac(short s, int c) {
        String g;
        String d = Integer.toHexString(s);
        // 当short s为负数时  如：-10000 转换成16进制后为 FFFFD8F0 因为前4位没用，所以需要截取后4位。正数时则不需要
        if (s < 0) {
            g = d.substring(4);
        } else {
            g = d;
        }
        String f = Integer.toHexString(c);
        String h = f + g;
        // 需要转换成大写形式显示给前端方便显示
        StringBuilder stringBuilder = new StringBuilder(h.toUpperCase());
        // 补上冒号 直接传给前端
        stringBuilder.insert(2, " : ");
        stringBuilder.insert(7, " : ");
        stringBuilder.insert(12, " : ");
        stringBuilder.insert(17, " : ");
        stringBuilder.insert(22, " : ");
        return stringBuilder.toString();
    }

    // 解析IP地址
    private static String getIP(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        // 为什么要从大到小 循环
        // 因为 192.168.31.88 这样类似的ip byte[3] 是192 所以要先从大到小循环
        for (int i = bytes.length - 1; i >= 0; i--) {
            /**
             *
             * 如果当前值小于0，即为负数 那么需要将当前值转换成16进制，再转成10进制
             * 解释： 为什么会为负数？
             * 答： 因为当传过来的数大于127时，那么转成byte时会转换成负数 如168 --> 会变成 -88 这个时候就需要将-88先转换成16进制，再转换成String类型 转回168
             */
            if (bytes[i] < 0) {
                /**
                 * 因为由后端拼接IP所以 当遍历到最后一个字节时 不需要再加小数点 所以要做判断
                 */
                if (i == 0) {
                    stringBuilder.append(new BigInteger(Integer.toHexString(bytes[i]).substring(6), 16).toString());
                } else {
                    stringBuilder.append(new BigInteger(Integer.toHexString(bytes[i]).substring(6), 16).toString()).append(".");
                }
            } else {
                if (i == 0) {
                    stringBuilder.append(bytes[i]);
                } else {
                    // 当为正数时 不需要判断 直接在末尾加小数点
                    stringBuilder.append(bytes[i]).append(".");
                }
            }
        }
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[4];
        bytes[0] = 88;
        bytes[1] = 31;
        bytes[2] = -88;
        bytes[3] = (byte) 192;
        System.out.println(getIP(bytes));
//        String d = Integer.toHexString(bytes[1]);
//        System.out.println(d);
//        String a = new BigInteger(d, 16).toString();
//        System.out.println(a);
//        System.out.println(getMac((short) 10000, 589620011));
     /*   byte b=5;
        String s=Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
        System.out.println(s.substring(6,7));
        byte[] b1=new byte[5];
        b1[0]=1;
        b1[1]=2;
        b1[2]=3;
        b1[3]=4;
        b1[4]=5;
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        byteBuffer.put(b1);
        byte b2=byteBuffer.get(2);
        System.out.println(b2 );
        Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);*/
       /* byte b = (byte) 255;
        DeviceCheckCMD deviceCheckCMD = new DeviceCheckCMD();
        deviceCheckCMD.setCheckNum("255");
        byte c = (byte) Integer.parseInt(deviceCheckCMD.getCheckNum());

        System.out.println(Integer.toBinaryString((c & 0xFF) + 0x100).substring(1));*/
//        String s = Long.toBinaryString(Long.parseLong("0E8B5A"));
        //把字符串转成字符数组
  /*      char[] strChar="8c".toCharArray();
        String result="";
        for(int i=0;i<strChar.length;i++){
            //toBinaryString(int i)返回变量的二进制表示的字符串
            //toHexString(int i) 八进制
            //toOctalString(int i) 十六进制
            result +=Integer.toBinaryString(strChar[i])+ " ";
        }
        System.out.println(result);*/
        /*byte[] bytes=SocketConfig.hexToByte("8CEC4BB57051");
        System.out.println(bytes.length);*/
//        String s = "0010";
//        String a = Integer.toBinaryString((s & 0xFF) + 0x100).substring(1);
//        String a = String.valueOf(s);
//        System.out.println(Integer.parseInt(s));
    }


}
