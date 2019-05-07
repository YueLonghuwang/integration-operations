package com.rengu.project.integrationoperations.thread;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.util.SocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yaojiahao
 * @data 2019/4/29 15:30
 */

@Slf4j
@Component
public class TCPThread {
    private final HostRepository hostRepository;

    @Autowired
    public TCPThread(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    // 监听铁塔敌我端口
    @Async
    public void TCPIronFriendOrFoeReceiver() {
        int ironFriendOrFoePort = 5886;
        log.info("监听铁塔敌我端口: " + ironFriendOrFoePort);
        try {
            ServerSocket serverSocket = new ServerSocket(ironFriendOrFoePort);
            while (true) {
                Socket client = serverSocket.accept();
                scoketIronFriendOrFoeHandler(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 监听铁塔雷达端口
    @Async
    public void TCPIronRadarReceiver() {
        Set<String> setHost = new HashSet<>();
        int ironRadarPort = 5887;
        log.info("监听铁塔雷达端口: " + ironRadarPort);
        try {
            ServerSocket serverSocket = new ServerSocket(ironRadarPort);
            while (true) {
                Socket client = serverSocket.accept();
                setHost.add(client.getInetAddress().getHostAddress());
                if (setHost.size() == 3) {
                    allHost(setHost);
                }
                scoketIronRadarHandler(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 储存或更新当前连接服务端的IP地址
    private void allHost(Set<String> set) {
        List<AllHost> list = hostRepository.findAll();
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
        } else {
            for (String host : set) {
                AllHost allHost = new AllHost();
                allHost.setHost(host);
                hostRepository.save(allHost);
            }
        }
    }

    //  接收铁塔敌我报文
    @Async
    public void scoketIronFriendOrFoeHandler(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray());
    }

    //  接收铁塔雷达报文
    @Async
    public void scoketIronRadarHandler(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        reciveAndConvertIronRadar(byteArrayOutputStream.toByteArray());
    }

    //  解析铁塔敌我报文
    private void reciveAndConvertIronFriendOrFoe(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(600);
        byteBuffer.put(bytes);
        // 判断包
        short header = (short) SocketConfig.BinaryToDecimal(byteBuffer.getShort());
        short dataType = byteBuffer.getShort(3);
        int dataLength = byteBuffer.getInt(5);
        byte[] systemBytes = new byte[64];
        byteBuffer.get(systemBytes, 9, 64);
        byte[] dataGPS = new byte[64];
        byteBuffer.get(dataGPS, 73, 64);
        LabelDataFormat labelDataFormat = new LabelDataFormat();
        labelDataFormat.setSendNodeNum(byteBuffer.get(137));
        labelDataFormat.setReceiveNodeNum(byteBuffer.get(138));
        labelDataFormat.setReceiveCmdCount(byteBuffer.getShort(139));
        labelDataFormat.setReceiveCmdState(byteBuffer.getShort(141));
        labelDataFormat.setEquipmentSerialNum(byteBuffer.getShort(143));
        labelDataFormat.setFrontEndWorkT(byteBuffer.getShort(145));
        labelDataFormat.setSynthesizeOneWorkT(byteBuffer.getShort(147));
        labelDataFormat.setSynthesizeTwoWorkT(byteBuffer.getShort(149));
        labelDataFormat.setExtensionTwoWorkT(byteBuffer.getShort(151));
        labelDataFormat.setExtensionThreeWorkT(byteBuffer.getShort(153));
        labelDataFormat.setExtensionFourWorkT(byteBuffer.getShort(155));
        labelDataFormat.setExtensionFiveWorkT(byteBuffer.getShort(157));
        labelDataFormat.setExtensionSixWorkT(byteBuffer.getShort(159));
        labelDataFormat.setOverallPulseUploadingNum1030(byteBuffer.getInt(161));
        labelDataFormat.setFriendOrFoeRecognitionNum1030(byteBuffer.getInt(165));
        labelDataFormat.setMFNum1030(byteBuffer.getInt(169));
        labelDataFormat.setOverallPulseNum1090(byteBuffer.getInt(173));
        labelDataFormat.setFriendOrFoeRecognitionNum1090(byteBuffer.getInt(177));
        labelDataFormat.setMFNum1090(byteBuffer.getInt(181));
        labelDataFormat.setOverallPulseNum740(byteBuffer.getInt(185));
        labelDataFormat.setFriendOrFoeRecognitionNum1464(byteBuffer.getInt(189));
        labelDataFormat.setMFNum1464(byteBuffer.getInt(193));
        labelDataFormat.setFriendOrFoeRecognitionNum1532(byteBuffer.getInt(197));
        labelDataFormat.setMFNum1532(byteBuffer.getInt(201));
        labelDataFormat.setOverallPulseNum1464(byteBuffer.getInt(205));
        labelDataFormat.setOverallPulseNum1532(byteBuffer.getInt(209));
        labelDataFormat.setMFM51030(byteBuffer.getInt(213));
        labelDataFormat.setMFM1090(byteBuffer.getInt(217));
        // 分机计数  传过来的时候
        /*
         *   尾 》》》头
         *   截取5个字节
         *   再转换成2进制
         *   在循环的时候把从最后一个字节向前遍历 头 》》》尾
         * */
        byte[] extensionCount = new byte[5];
        byteBuffer.get(extensionCount, 221, 5);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            String tString = Integer.toBinaryString((b & 0xFF) + 0x100).substring(1);
            stringBuilder.append(tString);
        }
        String extensionCounts = stringBuilder.toString();
        labelDataFormat.setMainControlFPGA1(extensionCounts.substring(0, 2));
        labelDataFormat.setMainControlFPGA2(extensionCounts.substring(2, 4));
        labelDataFormat.setMainControlDSP(extensionCounts.substring(4, 6));
        labelDataFormat.setDetectionOneFPGA1(extensionCounts.substring(6, 8));
        labelDataFormat.setDetectionOneFPGA2(extensionCounts.substring(8, 10));
        labelDataFormat.setDetectionOneDSP(extensionCounts.substring(10, 12));
        labelDataFormat.setDetectionTwoFPGA1(extensionCounts.substring(12, 14));
        labelDataFormat.setDetectionTwoFPGA2(extensionCounts.substring(14, 16));
        labelDataFormat.setDetectionTwoDSP(extensionCounts.substring(16, 18));
        labelDataFormat.setDetectionThreeFPGA1(extensionCounts.substring(18, 20));
        labelDataFormat.setDetectionThreeFPGA2(extensionCounts.substring(20, 22));
        labelDataFormat.setDetectionThreeDSP(extensionCounts.substring(22, 24));
        labelDataFormat.setDetectionFourFPGA1(extensionCounts.substring(24, 26));
        labelDataFormat.setDetectionFourFPGA2(extensionCounts.substring(26, 28));
        labelDataFormat.setNoteTheNumber(extensionCounts.substring(37, 38));
        labelDataFormat.setExternalSecPulseAbnormalSign(extensionCounts.substring(38, 39));
        labelDataFormat.setPulsePosition(extensionCounts.substring(39));
        //  主控故障状态
        byte mainControlState = byteBuffer.get(229);
        String mainControlStates = Integer.toBinaryString((mainControlState & 0xFF) + 0x100).substring(1);
        labelDataFormat.setSignalDetection2(mainControlStates.substring(0, 1));
        labelDataFormat.setSignalDetection1(mainControlStates.substring(1, 2));
        labelDataFormat.setDDR2_2(mainControlStates.substring(2, 3));
        labelDataFormat.setDDR2_1(mainControlStates.substring(3, 4));
        labelDataFormat.setDDR3_2(mainControlStates.substring(4, 5));
        labelDataFormat.setDDR3_1(mainControlStates.substring(5));
        // 分机故障状态
        labelDataFormat.setExtensionMalfunctionState1(byteBuffer.get(230));
        labelDataFormat.setExtensionMalfunctionState2(byteBuffer.get(231));
//        labelDataFormat.setExtensionMalfunctionState3(byteBuffer.get(232));
        labelDataFormat.setExtensionMalfunctionState4(byteBuffer.get(233));
        labelDataFormat.setMainControlIP(byteBuffer.getInt(246));
        labelDataFormat.setGPRSOneIP(byteBuffer.getInt(250));
        labelDataFormat.setMainControlDSPPort(byteBuffer.getShort(262));
        labelDataFormat.setGPRSOneDSPPort(byteBuffer.getShort(264));
        byte[] bytes1 = new byte[6];
        byteBuffer.get(bytes1, 270, 6);
        labelDataFormat.setMainControlHost(bytes1);
        byte[] bytes2 = new byte[6];
        byteBuffer.get(bytes2, 276, 6);
        labelDataFormat.setGPRSOneMACHost(bytes2);
        labelDataFormat.setMainControlGateway(byteBuffer.getInt(294));
        labelDataFormat.setGPRSOneGateway(byteBuffer.getInt(298));
        labelDataFormat.setMainControlUpperIP(byteBuffer.getInt(310));
        labelDataFormat.setGPRSOneUpperIP(byteBuffer.getInt(314));
        labelDataFormat.setInteriorStateIP(byteBuffer.getInt(326));
        labelDataFormat.setUpperSysControlCMDPort(byteBuffer.getShort(330));
        labelDataFormat.setInteriorCMDPort(byteBuffer.getShort(332));
        labelDataFormat.setGPRSReconsitutionIP(byteBuffer.getInt(334));
        labelDataFormat.setGPRSReconsitutionPort(byteBuffer.getShort(338));
        labelDataFormat.setDSPInteriorCMDPort(byteBuffer.getShort(340));
        byte[] bytes3 = new byte[14];
        byteBuffer.get(bytes3, 344, 14);
        labelDataFormat.setFPGAReconsitutionState(bytes3);
        labelDataFormat.setDSPReconsitutionState(byteBuffer.getInt(358));
        labelDataFormat.setDSPReconsitutionIdentification(byteBuffer.getInt(366));
        labelDataFormat.setIPReconsitutionIdentification(byteBuffer.getInt(370));
        byte[] bytes4 = new byte[32];
        byteBuffer.get(bytes4, 386, 32);
        labelDataFormat.setFrontEndState(bytes4);
        byte[] bytes5 = new byte[128];
        byteBuffer.get(bytes5, 418, 128);
        labelDataFormat.setKeyStateInfo(bytes5);
        byte[] bytes6 = SocketConfig.hexToByte(SocketConfig.end);
        int end = byteBuffer.getInt(542);
    }

    //  解析铁塔雷达报文
    private void reciveAndConvertIronRadar(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(500);
        byteBuffer.put(bytes);
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
}