package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.repository.CMDSerialNumberRepository;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.thread.TCPThread;
import com.rengu.project.integrationoperations.util.SocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import static com.rengu.project.integrationoperations.util.SocketConfig.BinaryToDecimal;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:32
 */
@Service
@Slf4j
public class DeploymentService {
    private byte backups = 0;
    private final CMDSerialNumberRepository cmdSerialNumberRepository;
    private short shorts = 0;
    private final HostRepository hostRepository;
    private Set<String> set = new HashSet<>();
    private final ReceiveInformationService receiveInformationService;

    @Autowired
    public DeploymentService(CMDSerialNumberRepository cmdSerialNumberRepository, HostRepository hostRepository, ReceiveInformationService receiveInformationService) {
        this.cmdSerialNumberRepository = cmdSerialNumberRepository;
        this.hostRepository = hostRepository;
        this.receiveInformationService = receiveInformationService;
    }

    //  系统控制指令帧格式说明（头部固定信息）
    private void sendSystemControlCmdFormat(ByteBuffer byteBuffer) {
        // 报文头 7E9118E7
        byteBuffer.putInt(2122389735);
        // 当前包数据长度
    }

    /**
     * 3.4.6.3 设备自检指令
     */
    public void sendDeviceCheckCMD(DeviceCheckCMD deviceCheckCMD, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(76);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(4);  // 发报日期时间
            byteBuffer.putInt(4);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(4);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(4);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(4);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            // 报文内容
            byteBuffer.putInt(0); // 信息长度
            byteBuffer.putLong(Long.parseLong(deviceCheckCMD.getTaskFlowNo()));// 任务流水号
            byteBuffer.put(Byte.parseByte(deviceCheckCMD.getCheckType())); // 自检类型
            byteBuffer.putShort(Short.parseShort(deviceCheckCMD.getCheckPeriod())); // 自检周期
            byte num = (byte) (Integer.parseInt(deviceCheckCMD.getCheckNum()));
            byteBuffer.put(num); // 自检数量
            byteBuffer.putInt(Integer.parseInt(deviceCheckCMD.getSingleMachineCode())); // 被检单机代码
            byteBuffer.putInt(0); // 校验和 (暂时预留)
            //  帧尾
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }

    /**
     * 3.4.6.4 系统校时指令
     */
    public void sendSystemTiming(String timeNow, String time, String timingPattern, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(77);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(1);  // 发报日期时间
            byteBuffer.putInt(1);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(1);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(1);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(1);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            // 报文内容
            byteBuffer.putInt(1); //信息长度
            byteBuffer.putLong(Long.parseLong(timeNow));
            byteBuffer.put(Byte.parseByte(timingPattern));
            byteBuffer.putLong(Long.parseLong(time));
//            getTime(byteBuffer, time);
            /*//  包头
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
            byte day = Byte.parseByte(time.substring(10, 12));
            byteBuffer.put(day);
            byte month = Byte.parseByte(time.substring(13, 15));
            byteBuffer.put(month);
            short year = Short.parseShort(time.substring(16));
            byteBuffer.putShort(year);
            //  包尾
            getPackageTheTail(byteBuffer);*/

            byteBuffer.putInt(0); // 校验和 (暂时预留)

            /*int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);*/
            //  帧尾
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }

    /**
     * 3.4.6.5 设备复位
     */
    public void sendDeviceRestoration(String timeNow, String executePattern, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(69);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(5);  // 发报日期时间
            byteBuffer.putInt(5);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(5);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(5);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(5);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(timeNow)); // 任务流水号
            byteBuffer.put(Byte.parseByte(executePattern));  // 执行方式
            byteBuffer.putInt(0); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }

    /**
     * 3.4.6.6 软件版本远程更新
     */
    public void sendSoftwareUpdateCMD(String timeNow, String cmd, String softwareID, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(72);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(6);  // 发报日期时间
            byteBuffer.putInt(6);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(6);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(6);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(6);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            // 报文内容
            byteBuffer.putInt(2); // 信息长度
            byteBuffer.putLong(Long.parseLong(timeNow)); // 任务流水号
            byteBuffer.putShort(Short.parseShort(cmd));  // 指令操作码
            byteBuffer.putShort(Short.parseShort(softwareID)); // 软件ID号
            byteBuffer.putInt(0); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }

    /**
     * 3.4.6.7 设备网络参数更新指令
     */
    public void sendDeviceNetworkCMD(SendDeviceNetWorkParam sendDeviceNetWorkParam, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(156);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(7);  // 发报日期时间
            byteBuffer.putInt(7);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(7);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(7);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(7);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            // 报文内容
            byteBuffer.putInt(3); // 信息长度
            byteBuffer.putLong(Long.parseLong(sendDeviceNetWorkParam.getTaskFlowNo())); // 任务流水号
            byteBuffer.putShort(Short.parseShort(sendDeviceNetWorkParam.getCmd()));
            byteBuffer.putShort(Short.parseShort(sendDeviceNetWorkParam.getNetworkID())); // 网络终端ID号

            // 1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP1())); // 网络IP地址1
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP1()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage1())); //网络端口信息1

            // 2
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP2())); // 网络IP地址2
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP2()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage2())); //网络端口信息2

            // 3
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP3())); // 网络IP地址3
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP3()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage3())); //网络端口信息3

            // 4
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP4())); // 网络IP地址4
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP4()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage4())); //网络端口信息4

            // 5
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP5())); // 网络IP地址5
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP5()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage5())); //网络端口信息5

            // 6
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP6())); // 网络IP地址6
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP6()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage6())); //网络端口信息6
            byteBuffer.putInt(0); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }

    /**
     * 3.4.6.8 设备工作流程控制指令
     */
    public void sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, int count, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1094);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(8);  // 发报日期时间
            byteBuffer.putInt(8);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(8);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(8);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(8);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getTaskFlowNo()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getCmd()));
            for (int i = 1; i <= count; i++) {
                byteBuffer.putShort(SocketConfig.header);
                byte pulse = Byte.parseByte(deviceWorkFlowCMD.getPulse());
                byteBuffer.put(pulse);
                byteBuffer.put(backups);
                //  分机控制字
                extensionControl(deviceWorkFlowCMD.getExtensionControlCharacter(), byteBuffer);
                byteBuffer.put(backups);
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getThreshold()));
                byteBuffer.putShort((Short.parseShort(deviceWorkFlowCMD.getOverallpulse())));
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getMinamplitude()));
                byte c = (byte) (Integer.parseInt(deviceWorkFlowCMD.getMaxamplitude()) & 0xff);
                byteBuffer.put(c);
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMinPulsewidth()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMaxPulsewidth()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMaximumFrequency()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMinimumFrequency()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMaximumFrequency()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMinimumFrequency()));
                //  默认值更新标记
                if (deviceWorkFlowCMD.getDefalutUpdate().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (deviceWorkFlowCMD.getDefalutUpdate().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                byteBuffer.putShort(shorts);
                byteBuffer.put(backups);
                byteBuffer.putShort(shorts);
                //  包尾
                getPackageTheTail(byteBuffer);

            }
            // 512字节 多余补0
            int a = 512;
            byte[] bytes = new byte[a - count * 32];
            byteBuffer.put(bytes);
            for (int i = 1; i <= count; i++) {
                // 系统控制指令
                byteBuffer.putShort(SocketConfig.header);
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getWorkPattern()));
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getWorkCycle()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getWorkCycleAmount()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getBeginFrequency()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getEndFrequency()));
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getSteppedFrequency()));
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getSteppedFrequency()));
                byteBuffer.putShort(shorts);
                byteBuffer.put(backups);
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getChooseAntenna1()));
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getChooseAntenna2()));
                byteBuffer.putShort(shorts);
                byte b = 0;
                byteBuffer.put(b);
                byteBuffer.put(b);
                byteBuffer.putShort(shorts);
                byteBuffer.put(b);
                byteBuffer.put(b);
                byteBuffer.putShort(shorts);
                byteBuffer.put(b);
                byteBuffer.putShort(shorts);
                byteBuffer.put(backups);
                byteBuffer.put(b);
                byteBuffer.put(b);
                byteBuffer.putShort(shorts);
                //  自检源衰减
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getSelfInspectionAttenuation()));
                //  脉内引导批次开关
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getGuidanceSwitch()));
                //  脉内引导批次号
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getGuidance()));
                //  故障检测门限
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getFaultDetect()));

                int d = 0;
                byteBuffer.putInt(d);

                //  单次执行指令集所需时间
                byteBuffer.putShort(shorts);
                getPackageTheTail(byteBuffer);
            }
            // 512字节 多余补0
            int c = 512;
            byte[] byte1 = new byte[c - count * 48];
            byteBuffer.put(byte1);
            byteBuffer.putInt(0); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }

    //  分机控制指令
   /* public String sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, String host) throws SystemException {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(88);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            //  包头
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(2);  // 发报日期时间
            byteBuffer.putInt(2);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(2);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(2);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(2);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            byteBuffer.putShort(SocketConfig.header);
            byte pulse = Byte.parseByte(deviceWorkFlowCMD.getPulse());
            byteBuffer.put(pulse);
            byteBuffer.put(backups);
            //  分机控制字
            extensionControl(deviceWorkFlowCMD.getExtensionControlCharacter(), byteBuffer);
            byteBuffer.put(backups);
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getThreshold()));
            byteBuffer.putShort((Short.parseShort(deviceWorkFlowCMD.getOverallpulse())));
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getMinamplitude()));
            byte c = (byte) (Integer.parseInt(deviceWorkFlowCMD.getMaxamplitude()) & 0xff);
            byteBuffer.put(c);
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMinPulsewidth()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMaxPulsewidth()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMaximumFrequency()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMinimumFrequency()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMaximumFrequency()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMinimumFrequency()));
            //  默认值更新标记
            if (deviceWorkFlowCMD.getDefalutUpdate().equals("0")) {
                byteBuffer.put((byte) 0);
            } else if (deviceWorkFlowCMD.getDefalutUpdate().equals("1")) {
                byteBuffer.put((byte) 1);
            }

            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            byteBuffer.putShort(shorts);
            //  包尾
            getPackageTheTail(byteBuffer);

            byteBuffer.putInt(0); // 校验和 (暂时预留)
            *//*int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);*//*
            getBigPackageTheTail(byteBuffer);  //  帧尾
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
            return "SUCCESS";
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }*/

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
   /* public String sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, String host) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(104);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            //  包头
            sendSystemControlCmdFormat(byteBuffer);
            byteBuffer.putInt(1); // 当前包数据长度
            byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
            byteBuffer.putShort(shorts);  // 源地址(设备ID号)
            byteBuffer.put(backups); // 域ID(预留)
            byteBuffer.put(backups); // 主题ID(预留)
            byteBuffer.putShort(shorts);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
            byteBuffer.putLong(3);  // 发报日期时间
            byteBuffer.putInt(3);  // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
            byteBuffer.putInt(3);  // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
            byteBuffer.putInt(3);  // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
            byteBuffer.putInt(3);  // 数据总长度
            byteBuffer.putShort(shorts); // 版本号
            byteBuffer.putInt(0); // 保留字段
            byteBuffer.putShort(shorts); // 保留字段
            byteBuffer.putShort(SocketConfig.header);
            deviceWorkFlowCMD.getWorkPattern();
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getWorkPattern()));
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getWorkCycle()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getWorkCycleAmount()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getBeginFrequency()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getEndFrequency()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getSteppedFrequency()));
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getSteppedFrequency()));
            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getChooseAntenna1()));
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getChooseAntenna2()));
            byteBuffer.putShort(shorts);
            //  射频一衰减(最新)
           *//* StringBuilder stringBuilder = new StringBuilder();
            String attenuationRF1 = stringBuilder.reverse().append(deviceWorkFlowCMD.getAttenuationRF1()).toString();
            byte bytes = (byte) BinaryToDecimal(Integer.parseInt(attenuationRF1));*//*
            byte b = 0;
            byteBuffer.put(b);

            //  射频一长电缆均衡衰减控制
//            StringBuilder stringBuilders = new StringBuilder();
            //  反转数组的原因是因为二级制从第0位开始是从右边开始的，而传过来的值第0位在最左边，所以需要反转
//            String balancedAttenuationRF1 = stringBuilders.reverse().append(deviceWorkFlowCMD.getBalancedAttenuationRF1()).toString();
//            byte bytesAttenuationRF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));

            *//*(最新)
            byte bytesAttenuationRF1 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getBalancedAttenuationRF1()));
            byteBuffer.put(bytesAttenuationRF1);*//*
            byteBuffer.put(b);

            byteBuffer.putShort(shorts);
            //  射频二控制衰减
//            StringBuilder stringBuilder2 = new StringBuilder();
//            String attenuationRF2 = stringBuilder2.reverse().append(deviceWorkFlowCMD.getAttenuationRF2()).toString();
//            byte byteAttenuationRF2 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(attenuationRF2));

            *//*(最新)
            byte byteAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getBalancedAttenuationRF2()));
            byteBuffer.put(byteAttenuationRF2);*//*
            byteBuffer.put(b);

            *//*射频二长电缆均衡衰减控制(最新)
            StringBuilder stringBuilderAttenuationRF2 = new StringBuilder();
            String balancedAttenuationRF2 = stringBuilderAttenuationRF2.reverse().append(deviceWorkFlowCMD.getBalancedAttenuationRF2()).toString();
            byte bytesAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF2));
            byteBuffer.put(bytesAttenuationRF2);*//*
            byteBuffer.put(b);

            byteBuffer.putShort(shorts);
           *//* 中频一衰减(最新)
            byte bytesAttenuationMF1 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getAttenuationMF1()));
            byteBuffer.put(bytesAttenuationMF1);*//*
            byteBuffer.put(b);

            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            *//*中频二衰减(最新)
            byte bytesAttenuationMF2 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getAttenuationMF2()));
            byteBuffer.put(bytesAttenuationMF2);*//*
            byteBuffer.put(b);

//            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getAttenuationControlWay()));
            byteBuffer.put(b);

            byteBuffer.putShort(shorts);
            //  自检源衰减
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getSelfInspectionAttenuation()));
            //  脉内引导批次开关
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getGuidanceSwitch()));
            //  脉内引导批次号
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getGuidance()));
            //  故障检测门限
            byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getFaultDetect()));

            //  定时时间码

       *//*     String time = deviceWorkFlowCMD.getTimingCode();
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
            for (byte c : bytes1) {
                byteBuffer.put(c);
            }*//*
            int d = 0;
            byteBuffer.putInt(d);

            //  单次执行指令集所需时间
            byteBuffer.putShort(shorts);
            getPackageTheTail(byteBuffer);
            byteBuffer.putInt(0); // 校验和 (暂时预留)
            *//*int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);*//*
            getBigPackageTheTail(byteBuffer);  //  帧尾
            OutputStream outputStream = null;
            Socket socket = (Socket) TCPThread.map.get(host);
            if (TCPThread.map.get(host) != null) {
                outputStream = socket.getOutputStream();
            }
            assert outputStream != null;
            outputStream.write(byteBuffer.array());
            return "SUCCESS";
        } catch (IOException e) {
            throw new SystemException(SystemStatusCodeEnum.SOCKET_CONNENT_ERROR);
        }
    }
*/

/*    //  群发系统控制指令
    public void sendAllDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD) {
        List<AllHost> list = hostRepository.findAll();
        for (AllHost allHost : list) {
            sendDeviceWorkFlowCMD(deviceWorkFlowCMD, allHost.getHost());
        }
    }

    //  群发分机控制指令
    public void sendAllExtensionControl(DeviceWorkFlowCMD deviceWorkFlowCMD) {
        List<AllHost> list = hostRepository.findAll();
        for (AllHost allHost : list) {
            sendDeviceWorkFlowCMD(deviceWorkFlowCMD, allHost.getHost());
        }
    }*/

    //  群发系统校时
    public void sendAllSendSystemTiming(String timeNow, String timingPattern, String time) throws IOException {
        List<AllHost> list = hostRepository.findAll();
        for (AllHost allHost : list) {
            sendSystemTiming(timeNow, time, timingPattern, allHost.getHost());
        }
    }

    //  封装包尾信息
    private void getPackageTheTail(ByteBuffer byteBuffer) {
        byte[] bytes = SocketConfig.hexToByte(SocketConfig.end);
        byteBuffer.put(bytes);
    }

    //  封装大包尾信息
    private void getBigPackageTheTail(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) 143;
        bytes[1] = (byte) 144;
        bytes[2] = 9;
        bytes[3] = (byte) 248;
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

    // 时间
    private void getTime(ByteBuffer byteBuffer, String time) {
        short year = Short.parseShort(time.substring(0, 4));
        byteBuffer.putShort(year);
        byte month = Byte.parseByte(time.substring(5, 7));
        byteBuffer.put(month);
        byte day = Byte.parseByte(time.substring(8, 10));
        byteBuffer.put(day);
        byte hour = Byte.parseByte(time.substring(11, 13));
        byteBuffer.put(hour);
        byte minute = Byte.parseByte(time.substring(14, 16));
        byteBuffer.put(minute);
        byte second = Byte.parseByte(time.substring(17, 19));
        byteBuffer.put(second);
        //  毫秒
        byteBuffer.putShort(Short.parseShort(time.substring(20)));
    }

    // MAC地址信息解析
    private byte[] getMac(String macIP) {
        return SocketConfig.hexToByte(macIP);
    }
}
