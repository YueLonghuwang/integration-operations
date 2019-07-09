package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.util.SocketConfig;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * author : yaojiahao
 * Date: 2019/7/8 11:19
 **/

@Service
@Slf4j
public class ReceiveInformationService {
    private final HostRepository hostRepository;
    private Set<String> set = new HashSet<>();

    public ReceiveInformationService(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    // 储存或更新当前连接服务端的IP地址
    public void allHost(String hosts) {
        List<AllHost> list = hostRepository.findAll();
        if (!hasHostIP(hosts)) {
            set.add(hosts);
            AllHost allHosts = new AllHost();
            allHosts.setHost(hosts);
            allHosts.setNum(list.size() + 1);
            hostRepository.save(allHosts);
        }
        //  如果数据库的ip地址有三个，并且当前ip有修改，那么修改当前IP,并且存入数据库
        Set<String> set1 = new HashSet<>(set);
        if (list.size() == 3) {
            for (AllHost allHost : list) {
                set.add(allHost.getHost());
            }
            // 如果存入的size大于3，那么代表有新的ip地址，因为set自动去重,所以存入新的IP地址
            if (set.size() > 3) {
                for (AllHost allHost : list) {
                    hostRepository.deleteById(allHost.getId());
                }
                for (String s : set1) {
                    AllHost allHost = new AllHost();
                    allHost.setHost(s);
                    hostRepository.save(allHost);
                }
            }
        }
    }

    // 解析报文固定信息
    @Async
    public Map<String, Number> receiveFixedInformation(ByteBuffer byteBuffer) {
        Map<String, Number> map = new HashMap<>();
        map.put("header", byteBuffer.getInt()); // 报文头
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

    @Async
    public void receiveSocketHandler1(Socket socket) throws IOException {
        @Cleanup InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        log.info("-------接收报文-----");
        /*byte[] bytes = new byte[1024];
        inputStream.read(bytes);*/
        IOUtils.copy(inputStream, byteArrayOutputStream);
        String host = socket.getInetAddress().getHostAddress();
        if (byteArrayOutputStream.toByteArray().length > 600) {
            reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray(), host);
        } else if (byteArrayOutputStream.toByteArray().length > 400) {
            reciveAndConvertIronRadar(byteArrayOutputStream.toByteArray(), host);
        } else if (byteArrayOutputStream.toByteArray().length > 60) {

        }
    }

    @Async
    public void receiveSocketHandler2(Socket socket) throws IOException {
        @Cleanup InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        log.info("-------接收报文-----");
        IOUtils.copy(inputStream, byteArrayOutputStream);
        String host = socket.getInetAddress().getHostAddress();
        if (byteArrayOutputStream.toByteArray().length > 600) {
            reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray(), host);
        } else if (byteArrayOutputStream.toByteArray().length > 400) {
            reciveAndConvertIronRadar(byteArrayOutputStream.toByteArray(), host);
        }
    }

    @Async
    public void receiveSocketHandler3(Socket socket) throws IOException {
        @Cleanup InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        log.info("-------接收报文-----");
         /* byte[] bytes = new byte[1024];
        inputStream.read(bytes);*/
        IOUtils.copy(inputStream, byteArrayOutputStream);
        String host = socket.getInetAddress().getHostAddress();
        if (byteArrayOutputStream.toByteArray().length > 600) {
            reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray(), host);
        } else if (byteArrayOutputStream.toByteArray().length > 400) {
            reciveAndConvertIronRadar(byteArrayOutputStream.toByteArray(), host);
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
     * 解析铁塔敌我报文
     */
    @Async
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
     * 解析铁塔雷达报文
     */
    @Async
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
     * 3.4.6.2 心跳指令
     */
    @Async
    public void receiveHeartbeatCMD(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(69);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48); // 信息长度
        long taskFlowNo = byteBuffer.getLong(52); // 任务流水号
        byte heartbeat = byteBuffer.get(60); // 心跳
        int verify = byteBuffer.getInt(61); // 校验和
        int messageEnd = byteBuffer.getInt(65); // 报文尾
    }

    /**
     * 3.4.6.9 上传心跳信息
     */
    @Async
    public void uploadHeartBeatMessage(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(72);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        byte systemWorkStatus = byteBuffer.get(60);
        Map map1 = workStatus(systemWorkStatus); // 解析系统工作状态
        byte[] backups = new byte[3];
        byteBuffer.get(backups, 61, 3);
        int verify = byteBuffer.getInt(64); // 校验和
        int messageEnd = byteBuffer.getInt(68); // 报文尾
    }

    /**
     * 3.4.6.10 上报自检结果
     */
    @Async
    public void uploadSelfInspectionResult(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(69);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        byte systemWorkStatus = byteBuffer.get(60);
        Map map1 = workStatus(systemWorkStatus); // 解析系统工作状态
        int verify = byteBuffer.getInt(64); // 校验和
        int messageEnd = byteBuffer.getInt(68); // 报文尾
    }


    /**
     * 3.4.6.11 上报软件版本信息包 (软件版本信息表256字节待定)
     */
    @Async
    public void uploadSoftwareVersionMessage(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(326);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        short cmd = byteBuffer.getShort(60);
        // 软件版本信息表
        byte[] softwareVersionMessage = new byte[256];
        byteBuffer.get(softwareVersionMessage, 62, 256);
        int verify = byteBuffer.getInt(64); // 校验和
        int messageEnd = byteBuffer.getInt(68); // 报文尾
    }

    /**
     * 3.4.6.12 上传设备网络参数信息包 (有重复，待定)
     */
    @Async
    public void uploadDeviceNetWorkParamMessage(byte[] bytes) {
        DeviceNetWorkParam deviceNetWorkParam = new DeviceNetWorkParam();
        ByteBuffer byteBuffer = ByteBuffer.allocate(143);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        short cmd = byteBuffer.getShort(60);
        short networkID = byteBuffer.getShort(62); // 网络终端ID号
        int networkIP1 = byteBuffer.getInt(64); // 网络IP地址1
        byte[] bytes1 = new byte[6]; // 网络MAC地址1
        byteBuffer.get(bytes1, 68, 6);
        int networkMessage1 = byteBuffer.getInt(74); // 网络端口信息1
        int networkIP2 = byteBuffer.getInt(78); // 网络IP地址2
        byte[] bytes2 = new byte[6]; // 网络MAC地址2
        byteBuffer.get(bytes1, 82, 6);
        int networkMessage2 = byteBuffer.getInt(88); // 网络端口信息2
        int networkIP3 = byteBuffer.getInt(92); // 网络IP地址3
        byte[] bytes3 = new byte[6]; // 网络MAC地址3
        byteBuffer.get(bytes1, 96, 6);
        int networkMessage3 = byteBuffer.getInt(102); // 网络端口信息3
        int networkIP4 = byteBuffer.getInt(106); // 网络IP地址4
        byte[] bytes4 = new byte[6]; // 网络MAC地址4
        byteBuffer.get(bytes1, 110, 6);
        int networkMessage4 = byteBuffer.getInt(116); // 网络端口信息4

        // 有重复 待定

        // 结尾
        int verify = byteBuffer.getInt(64); // 校验和
        int messageEnd = byteBuffer.getInt(68); // 报文尾
    }

    /**
     * 3.4.6.13上报雷达子系统工作状态信息包 雷达子系统状态信息(512字节待完成)
     */
    @Async
    public void uploadRadarSubSystemWorkStatusMessage(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(580);
        byteBuffer.put(bytes);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        Map<String, Number> map = receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(48);
        long taskFlowNo = byteBuffer.getLong(52);
        // 雷达子系统状态信息
        byte[] bytes1 = new byte[512];
        byteBuffer.get(bytes1, 60, 512);
    }


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
        List<AllHost> list = hostRepository.findAll();
        return list;
    }

    public static void main(String[] args) {
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
    }
}
