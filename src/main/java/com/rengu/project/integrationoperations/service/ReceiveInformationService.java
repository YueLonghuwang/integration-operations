package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.AllHost;
import com.rengu.project.integrationoperations.entity.LabelDataFormat;
import com.rengu.project.integrationoperations.entity.LabelPackageInfo;
import com.rengu.project.integrationoperations.entity.SystemControlBroadcastCMD;
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
    public Map receiveFixedInformation(ByteBuffer byteBuffer) {
        Map<String, Number> map = new HashMap<>();
        map.put("header", byteBuffer.getInt()); // 报文头
        map.put("dataLength", byteBuffer.getInt(5)); // 当前包数据长度
        map.put("targetHost", byteBuffer.getShort(9)); // 目的地址
        map.put("sourceHost", byteBuffer.getShort(11)); // 源地址
        map.put("regionID", byteBuffer.get(13)); // 域ID
        map.put("themeID", byteBuffer.get(14)); // 主题ID
        map.put("messageCategory", byteBuffer.getShort(15)); // 信息类别
        map.put("transmitDate", byteBuffer.getLong(17)); // 发报日期时间
        map.put("serialNumber", byteBuffer.getInt(25)); // 序列号
        map.put("bagTotal", byteBuffer.getInt(29)); // 包总数
        map.put("currentBagNo", byteBuffer.getInt(33)); // 当前包号
        map.put("dataTotalLength", byteBuffer.getInt(37)); // 数据总长度
        map.put("versionNumber", byteBuffer.getShort(41)); // 版本号
        map.put("backups1", byteBuffer.getInt(43)); // 保留字段
        map.put("backups2", byteBuffer.getShort(47)); // 保留字段
        /*int header = byteBuffer.getInt();
        int dataLength = byteBuffer.getInt(5);
        short targetHost = byteBuffer.getShort(9);
        short sourceHost = byteBuffer.getShort(11);
        byte regionID = byteBuffer.get(13);
        byte themeID = byteBuffer.get(14);
        short messageCategory = byteBuffer.getShort(15);
        long transmitDate = byteBuffer.getLong(17);
        int serialNumber = byteBuffer.getInt(25);
        int bagTotal = byteBuffer.getInt(29);
        int currentBagNo = byteBuffer.getInt(33);
        int dataTotalLength = byteBuffer.getInt(37);
        short versionNumber = byteBuffer.getShort(41);
        int backups1 = byteBuffer.getInt(43);
        short backups2 = byteBuffer.getShort(47);*/
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

    //  解析铁塔敌我报文
    @Async
    public void reciveAndConvertIronFriendOrFoe(byte[] bytes, String host) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(650);
        byteBuffer.put(bytes);
        // 判断包
        short header = (short) SocketConfig.BinaryToDecimal(byteBuffer.getShort());
        short dataType = byteBuffer.getShort(3);
        int dataLength = byteBuffer.getInt(5);
        byte[] systemBytes = new byte[64];
        byteBuffer.get(systemBytes, 9, 64);
        systemControlBroadcastCMDs(systemBytes);
        byte[] dataGPS = new byte[64];
        byteBuffer.get(dataGPS, 73, 64);
        LabelDataFormat labelDataFormat = new LabelDataFormat();
        labelDataFormat.setSystemWorkState(byteBuffer.get(137));
        labelDataFormat.setReceiveCmdCount(byteBuffer.get(138));
        // 分机计数
        byte[] extensionCountByte = new byte[6];
        byteBuffer.get(extensionCountByte, 139, 6);
        StringBuilder stringBuilder = getBit(extensionCountByte);
        labelDataFormat.setExtensionCount(stringBuilder.toString());
        labelDataFormat.setFrontEndWorkT(byteBuffer.getShort(145));
        labelDataFormat.setMainWorkT(byteBuffer.getShort(147));
        labelDataFormat.setDetectionWorkT(byteBuffer.getShort(149));
        labelDataFormat.setExtensionTwoWorkT(byteBuffer.getShort(151));
        labelDataFormat.setExtensionThreeWorkT(byteBuffer.getShort(153));
        labelDataFormat.setExtensionFourWorkT(byteBuffer.getShort(155));
        labelDataFormat.setExtensionFiveWorkT(byteBuffer.getShort(157));
        labelDataFormat.setExtensionSixWorkT(byteBuffer.getShort(159));
        labelDataFormat.setPDW740(byteBuffer.getInt(161));
        labelDataFormat.setPDW837_5(byteBuffer.getInt(165));
        labelDataFormat.setPDW1030(byteBuffer.getInt(169));
        labelDataFormat.setPDW1059(byteBuffer.getInt(173));
        labelDataFormat.setPDW1090(byteBuffer.getInt(177));
        labelDataFormat.setPDW1464(byteBuffer.getInt(181));
        labelDataFormat.setPDW1532(byteBuffer.getInt(185));
        labelDataFormat.setIFF740(byteBuffer.getInt(189));
        labelDataFormat.setIFF837_5(byteBuffer.getInt(193));
        labelDataFormat.setIFF1030(byteBuffer.getInt(197));
        labelDataFormat.setIFF1090(byteBuffer.getInt(201));
        labelDataFormat.setIFF1464(byteBuffer.getInt(205));
        labelDataFormat.setIFF1532(byteBuffer.getInt(209));
        //  IF个数
        byte[] ifCount = new byte[28];
        byteBuffer.get(ifCount, 213, 28);
        labelDataFormat.setIF(ifCount);
        labelDataFormat.setM51030(byteBuffer.getInt(241));
        labelDataFormat.setM51090(byteBuffer.getInt(245));
        labelDataFormat.setM5MF1030(byteBuffer.getInt(249));
        labelDataFormat.setM5MF1030S(byteBuffer.getInt(253));
        //  电源状态
        byte[] powerState = new byte[154];
        byteBuffer.get(powerState, 257, 154);
        labelDataFormat.setPowerState(powerState);
        labelDataFormat.setMainFPGA1Versions(byteBuffer.get(411));
        labelDataFormat.setMainFPGA2Versions(byteBuffer.get(412));
        labelDataFormat.setMainDSPVersions(byteBuffer.get(413));
        labelDataFormat.setDetectionTwoFPGA1(byteBuffer.get(414));
        labelDataFormat.setDetectionTwoFPGA2(byteBuffer.get(415));
        labelDataFormat.setDetectionTwoDSP(byteBuffer.get(416));
        //  版本号
        byte[] versions = new byte[16];
        byteBuffer.get(versions, 417, 16);
        labelDataFormat.setVersions(versions);
        labelDataFormat.setMainIPAddress(byteBuffer.getInt(433));
        labelDataFormat.setDetectionIPAddress(byteBuffer.getInt(437));
        labelDataFormat.setGPRS2IPAddress(byteBuffer.getInt(441));
        labelDataFormat.setGPRS3IPAddress(byteBuffer.getInt(445));
        labelDataFormat.setUserCMDPort(byteBuffer.getShort(449));
        labelDataFormat.setInteriorCMDPort(byteBuffer.getShort(451));
        labelDataFormat.setBackup1(byteBuffer.getInt(453));
        // 主控MAC地址
        byte[] mainControlAddress = new byte[6];
        byteBuffer.get(mainControlAddress, 457, 6);
        labelDataFormat.setMainControlAddress(mainControlAddress);
        // 检测MAC地址
        byte[] detectionMACAddress = new byte[6];
        byteBuffer.get(detectionMACAddress, 463, 6);
        labelDataFormat.setDetectionMACAddress(detectionMACAddress);
        // 数传2MAC地址
        byte[] GPRSTwoMACAddress = new byte[6];
        byteBuffer.get(GPRSTwoMACAddress, 469, 6);
        labelDataFormat.setGPRSTwoMACAddress(GPRSTwoMACAddress);
        // 数传3MAC地址
        byte[] GPRSThreeMACAddress = new byte[6];
        byteBuffer.get(GPRSThreeMACAddress, 475, 6);
        labelDataFormat.setGPRSThreeMACAddress(GPRSThreeMACAddress);
        // 备份
        byte[] backup2 = new byte[16];
        byteBuffer.get(backup2, 481, 16);
        labelDataFormat.setBackup2(backup2);
        labelDataFormat.setUpperIP(byteBuffer.getInt(497));
        // 备份
        byte[] backup3 = new byte[12];
        byteBuffer.get(backup3, 501, 12);
        labelDataFormat.setBackup3(backup3);
        labelDataFormat.setTagPort(byteBuffer.getShort(513));
        labelDataFormat.setDataPort1(byteBuffer.getShort(515));
        labelDataFormat.setDataPort2(byteBuffer.getShort(517));
        labelDataFormat.setDataPort3(byteBuffer.getShort(519));
        labelDataFormat.setInteriorStateIP(byteBuffer.getInt(521));
        labelDataFormat.setInteriorStatePortIP(byteBuffer.getShort(525));
        labelDataFormat.setBackup4(byteBuffer.getShort(527));
        //  MCU加载片区
        byte[] mcuLoad = new byte[8];
        byteBuffer.get(mcuLoad, 529, 8);
        StringBuilder stringBuilders = getBit(mcuLoad);
        labelDataFormat.setMCULoad(stringBuilders.toString());
        //  备份
        byte[] backup5 = new byte[64];
        byteBuffer.get(backup5, 537, 64);
        labelDataFormat.setBackup5(backup5);
        int end = byteBuffer.getInt(601);
    }

    //  解析铁塔雷达报文
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


    /**
     *                 3.4.6.2 心跳指令
     *  ==============================================
     */
    @Async
    public void receiveHeartbeatCMD(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(69);
        byteBuffer.put(bytes);
        Map map=receiveFixedInformation(byteBuffer);
        int messageLength = byteBuffer.getInt(51); // 信息长度
        long taskFlowNo = byteBuffer.getLong(55); // 任务流水号
        byte heartbeat = byteBuffer.get(63); // 心跳
        int verify = byteBuffer.getInt(64); // 校验和
        int messageEnd = byteBuffer.getInt(68); // 报文尾
    }

    /**
     *                 3.4.6.9 上传心跳信息
     *  ==============================================
     */
    @Async void uploadHeartBeatMessage(){
        ByteBuffer byteBuffer = ByteBuffer.allocate(72);
        byteBuffer.getInt(51);

    }

    /**
     *                 3.4.6.10 上报自检结果
     *  ==============================================
     */

    /**
     *                 3.4.6.11 上报软件版本信息包
     *  ==============================================
     */

}
