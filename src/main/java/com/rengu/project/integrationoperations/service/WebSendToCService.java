package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.repository.CMDSerialNumberRepository;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.thread.TCPThread;
import com.rengu.project.integrationoperations.util.SocketConfig;
import com.sun.deploy.panel.ITreeNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rengu.project.integrationoperations.util.SocketConfig.BinaryToDecimal;
import static com.rengu.project.integrationoperations.util.SocketConfig.BinaryToDecimals;

/**
 * java端发送给c++端
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:32
 */
@Service
@Slf4j
public class WebSendToCService {
    private byte backups = 0;
    private final CMDSerialNumberRepository cmdSerialNumberRepository;
    private short shorts = 0;
    private final HostRepository hostRepository;//当前连接的最大数

    @Autowired
    public WebSendToCService(CMDSerialNumberRepository cmdSerialNumberRepository, HostRepository hostRepository) {
        this.cmdSerialNumberRepository = cmdSerialNumberRepository;
        this.hostRepository = hostRepository;
    }

    //  系统控制指令帧格式说明（头部固定信息）
    private void sendSystemControlCmdFormat(ByteBuffer byteBuffer, int dataLength, short purPoseAddress, short sourceAddress, byte regionID, byte themeID, short messageCategory, long sendingDateTime, int seriesNumber, int packageSum, int currentNum, int dataLengthSum, short version, int retain1, short retain2) {
        //十六进制转换成十进制
        byteBuffer.putInt(2122389735);  // 报文头 7E8118E7
        byteBuffer.putInt(dataLength); // 当前包数据长度
        // 获取当前pid (无法获取目标的pid，暂时拿源地址的PID)
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        byteBuffer.putShort(Short.parseShort(pid));  // 目的地址(设备ID号)
        byteBuffer.putShort(Short.parseShort(pid));  // 源地址(设备ID号)
        byteBuffer.put(regionID); // 域ID(预留)
        byteBuffer.put(themeID); // 主题ID(预留)
        byteBuffer.putShort(messageCategory);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
        byteBuffer.putLong(getTimes());    // 发报日期时间
        byteBuffer.putInt(seriesNumber);    // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
        byteBuffer.putInt(1);    // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
        byteBuffer.putInt(currentNum);   // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
        byteBuffer.putInt(1536);   // 数据总长度
        // 高八位 主版本号 低八位 副版本号 0001 1000
        byteBuffer.putShort((short) 24); // 版本号
        byteBuffer.putInt(retain1); // 保留字段
        byteBuffer.putShort(retain2); // 保留字段
    }

    /**
     * 3.4.6.3 设备自检指令
     */
    public void sendDeviceCheckCMD(DeviceCheckCMD deviceCheckCMD, String host, String updateAll, int serialNumber) {
        // 群发消息
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            // 调用序号自增
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendDeviceCheckCMD(deviceCheckCMD, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(76);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 76, backups, backups, backups, backups, (short) 12290, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(0); // 信息长度 待定
            byteBuffer.putLong(Long.parseLong(deviceCheckCMD.getTaskFlowNo()));// 任务流水号
            byteBuffer.put(Byte.parseByte(deviceCheckCMD.getCheckType())); // 自检类型
            byteBuffer.putShort(Short.parseShort(deviceCheckCMD.getCheckPeriod())); // 自检周期
            byte num = (byte) (Integer.parseInt(deviceCheckCMD.getCheckNum()));
            byteBuffer.put(num); // 自检数量
            byteBuffer.putInt(Integer.parseInt(deviceCheckCMD.getSingleMachineCode())); // 被检单机代码
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            //  帧尾
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 3.4.6.4 系统校时指令
     */
    public void sendSystemTiming(String timeNow, String time, String timingPattern, String host, String updateAll, int serialNumber) {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendSystemTiming(timeNow, time, timingPattern, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(77);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 77, shorts, shorts, backups, backups, (short) 12291, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(1); //信息长度
            byteBuffer.putLong(Long.parseLong(timeNow));
            byteBuffer.put(Byte.parseByte(timingPattern));
            byteBuffer.putLong(Long.parseLong(time));
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 3.4.6.5 设备复位
     */
    public void sendDeviceRestoration(String timeNow, String executePattern, String host, String updateAll, int serialNumber) {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendDeviceRestoration(timeNow, executePattern, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(69);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 69, shorts, shorts, backups, backups, (short) 12292, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(timeNow)); // 任务流水号
            byteBuffer.put(Byte.parseByte(executePattern));  // 执行方式
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 3.4.6.6 软件版本远程更新
     */
    public void sendSoftwareUpdateCMD(String timeNow, String cmd, String softwareID, String host, String updateAll, int serialNumber) {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendSoftwareUpdateCMD(timeNow, cmd, softwareID, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(72);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 72, shorts, shorts, backups, backups, (short) 12293, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(2); // 信息长度
            byteBuffer.putLong(Long.parseLong(timeNow)); // 任务流水号
            byteBuffer.putShort(Short.parseShort(cmd));  // 指令操作码
            byteBuffer.putShort(Short.parseShort(softwareID)); // 软件ID号
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 3.4.6.7 设备网络参数更新指令
     */
    public void sendDeviceNetworkCMD(SendDeviceNetWorkParam sendDeviceNetWorkParam, String host, String updateAll, int serialNumber) {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendDeviceNetworkCMD(sendDeviceNetWorkParam, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(156);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 156, shorts, shorts, backups, backups, (short) 12294, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(3); // 信息长度
            byteBuffer.putLong(Long.parseLong(sendDeviceNetWorkParam.getTaskFlowNo())); // 任务流水号
            byteBuffer.putShort(Short.parseShort(sendDeviceNetWorkParam.getCmd())); //指令操作码
            byteBuffer.putShort(Short.parseShort(sendDeviceNetWorkParam.getNetworkID())); // 网络终端ID号

            // 1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP1())); // 网络IP地址1
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP1()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage1())); //网络端口信息1

            // 2
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP2())); // 网络IP地址2
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP2()));// MAC地址2
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage2())); //网络端口信息2

            // 3
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP3())); // 网络IP地址3
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP3()));// MAC地址3
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage3())); //网络端口信息3

            // 4
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP4())); // 网络IP地址4
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP4()));// MAC地址4
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage4())); //网络端口信息4

            // 5
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP5())); // 网络IP地址5
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP5()));// MAC地址5
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage5())); //网络端口信息5

            // 6
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP6())); // 网络IP地址6
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP6()));// MAC地址6
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage6())); //网络端口信息6
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 3.4.6.8 设备工作流程控制指令
     */
    public void sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, int count, String host, String updateAll, int serialNumber) {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendDeviceWorkFlowCMD(deviceWorkFlowCMD, count, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1110);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 1110, shorts, shorts, backups, backups, (short) 12295, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getTaskFlowNo()));
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getCmd()));
            // 分机控制指令
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
                // 敌我系统控制指令
                byteBuffer.putShort((short) 21496); // 包头 53F8
                byteBuffer.putShort(shorts); // 信息包序号
                String time = deviceWorkFlowCMD.getTimeCode(); // 解析时间
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
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getWorkPattern())); // 工作方式
                // 带宽选择解析
                String bandwidthChoose = deviceWorkFlowCMD.getBandwidthChoose();

                byteBuffer.put((byte) BinaryToDecimals(Long.parseLong(bandwidthChoose(bandwidthChoose))));
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getPulseChoice())); // 内外秒脉冲选择
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getAntennaSelection())); // 天线选择
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getIPReconsitution())); //分机IP重构
                byteBuffer.putShort(backups);// 备份
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getIfAcquisitionMode())); // 中频采集模式
                byteBuffer.putInt(0);  // 中频采集时间 未知
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW740())); // 740PDW个数 备份
                // FPGA重构标识 将接收过来的16进制转换成10进制
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getFPGAReconsitution(), 16));
                // DSP重构标识
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getDSPReconsitution(), 16));
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1030())); // 1030 PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1090())); // 1090 PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1059())); // 1059PDW个数备份
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW837())); // 837.5 PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1464())); // 1464PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1532())); // 1532PDW个数
                // 分机控制
                String hierarchicalControl = deviceWorkFlowCMD.getHierarchicalControl();
                byteBuffer.putInt(BinaryToDecimal(Integer.parseInt(hierarchicalControl(hierarchicalControl))));
            }
            // 512字节 多余补0
            int c = 512;
            byte[] byte1 = new byte[c - count * 48];
            byteBuffer.put(byte1);
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 统一发送信息
     */
    private static void sendMessage(String host, ByteBuffer byteBuffer) {
        Socket socket = (Socket) TCPThread.map.get(host);
        SocketChannel socketChannel = socket.getChannel();
        try {
            socketChannel.write(ByteBuffer.wrap(byteBuffer.array()));
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  分机控制字(解析String为bit)
    private void extensionControl(String eccs, ByteBuffer byteBuffer) {
        /**
         *  因为二进制的bit0位置是最右，而接收到web端的值bit0的位置是最左，所以先将数据反转
         */
        StringBuilder stringBuilders = new StringBuilder();
        stringBuilders.append(eccs);
        String ecc = stringBuilders.reverse().toString();  // 反转
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
         // 从目的地址开始叠加
        byte[] b = byteBuffer.array();
        int c = 0;
        for(int a=7;a<b.length;a++){
            String s = Integer.toBinaryString((b[a] & 0xFF) + 0x100).substring(1);
            c += BinaryToDecimal(Integer.parseInt(s));
        }
        return c;
    }

    // MAC地址信息解析
    private byte[] getMac(String macIP) {
        return SocketConfig.hexToByte(macIP);
    }

    // 敌我控制指令中的带宽选择解析
    private String bandwidthChoose(String bandwidthChooses) {
        StringBuilder stringBuilders = new StringBuilder();
        stringBuilders.append(bandwidthChooses);
        String bandwidthChoose = stringBuilders.reverse().toString();
        StringBuilder stringBuilder = new StringBuilder();
        // 截取bit7
        String bit7 = bandwidthChoose.substring(0, 1);
        if (bit7.equals("1")) {
            stringBuilder.append("01");
        } else if (bit7.equals("3")) {
            stringBuilder.append("11");
        }
        // 截取bit6
        String bit6 = bandwidthChoose.substring(1, 2);
        if (bit6.equals("1")) {
            stringBuilder.append("01");
        } else if (bit6.equals("3")) {
            stringBuilder.append("11");
        }
        // 截取bit5
        String bit5 = bandwidthChoose.substring(2, 3);
        if (bit5.equals("0")) {
            stringBuilder.append("00");
        } else if (bit5.equals("1")) {
            stringBuilder.append("01");
        }
        // 截取bit4
        String bit4 = bandwidthChoose.substring(3, 4);
        if (bit4.equals("0")) {
            stringBuilder.append("00");
        } else if (bit4.equals("1")) {
            stringBuilder.append("01");
        }
        // 截取bit0-3
        for (int i = 4; i < 6; i++) {
            String bit = bandwidthChoose.substring(i, i + 1);
            switch (bit) {
                case "0":
                    stringBuilder.append("00");
                    break;
                case "1":
                    stringBuilder.append("01");
                    break;
                case "2":
                    stringBuilder.append("10");
                    break;
                case "3":
                    stringBuilder.append("11");
                    break;
            }
        }
        return stringBuilder.toString();
    }

    // 敌我控制指令的分机控制
    private static String hierarchicalControl(String hierarchicalControl) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hierarchicalControl);
        String res = stringBuilder.reverse().toString();
        StringBuilder stringBuilder1 = new StringBuilder();
        /**
         * 因bit5-bit32 数据暂时未确定 所以先做补0处理 其实无需补0，int类型自然会补齐 但为了日后开发 留下接口以便提醒
         */
        for (int i = 0; i < 27; i++) {
            stringBuilder1.append("0");
        }
        String bit = res.substring(0, 1);
        switch (bit) {
            case "0":
                stringBuilder1.append("0000");
                break;
            case "1":
                stringBuilder1.append("0001");
                break;
            case "2":
                stringBuilder1.append("0010");
                break;
        }
        // bit0
        stringBuilder1.append(res, 1, 2);
        return stringBuilder1.toString();
    }

    // 获取时间偏移量
    public long getTimes() {
        // 获取1970-1-1 到现在的毫秒数 拼接成long类型
        int a = (int) System.currentTimeMillis() / 1000;
        StringBuilder stringBuilder = new StringBuilder();
        if (Integer.toBinaryString(a).length() < 32) {
            for (int d = 0; d < 32 - Integer.toBinaryString(a).length(); d++) {
                stringBuilder.append("0");
            }
        }
        stringBuilder.append(Integer.toBinaryString(a));
        for (int e = 0; e < 32; e++) {
            stringBuilder.append("0");
        }
        return Long.parseLong(stringBuilder.toString());
    }
}
