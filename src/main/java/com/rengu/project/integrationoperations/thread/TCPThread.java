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
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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

//    //  接收铁塔敌我报文
//    @Async
//    public void scoketIronFriendOrFoeHandler(Socket socket) throws IOException {
//        InputStream inputStream = socket.getInputStream();
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        IOUtils.copy(inputStream, byteArrayOutputStream);
//        reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray());
//    }
//    // 监听铁塔敌我端口
//    @Async
//    public void TCPIronFriendOrFoeReceiver() {
//        int ironFriendOrFoePort = 5886;
//        log.info("监听铁塔敌我端口: " + ironFriendOrFoePort);
//        try {
//            ServerSocket serverSocket = new ServerSocket(ironFriendOrFoePort);
//            while (true) {
//                Socket client = serverSocket.accept();
//                scoketIronFriendOrFoeHandler(client);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // 监听铁塔雷达端口
//    @Async
//    public void TCPIronRadarReceiver() {
//        Set<String> setHost = new HashSet<>();
//        int ironRadarPort = 5887;
//        log.info("监听铁塔雷达端口: " + ironRadarPort);
//        try {
//            ServerSocket serverSocket = new ServerSocket(ironRadarPort);
//            while (true) {
//                Socket client = serverSocket.accept();
//                setHost.add(client.getInetAddress().getHostAddress());
//                if (setHost.size() == 3) {
//                    allHost(setHost);
//                }
//                scoketIronRadarHandler(client);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

      /*  labelDataFormat.setSendNodeNum(byteBuffer.get(137));
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
        *//*
     *   尾 》》》头
     *   截取5个字节
     *   再转换成2进制
     *   在循环的时候把从最后一个字节向前遍历 头 》》》尾
     * *//*
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
        labelDataFormat.setKeyStateInfo(bytes5);*/
}