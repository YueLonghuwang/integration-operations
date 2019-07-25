package com.rengu.project.integrationoperations.thread;

import com.rengu.project.integrationoperations.entity.AllHost;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.service.WebSendToCService;
import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yaojiahao
 * @data 2019/4/29 15:30
 */

@Slf4j
@Component
public class TCPThread {
    public static final Map<String, Object> map = new ConcurrentHashMap<>();
    //  接收报文
    private final WebSendToCService webSendToCService;
    private final WebReceiveToCService receiveInformationService;
    private final HostRepository hostRepository;
    private static Selector selector;

    public TCPThread(WebSendToCService webSendToCService, WebReceiveToCService receiveInformationService, HostRepository hostRepository) {
        this.webSendToCService = webSendToCService;
        this.receiveInformationService = receiveInformationService;
        this.hostRepository = hostRepository;
    }

    @Async
    public void monitoringTCP() {
        int portTCP = 5889;
        try {
            log.info("监听TCP端口: " + portTCP);
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); // 新建channel
            // 监听端口
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(portTCP));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isValid() & key.isReadable()) { // 监听到读事件，对读事件进行处理
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听到读事件，读取客户端发送过来的消息
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(3000);
        try {
            channel.read(buffer);
        } catch (IOException e) {
            key.cancel();
            channel.socket().close();
            channel.close();
            return;
        }
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        String host = channel.socket().getInetAddress().getHostAddress();
        List<AllHost> listAllHost = hostRepository.findAll();
        for (AllHost allHost : listAllHost) {
            if (allHost.getHost().equals(host) && allHost.getNum() == 1) {
                receiveInformationService.receiveSocketHandler1(buffer,host);
            } else if (allHost.getHost().equals(host) && allHost.getNum() == 2) {
                receiveInformationService.receiveSocketHandler2(buffer,host);
            } else if (allHost.getHost().equals(host) && allHost.getNum() == 3) {
                receiveInformationService.receiveSocketHandler3(buffer,host);
            }
        }
    }

    /**
     * 处理客户端连接成功事件
     */
    private void handleAccept(SelectionKey key) throws IOException {
        Set<String> set = new HashSet<>();
        // 获取客户端连接通道
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);
        map.put(socketChannel.socket().getInetAddress().getHostAddress(), socketChannel.socket());
        receiveInformationService.allHost(socketChannel.socket().getInetAddress().getHostAddress());
        set.add(socketChannel.socket().getInetAddress().getHostAddress());
        log.info("当前连接数: " + set.size());
        socketChannel.register(selector, SelectionKey.OP_READ);
    }


    /*// 监听TCP
    @Async
    public void monitoringTCPs() {
        int portTCP = 5888;

        try {
            log.info("监听TCP端口: " + portTCP);
            ServerSocket serverSocket = new ServerSocket(portTCP);
            while (true) {
                Socket socket = serverSocket.accept();
                String host = socket.getInetAddress().getHostAddress();

                // 存放Socket
                map.put(host, socket);
                receiveInformationService.allHost(host);
               *//* List<AllHost> listAllHost = hostRepository.findAll();
                for (AllHost allHost : listAllHost) {
                    if (allHost.getHost().equals(host) && allHost.getNum() == 1) {
                        receiveSocketHandler1(socket);
                    } else if (allHost.getHost().equals(host) && allHost.getNum() == 2) {
                        receiveInformationService.receiveSocketHandler2(socket);
                    } else if (allHost.getHost().equals(host) && allHost.getNum() == 3) {
                        receiveInformationService.receiveSocketHandler3(socket);
                    }
                }*//*
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void receiveSocketHandler1(Socket socket) throws IOException {
        // 为什么需要断开Socket才可以继续往下走
        InputStream inputStream = null;
        inputStream = socket.getInputStream();
        System.out.println(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        log.info("-------接收报文1-----");
        String host = socket.getInetAddress().getHostAddress();
//        receiveInformationService.sendMessage(inputStream, host);
    }*/
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