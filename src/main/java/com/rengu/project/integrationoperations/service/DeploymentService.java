package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.repository.CMDSerialNumberRepository;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.thread.TCPThread;
import com.rengu.project.integrationoperations.util.SocketConfig;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rengu.project.integrationoperations.util.SocketConfig.BinaryToDecimal;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:32
 */
@Service
@Slf4j
public class DeploymentService {
    private Socket socket = null;
    private byte backups = 0;
    private final CMDSerialNumberRepository cmdSerialNumberRepository;
    private short shorts = 0;
    private final HostRepository hostRepository;
    private Set<String> set = new HashSet<>();

    @Autowired
    public DeploymentService(CMDSerialNumberRepository cmdSerialNumberRepository, HostRepository hostRepository) {
        this.cmdSerialNumberRepository = cmdSerialNumberRepository;
        this.hostRepository = hostRepository;
    }


    //  系统控制指令帧格式说明（头部固定信息）
    private void sendSystemControlCmdFormat(ByteBuffer byteBuffer) {
        String frameHeader = "55AA55AA";
        byte[] bytes = SocketConfig.hexToByte(frameHeader);
        byteBuffer.putInt(1437226410);
        byteBuffer.putShort(shorts);
        byteBuffer.putShort(shorts);
    }

    //  发送时间
    public void sendSystemTiming(String time, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(40);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(addSerialNum());
            byteBuffer.putInt(40);
            //  指令类型
            byteBuffer.putShort((short) 1);
            byteBuffer.putShort((short) 1);
            //  包头
            byteBuffer.putShort(SocketConfig.header);
            //  解时间
            if (time.isEmpty()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:SSS:dd:MM:yyyy");
                time = simpleDateFormat.format(new Date());
            }
            byte hour = Byte.parseByte(time.substring(0, 2));
            byteBuffer.put(hour);
            byte minute = Byte.parseByte(time.substring(3, 5));
            byteBuffer.put(minute);
            //  秒>毫秒>int>16进制
            byteBuffer.putShort(Short.parseShort(time.substring(6, 9)));
//            String millisecond = Integer.toHexString(Integer.parseInt(time.substring(6, 9)) );
//            byte[] byteMS = SocketConfig.hexToByte(millisecond);
//            byteBuffer.put(byteMS);
//            if (byteMS.length == 0) {
//                byteBuffer.putShort(shorts);
//            }
            byte day = Byte.parseByte(time.substring(10, 12));
            byteBuffer.put(day);
            byte month = Byte.parseByte(time.substring(13, 15));
            byteBuffer.put(month);
            short year = Short.parseShort(time.substring(16));
            byteBuffer.putShort(year);
            //  包尾
            getPackageTheTail(byteBuffer);
            int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);
            //  帧尾

            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            if (TCPThread.map.get(host) != null) {
                Socket socket = (Socket) TCPThread.map.get(host);
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
            outputStream.close();
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //  分机控制指令
    public String sendExtensionControlCMD(ExtensionControlCMD extensionControlCMD, String host) throws SystemException {
        try {
            socket = new Socket(host, SocketConfig.port);
            ByteBuffer byteBuffer = ByteBuffer.allocate(60);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            //  包头
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(addSerialNum());
            byteBuffer.putInt(60);
            byteBuffer.putShort((short) 6);
            byteBuffer.putShort((short) 1);
            byteBuffer.putShort(SocketConfig.header);
            byte pulse = Byte.parseByte(extensionControlCMD.getPulse());
            byteBuffer.put(pulse);
            byteBuffer.put(backups);
            //  分机控制字
            extensionControl(extensionControlCMD.getExtensionControlCharacter(), byteBuffer);
            byteBuffer.put(backups);
            byteBuffer.put(Byte.parseByte(extensionControlCMD.getThreshold()));
            byteBuffer.putShort((Short.parseShort(extensionControlCMD.getOverallpulse())));
            byteBuffer.put(Byte.parseByte(extensionControlCMD.getMinamplitude()));
            byte c = (byte) (Integer.parseInt(extensionControlCMD.getMaxamplitude()) & 0xff);
            byteBuffer.put(c);
            byteBuffer.putShort(Short.parseShort(extensionControlCMD.getMinPulsewidth()));
            byteBuffer.putShort(Short.parseShort(extensionControlCMD.getMaxPulsewidth()));
            byteBuffer.putShort(Short.parseShort(extensionControlCMD.getFilterMaximumFrequency()));
            byteBuffer.putShort(Short.parseShort(extensionControlCMD.getFilterMinimumFrequency()));
            byteBuffer.putShort(Short.parseShort(extensionControlCMD.getShieldingMaximumFrequency()));
            byteBuffer.putShort(Short.parseShort(extensionControlCMD.getShieldingMinimumFrequency()));
            //  默认值更新标记
            if (extensionControlCMD.getDefalutUpdate().equals("0")) {
                byteBuffer.put((byte) 0);
            } else if (extensionControlCMD.getDefalutUpdate().equals("1")) {
                byteBuffer.put((byte) 1);
            }

            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            byteBuffer.putShort(shorts);
            //  包尾
            getPackageTheTail(byteBuffer);
            int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);

            //  帧尾
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            if (TCPThread.map.get(host) != null) {
                Socket socket = (Socket) TCPThread.map.get(host);
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
            outputStream.close();
            return "SUCCESS";
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    socket = null;
                }
            }
        }
    }

    //  分机控制字(解析String为bit)
    private void extensionControl(String eccs, ByteBuffer byteBuffer) {
        //  截取分机控制字中的数据》再将二进制转换成十进制
        StringBuilder stringBuilders = new StringBuilder();
        stringBuilders.append(eccs);
        String ecc = stringBuilders.reverse().toString();
        StringBuilder stringBuilder = new StringBuilder();
        //  合路选择
        String mergeToChoose = ecc.substring(0, 1);
        switch (mergeToChoose) {
            case "2":
                stringBuilder.append("10");
                break;
            case "1":
                stringBuilder.append("01");
                break;
            case "0":
                stringBuilder.append("00");
                break;
            default:
                throw new SystemException(SystemStatusCodeEnum.ExtensionControlCharacter_ERROR);
        }
        //  校正
        String revise = ecc.substring(1, 2);
        switch (revise) {
            case "3":
                stringBuilder.append("11");
                break;
            case "2":
                stringBuilder.append("10");
                break;
            case "1":
                stringBuilder.append("01");
                break;
            case "0":
                stringBuilder.append("00");
                break;
            default:
                throw new SystemException(SystemStatusCodeEnum.ExtensionControlCharacter_ERROR);
        }
        //  校准模式选择
        stringBuilder.append(ecc, 2, 3);
        //  分选预处理模式
        String pretreatment = ecc.substring(3, 4);
        if (pretreatment.equals("1")) {
            stringBuilder.append("01");
        } else if (pretreatment.equals("0")) {
            stringBuilder.append("00");
        } else {
            throw new SystemException(SystemStatusCodeEnum.ExtensionControlCharacter_ERROR);
        }
        //  脉冲
        String allPulse = ecc.substring(4, 5);
        switch (allPulse) {
            case "3":
                stringBuilder.append("011");
                break;
            case "2":
                stringBuilder.append("010");
                break;
            case "1":
                stringBuilder.append("001");
                break;
            case "0":
                stringBuilder.append("000");
                break;
            default:
                throw new SystemException(SystemStatusCodeEnum.ExtensionControlCharacter_ERROR);
        }
        stringBuilder.append(ecc.substring(5));
        byte[] bytes = new byte[2];
        String reverse = stringBuilder.toString();
        bytes[0] = (byte) BinaryToDecimal(Integer.parseInt(reverse.substring(0, 5)));
        bytes[1] = (byte) BinaryToDecimal(Integer.parseInt(reverse.substring(5)));
        byteBuffer.put(bytes[0]);
        byteBuffer.put(bytes[1]);
    }

    //  发送系统控制指令
    public String sendSystemControlCMD(SystemControlCMD systemControlCMD, String host) {
        try {
            socket = new Socket(host, SocketConfig.port);
            ByteBuffer byteBuffer = ByteBuffer.allocate(76);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(addSerialNum());
            byteBuffer.putInt(76);
            byteBuffer.putShort((short) 3);
            byteBuffer.putShort((short) 1);
            byteBuffer.putShort(SocketConfig.header);
            byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkCycle()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getWorkCycleAmount()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getBeginFrequency()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getEndFrequency()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getSteppedFrequency()));
            byteBuffer.put(Byte.parseByte(systemControlCMD.getSteppedFrequency()));
            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            byteBuffer.put(Byte.parseByte(systemControlCMD.getChooseAntenna1()));
            byteBuffer.put(Byte.parseByte(systemControlCMD.getChooseAntenna2()));
            byteBuffer.putShort(shorts);
            //  射频一衰减
            StringBuilder stringBuilder = new StringBuilder();
            String attenuationRF1 = stringBuilder.reverse().append(systemControlCMD.getAttenuationRF1()).toString();
            byte bytes = (byte) BinaryToDecimal(Integer.parseInt(attenuationRF1));
            byteBuffer.put(bytes);
            //  射频一长电缆均衡衰减控制
//            StringBuilder stringBuilders = new StringBuilder();
            //  反转数组的原因是因为二级制从第0位开始是从右边开始的，而传过来的值第0位在最左边，所以需要反转
//            String balancedAttenuationRF1 = stringBuilders.reverse().append(systemControlCMD.getBalancedAttenuationRF1()).toString();
//            byte bytesAttenuationRF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));
            byte bytesAttenuationRF1 = (byte) BinaryToDecimal(Integer.parseInt(systemControlCMD.getBalancedAttenuationRF1()));
            byteBuffer.put(bytesAttenuationRF1);
            byteBuffer.putShort(shorts);
            //  射频二控制衰减
//            StringBuilder stringBuilder2 = new StringBuilder();
//            String attenuationRF2 = stringBuilder2.reverse().append(systemControlCMD.getAttenuationRF2()).toString();
//            byte byteAttenuationRF2 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(attenuationRF2));
            byte byteAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(systemControlCMD.getBalancedAttenuationRF2()));
            byteBuffer.put(byteAttenuationRF2);
            //  射频二长电缆均衡衰减控制
            StringBuilder stringBuilderAttenuationRF2 = new StringBuilder();
            String balancedAttenuationRF2 = stringBuilderAttenuationRF2.reverse().append(systemControlCMD.getBalancedAttenuationRF2()).toString();
            byte bytesAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF2));
            byteBuffer.put(bytesAttenuationRF2);
            byteBuffer.putShort(shorts);
            //  中频一衰减
            byte bytesAttenuationMF1 = (byte) BinaryToDecimal(Integer.parseInt(systemControlCMD.getAttenuationMF1()));
            byteBuffer.put(bytesAttenuationMF1);
            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            //  中频二衰减
            byte bytesAttenuationMF2 = (byte) BinaryToDecimal(Integer.parseInt(systemControlCMD.getAttenuationMF2()));
            byteBuffer.put(bytesAttenuationMF2);
            byteBuffer.put(Byte.parseByte(systemControlCMD.getAttenuationControlWay()));
            byteBuffer.putShort(shorts);
            //  自检源衰减
            byteBuffer.put(Byte.parseByte(systemControlCMD.getSelfInspectionAttenuation()));
            //  脉内引导批次开关
            byteBuffer.put(Byte.parseByte(systemControlCMD.getGuidanceSwitch()));
            //  脉内引导批次号
            byteBuffer.put(Byte.parseByte(systemControlCMD.getGuidance()));
            //  故障检测门限
            byteBuffer.put(Byte.parseByte(systemControlCMD.getFaultDetect()));
            //  定时时间码
            String time = systemControlCMD.getTimingCode();
            //  转换2进制
            StringBuilder month = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(0, 2))));
            StringBuilder day = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(2, 4))));
            StringBuilder hour = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(4, 6))));
            StringBuilder minute = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(6, 8))));
            StringBuilder second = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(8, 10))));
            //  拼接秒数
            int seconds = second.length();
            for (int i = 0; i < 11 - seconds; i++) {
                second.insert(0, "0");
            }
            //  拼接分钟
            int minutes = minute.length();
            for (int i = 0; i < 6 - minutes; i++) {
                minute.insert(0, "0");
            }
            //  拼接时钟
            int hours = hour.length();
            for (int i = 0; i < 5 - hours; i++) {
                hour.insert(0, "0");
            }
            //  拼接天数
            int days = day.length();
            for (int i = 0; i < 5 - days; i++) {
                day.insert(0, "0");
            }
            //  拼接月份
            int months = month.length();
            for (int i = 0; i < 4 - months; i++) {
                month.insert(0, "0");
            }
            String thisTime = month.toString() + day.toString() + hour.toString() + minute.toString() + second.toString();
            byte[] bytes1 = new byte[4];
            bytes1[0] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(0, 8)));
            bytes1[1] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(8, 16)));
            bytes1[2] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(16, 24)));
            bytes1[3] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(24)));
            for (byte b : bytes1) {
                byteBuffer.put(b);
            }
            //  单次执行指令集所需时间
            byteBuffer.putShort(shorts);
            getPackageTheTail(byteBuffer);
            int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            if (TCPThread.map.get(host) != null) {
                Socket socket = (Socket) TCPThread.map.get(host);
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
            outputStream.close();
            return "SUCCESS";
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    socket = null;
                }
            }
        }
    }

    //  群发系统控制指令
    public void sendAllSystemControlCMD(SystemControlCMD systemControlCMD) {
        List<AllHost> list = hostRepository.findAll();
        for (AllHost allHost : list) {
            sendSystemControlCMD(systemControlCMD, allHost.getHost());
        }
    }

    //  群发分机控制指令
    public void sendAllExtensionControl(ExtensionControlCMD extensionControlCMD) {
        List<AllHost> list = hostRepository.findAll();
        for (AllHost allHost : list) {
            sendExtensionControlCMD(extensionControlCMD, allHost.getHost());
        }
    }

    //  群发系统校时
    public void sendAllSendSystemTiming(String time) {
        List<AllHost> list = hostRepository.findAll();
        for (AllHost allHost : list) {
            sendSystemTiming(time, allHost.getHost());
        }
    }

    //  封装包尾信息
    private void getPackageTheTail(ByteBuffer byteBuffer) {
        byte[] bytes = SocketConfig.hexToByte(SocketConfig.end);
        byteBuffer.put(bytes);
    }

    //  封装大包尾信息
    private void getBigPackageTheTail(ByteBuffer byteBuffer) {
        String frameEnd = "55AA55AA";
        byte[] bytes = SocketConfig.hexToByte(frameEnd);
        byteBuffer.put(bytes);
    }

    // 查询当前编号,并且+1
    private int addSerialNum() {
        List<CMDSerialNumber> list = cmdSerialNumberRepository.findAll();
        int a = 0;
        if (list.size() == 1) {
            for (CMDSerialNumber cmdSerialNumber : list) {
                assert cmdSerialNumber != null;
                a = cmdSerialNumber.getSerialNumber() + 1;
                cmdSerialNumber.setSerialNumber(a);
                cmdSerialNumberRepository.save(cmdSerialNumber);
            }
        }
        return a;
    }

    // 在校验和的所有字节数相加
    private int getByteCount(ByteBuffer byteBuffer) {
        byte[] b = byteBuffer.array();
        int c = 0;
        for (byte a : b) {
            String s = Integer.toBinaryString((a & 0xFF) + 0x100).substring(1);
            c += BinaryToDecimal(Integer.parseInt(s));

        }
        return c;
    }


    // 储存或更新当前连接服务端的IP地址
    public void allHost(String hosts) {
        List<AllHost> list = hostRepository.findAll();
        if (!hasHostIP(hosts)) {
            set.add(hosts);
            AllHost allHosts = new AllHost();
            allHosts.setHost(hosts);
            allHosts.setNum(list.size()+1);
            hostRepository.save(allHosts);
        }
        log.info("当前连接数: " + set.size());
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


    @Async
    public void receiveSocketHandler1(Socket socket) throws IOException {
        @Cleanup InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        log.info("-------接收报文-----");
        byte[] bytes = new byte[1024];
        inputStream.read(bytes);
//        IOUtils.copy(inputStream, byteArrayOutputStream);
        String host = socket.getInetAddress().getHostAddress();
        if (byteArrayOutputStream.toByteArray().length > 600) {
            reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray(), host);
        } else if (byteArrayOutputStream.toByteArray().length > 400) {
            reciveAndConvertIronRadar(byteArrayOutputStream.toByteArray(), host);
        }
    }

    @Async
    public void receiveSocketHandler2(Socket socket) throws IOException {
        @Cleanup InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        log.info("-------接收报文-----");
        byte[] bytes = new byte[1024];
        inputStream.read(bytes);
//        IOUtils.copy(inputStream, byteArrayOutputStream);
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
        byte[] bytes = new byte[1024];
        inputStream.read(bytes);
//        IOUtils.copy(inputStream, byteArrayOutputStream);
        String host = socket.getInetAddress().getHostAddress();
        if (byteArrayOutputStream.toByteArray().length > 600) {
            reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray(), host);
        } else if (byteArrayOutputStream.toByteArray().length > 400) {
            reciveAndConvertIronRadar(byteArrayOutputStream.toByteArray(), host);
        }
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
}
