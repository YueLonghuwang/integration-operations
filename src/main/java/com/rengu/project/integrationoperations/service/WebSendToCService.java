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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rengu.project.integrationoperations.util.SocketConfig.BinaryToDecimal;
import static com.rengu.project.integrationoperations.util.SocketConfig.BinaryToDecimals;

/**
 * java端发送给c++端
 *
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:32
 */
@Service
@Slf4j
public class WebSendToCService {
    private byte backups = 0;
    private final CMDSerialNumberRepository cmdSerialNumberRepository;
    private short shorts = 0;
    private final HostRepository hostRepository;//当前连接的最大数·

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

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
        //String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        byteBuffer.putShort(shorts);  // 目的地址(设备ID号)
        byteBuffer.putShort(shorts);  // 源地址(设备ID号)
        byteBuffer.put(regionID); // 域ID(预留)
        byteBuffer.put(themeID); // 主题ID(预留)
        byteBuffer.putShort(messageCategory);  // 信息类别号 (各种交换的信息格式报分配唯一的编号)
        //byteBuffer.putLong(getTimes());    // 发报日期时间
        byteBuffer.putLong((long) 0);
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
            String networkIP1 = sendDeviceNetWorkParam.getNetworkIP1();
            //byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP1())); // 网络IP地址1
            //byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP1)));
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP1)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP1()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage1())); //网络端口信息1

            // 2
            //byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP2())); // 网络IP地址2
            String networkIP2 = sendDeviceNetWorkParam.getNetworkIP2();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP2)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP2()));// MAC地址2
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage2())); //网络端口信息2

            // 3
            //byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP3())); // 网络IP地址3
            String networkIP3 = sendDeviceNetWorkParam.getNetworkIP3();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP3)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP3()));// MAC地址3
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage3())); //网络端口信息3

            // 4
            //byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP4())); // 网络IP地址4
            String networkIP4 = sendDeviceNetWorkParam.getNetworkIP4();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP4)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP4()));// MAC地址4
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage4())); //网络端口信息4

            // 5
            //byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP5())); // 网络IP地址5
            String networkIP5 = sendDeviceNetWorkParam.getNetworkIP5();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP5)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP5()));// MAC地址5
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage5())); //网络端口信息5

            // 6
            //byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP6())); // 网络IP地址6
            String networkIP6 = sendDeviceNetWorkParam.getNetworkIP6();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP6)));
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
    public void sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, SystemControlCMD systemControlCMD, int count, String host, String updateAll, int serialNumber) {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendDeviceWorkFlowCMD(deviceWorkFlowCMD, systemControlCMD, count, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1094);//1094
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 1094, shorts, shorts, backups, backups, (short) 12295, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getTaskFlowNo())); //任务流水号
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getCmd()));  //指令有效标记
            // 分机控制指令（雷达侦查指令）
            for (int i = 1; i <= count; i++) {
                byteBuffer.putShort(SocketConfig.header);  //包头
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
                // 小包尾
                getPackageTheTail(byteBuffer);
                /* for (int j = 1; j <= count; j++) {}*/
                //雷达系统控制指令
                byteBuffer.putShort(SocketConfig.header);//包头1ACF
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPattern()));//工作模式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPeriod())); //工作周期
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getWorkPeriodNum()));//工作周期数
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getInitialFrequency())); //起始频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getTerminationFrequency())); //终止频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getSteppedFrequency()));// 频率步进
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBandWidthSelection()));//宽带选择
                byteBuffer.putShort(shorts);//备份
                byteBuffer.put(backups);
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection1()));//天线1选择
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection2()));//天线2选择
                byteBuffer.putShort(shorts);//备份
                //  射频一衰减
                StringBuilder stringBuilder = new StringBuilder();
                String attenuationRF1 = stringBuilder.reverse().append(systemControlCMD.getRadioFrequencyAttenuation1()).toString();
                byte bytess = (byte) BinaryToDecimal(Integer.parseInt(attenuationRF1));
                byteBuffer.put(bytess);
                //  射频一长电缆均衡衰减控制
                StringBuilder stringBuilders = new StringBuilder();
                String balancedAttenuationRF1 = stringBuilders.reverse().append(systemControlCMD.getAttenuationControl1()).toString();
                //byte bytesAttenuationRF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));
                byte bytesAttenuationRF1 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));
                byteBuffer.put(bytesAttenuationRF1);
                byteBuffer.putShort(shorts); //备份
                //  射频二衰减
                StringBuilder stringBuilder2 = new StringBuilder();
                String attenuationRF2 = stringBuilder2.reverse().append(systemControlCMD.getAttenuationControl2()).toString();
                byte byteAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(attenuationRF2));
                byteBuffer.put(byteAttenuationRF2);
                //射频二长电缆均衡衰减控制(最新)
                StringBuilder stringBuilderAttenuationRF2 = new StringBuilder();
                String balancedAttenuationRF2 = stringBuilderAttenuationRF2.reverse().append(systemControlCMD.getAttenuationControl2()).toString();
                byte bytesAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF2));
                byteBuffer.put(bytesAttenuationRF2);
                byteBuffer.putShort(shorts);
                // 中频一衰减(最新)
                StringBuilder cut1 = new StringBuilder();
                String midCut1 = cut1.reverse().append(systemControlCMD.getMidCut1()).toString();
                byte bytesAttenuationMF1 = (byte) BinaryToDecimal(Integer.parseInt(midCut1));
                byteBuffer.put(bytesAttenuationMF1);
                byteBuffer.putShort(shorts);
                byteBuffer.put(backups);
                //中频二衰减(最新)
                StringBuilder cut2 = new StringBuilder();
                String midCut2 = cut2.reverse().append(systemControlCMD.getMidCut2()).toString();
                byte bytesAttenuationMF2 = (byte) BinaryToDecimal(Integer.parseInt(midCut2));
                byteBuffer.put(bytesAttenuationMF2);
                //衰减吗控制方式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAttenuationCodeControlMode()));
                //备份
                byteBuffer.putShort(shorts);
                //自检源衰减
                byteBuffer.put(backups);
                //byteBuffer.put(Byte.parseByte(systemControlCMD.getSelfCheckingSourceAttenuation()));
                //  脉内引导批次开关
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumberSwitch()));
                //  脉内引导批次号
                byteBuffer.put(backups);
                //byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumber()));
                //  故障检测门限
                byteBuffer.put(backups);
                //byteBuffer.put(Byte.parseByte(systemControlCMD.getFaultDetectionThreshold()));
                //  定时时间码
                String time = systemControlCMD.getTimingTimeCode();
                //  转换2进制
                StringBuilder month = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(0, 2))));
                StringBuilder day = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(2, 4))));
                StringBuilder hour = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(4, 6))));
                StringBuilder minute = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(6, 8))));
                StringBuilder second = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(8, 10))));
                //  拼接秒数
                int seconds = second.length();
                for (int ii = 0; ii < 11 - seconds; ii++) {
                    second.insert(0, "0");
                }
                //  拼接分钟
                int minutes = minute.length();
                for (int ii = 0; ii < 6 - minutes; ii++) {
                    minute.insert(0, "0");
                }
                //  拼接时钟
                int hours = hour.length();
                for (int ii = 0; ii < 5 - hours; ii++) {
                    hour.insert(0, "0");
                }
                //  拼接天数
                int days = day.length();
                for (int ii = 0; ii < 5 - days; ii++) {
                    day.insert(0, "0");
                }
                //  拼接月份
                int months = month.length();
                for (int ii = 0; ii < 4 - months; ii++) {
                    month.insert(0, "0");
                }
                String thisTime = month.toString() + day.toString() + hour.toString() + minute.toString() + second.toString();
                byte[] bytes1 = new byte[4];
                bytes1[0] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(0, 8)));
                bytes1[1] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(8, 16)));
                bytes1[2] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(16, 24)));
                bytes1[3] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(24)));
                for (byte cc : bytes1) {
                    byteBuffer.put(cc);
                }
                //  单次执行指令集所需时间
                byteBuffer.putShort(shorts);
                //byteBuffer.putShort(Short.parseShort(systemControlCMD.getTimeRequired()));
                //小包尾
                getPackageTheTail(byteBuffer);
            }
            // 512字节 多余补0
            int a = 512;
            byte[] bytes = new byte[a - count * 80];
            byteBuffer.put(bytes);
            for (int i = 1; i <= count; i++) {
                // 敌我系统控制指令
                byteBuffer.putShort(SocketConfig.header); // 包头 53F8
                byteBuffer.put(backups); // 信息包序号
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getWorkPattern())); // 工作模式（0：自检 1：工作）
                // 带宽选择解析
                String bandwidthChoose = deviceWorkFlowCMD.getBandwidthChoose();
                byteBuffer.put((byte) BinaryToDecimals(Long.parseLong(bandwidthChoose(bandwidthChoose))));
                //byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getPulseChoice())); // 秒脉冲选择
                if (deviceWorkFlowCMD.getPulseChoice().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (deviceWorkFlowCMD.getPulseChoice().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getSelfChoose()));// //字频模式选择
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getPointSelection())); //自检频点选择

                String time = deviceWorkFlowCMD.getTimeCode(); // 解析时间（同步时间）
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
                // 分机控制
                String hierarchicalControl = deviceWorkFlowCMD.getHierarchicalControl();
                byteBuffer.putInt(BinaryToDecimal((int) Long.parseLong(hierarchicalControl(hierarchicalControl))));
                byteBuffer.putInt(shorts); // 740全脉冲个数 备份
                byteBuffer.putInt(shorts); // 837.5 PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1030())); // 1030 PDW个数
                byteBuffer.putInt(shorts); // 1059PDW个数备份
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1090())); // 1090 PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1464())); // 1464PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1532())); // 1532PDW个数
                byteBuffer.put(backups); //740中频个数
                byteBuffer.put(backups); //1030中频个数
                byteBuffer.put(backups); //1090中频个数
                byteBuffer.put(backups);  // 中频采集时间 未知
                byteBuffer.putLong(backups); //故障检测门限（默认0x1111111111111111111111111)
                byteBuffer.put(backups);//网络包计数
                byteBuffer.put(backups);//备份
                //  包尾
                getPackageTheTail(byteBuffer);
            }
            // 512字节 多余补0
            int c = 512;
            byte[] byte1 = new byte[c - count * 64];
            byteBuffer.put(byte1);
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer); //  帧尾
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 设备工作流程控制指令(雷达分机指令)
     */
    public void sendExtensionInstructionsCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, int count, String host, String updateAll, int serialNumber, int radarExtensionNum) {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendExtensionInstructionsCMD(deviceWorkFlowCMD, count, allHost.getHost(), "0", serialNumbers, radarExtensionNum);
            }
        } else {
            // todo 1.放包头 checked sendSystemControlCmdFormat
            // todo 2. 放长度 计算总长度 固定值1000多
            // todo 3. 放流水号 getTaskFlowNo
            // todo 4. 放指令有效标记 静态值0、1、2
            // todo 5. 放第一个512 下面的代码
            // todo 6. 放第二个512 这个看具体的协议
            ByteBuffer byteBuffer = ByteBuffer.allocate(568);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            //大包头
            sendSystemControlCmdFormat(byteBuffer, 568, shorts, shorts, backups, backups, (short) 12296, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            for (int i = 1; i <= radarExtensionNum; i++) {
                // 分机控制指令
                byteBuffer.putShort(SocketConfig.header); //小包头
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
            byte[] bytes = new byte[a - radarExtensionNum * 32];
            byteBuffer.put(bytes);
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和// 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer); //  帧尾
            sendMessage(host, byteBuffer);
        }
    }

    /**
     *设备工作流程控制指令(雷达系统指令)
     */
    public void sendSystemInstructionsCMD(SystemControlCMD systemControlCMD, int count, String host, String updateAll, int serialNumber, int radarSystemNum) {
        //String message = null;
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendSystemInstructionsCMD(systemControlCMD, count, allHost.getHost(), "0", serialNumbers, radarSystemNum);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(568);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            //大包头
            sendSystemControlCmdFormat(byteBuffer, 568, shorts, shorts, backups, backups, (short) 12297, 0, serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            for (int j = 1; j <= radarSystemNum; j++) {
                byteBuffer.putShort(SocketConfig.header);//包头1ACF
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPattern()));//工作模式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPeriod())); //工作周期
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getWorkPeriodNum()));//工作周期数
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getInitialFrequency())); //起始频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getTerminationFrequency())); //终止频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getSteppedFrequency()));// 频率步进
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBandWidthSelection()));//宽带选择
                byteBuffer.putShort(shorts);//备份
                byteBuffer.put(backups);
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection1()));//天线1选择
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection2()));//天线2选择
                byteBuffer.putShort(shorts);//备份
                //  射频一衰减(最新)
                StringBuilder stringBuilder = new StringBuilder();
                String attenuationRF1 = stringBuilder.reverse().append(systemControlCMD.getRadioFrequencyAttenuation1()).toString();
                byte bytess = (byte) BinaryToDecimal(Integer.parseInt(attenuationRF1));
                byteBuffer.put(bytess);
                //  射频一长电缆均衡衰减控制
                StringBuilder stringBuilders = new StringBuilder();
                String balancedAttenuationRF1 = stringBuilders.reverse().append(systemControlCMD.getAttenuationControl1()).toString();
                //byte bytesAttenuationRF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));
                byte bytesAttenuationRF1 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));
                byteBuffer.put(bytesAttenuationRF1);
                byteBuffer.putShort(shorts); //备份
                //  射频二衰减
                StringBuilder stringBuilder2 = new StringBuilder();
                String attenuationRF2 = stringBuilder2.reverse().append(systemControlCMD.getAttenuationControl2()).toString();
                byte byteAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(attenuationRF2));
                byteBuffer.put(byteAttenuationRF2);
                //射频二长电缆均衡衰减控制(最新)
                StringBuilder stringBuilderAttenuationRF2 = new StringBuilder();
                String balancedAttenuationRF2 = stringBuilderAttenuationRF2.reverse().append(systemControlCMD.getAttenuationControl2()).toString();
                byte bytesAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF2));
                byteBuffer.put(bytesAttenuationRF2);
                byteBuffer.putShort(shorts);
                // 中频一衰减(最新)
                StringBuilder cut1 = new StringBuilder();
                String midCut1 = cut1.reverse().append(systemControlCMD.getMidCut1()).toString();
                byte bytesAttenuationMF1 = (byte) BinaryToDecimal(Integer.parseInt(midCut1));
                byteBuffer.put(bytesAttenuationMF1);
                byteBuffer.putShort(shorts);
                byteBuffer.put(backups);
                //中频二衰减(最新)
                StringBuilder cut2 = new StringBuilder();
                String midCut2 = cut2.reverse().append(systemControlCMD.getMidCut2()).toString();
                byte bytesAttenuationMF2 = (byte) BinaryToDecimal(Integer.parseInt(midCut2));
                byteBuffer.put(bytesAttenuationMF2);
                //衰减吗控制方式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAttenuationCodeControlMode()));
                //备份
                byteBuffer.putShort(shorts);
                //自检源衰减
                byteBuffer.put(backups);
                //byteBuffer.put(Byte.parseByte(systemControlCMD.getSelfCheckingSourceAttenuation()));
                //  脉内引导批次开关
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumberSwitch()));
                //  脉内引导批次号
                byteBuffer.put(backups);
                //byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumber()));
                //  故障检测门限
                byteBuffer.put(backups);
                //byteBuffer.put(Byte.parseByte(systemControlCMD.getFaultDetectionThreshold()));
                //  定时时间码
                String time = systemControlCMD.getTimingTimeCode();
                //  转换2进制
                StringBuilder month = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(0, 2))));
                StringBuilder day = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(2, 4))));
                StringBuilder hour = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(4, 6))));
                StringBuilder minute = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(6, 8))));
                StringBuilder second = new StringBuilder(Integer.toBinaryString(Integer.parseInt(time.substring(8, 10))));
                //  拼接秒数
                int seconds = second.length();
                for (int ii = 0; ii < 11 - seconds; ii++) {
                    second.insert(0, "0");
                }
                //  拼接分钟
                int minutes = minute.length();
                for (int ii = 0; ii < 6 - minutes; ii++) {
                    minute.insert(0, "0");
                }
                //  拼接时钟
                int hours = hour.length();
                for (int ii = 0; ii < 5 - hours; ii++) {
                    hour.insert(0, "0");
                }
                //  拼接天数
                int days = day.length();
                for (int ii = 0; ii < 5 - days; ii++) {
                    day.insert(0, "0");
                }
                //  拼接月份
                int months = month.length();
                for (int ii = 0; ii < 4 - months; ii++) {
                    month.insert(0, "0");
                }
                String thisTime = month.toString() + day.toString() + hour.toString() + minute.toString() + second.toString();
                byte[] bytes1 = new byte[4];
                bytes1[0] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(0, 8)));
                bytes1[1] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(8, 16)));
                bytes1[2] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(16, 24)));
                bytes1[3] = (byte) BinaryToDecimal(Integer.parseInt(thisTime.substring(24)));
                for (byte cc : bytes1) {
                    byteBuffer.put(cc);
                }
                //  单次执行指令集所需时间
                byteBuffer.putShort(shorts);
                //byteBuffer.putShort(Short.parseShort(systemControlCMD.getTimeRequired()));
                //小包尾
                getPackageTheTail(byteBuffer);
            }
            // 512字节 多余补0
            int c = 512;
            byte[] byte1 = new byte[c - radarSystemNum * 48];
            byteBuffer.put(byte1);
            byteBuffer.putInt(getByteCount(byteBuffer)); //校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer); //  帧尾
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
        //  合路选择（0：融合 1：全脉冲融合 2.辐射源融合）bit11-bit12
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
        for (int a = 7; a < b.length; a++) {
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
        // bit7(固定0)
        stringBuilder.append(bandwidthChoose, 0, 1);
        //bit6（固定1）
        stringBuilder.append(bandwidthChoose, 1, 2);
        // 截取bit2-bit3(由于bit5，bit4没有值，用0补充)
        String bit3 = bandwidthChoose.substring(2, 3);
        if (bit3.equals("1")) {
            stringBuilder.append("0000");
        } else if (bit3.equals("3")) {
            stringBuilder.append("0011");
        }
        // 截取bit0-bit1
        String bit1 = bandwidthChoose.substring(2, 3);
        if (bit1.equals("1")) {
            stringBuilder.append("00");
        } else if (bit1.equals("3")) {
            stringBuilder.append("11");
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
        //bit17-20
        String bit20 = res.substring(0, 1);
        switch (bit20) {
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
        //bit13-16
        String bit16 = res.substring(1, 2);
        switch (bit16) {
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
        //bit9-12
        String bit12 = res.substring(2, 3);
        switch (bit12) {
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
        //bit5-8
        String bit8 = res.substring(3, 4);
        switch (bit8) {
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
        //bit1-4
        String bit4 = res.substring(3, 4);
        switch (bit4) {
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
        stringBuilder1.append(res, 4, 5);
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
    //网络ip地址String类型放入4byte
    private static String networkIP1(String networkIP1) {
        int[] ip1 = new int[4];
        //先找到IP地址字符串中.的位置
        int position1 = networkIP1.indexOf(".");
        int position2 = networkIP1.indexOf(".", position1 + 1);
        int position3 = networkIP1.indexOf(".", position2 + 1);
        //将每个.之间的字符串转换成整型
        ip1[0] = Integer.parseInt(networkIP1.substring(0, position1));
        ip1[1] = Integer.parseInt(networkIP1.substring(position1 + 1, position2));
        ip1[2] = Integer.parseInt(networkIP1.substring(position2 + 1, position3));
        ip1[3] = Integer.parseInt(networkIP1.substring(position3 + 1));
        Integer ipArr = (ip1[0] << 24) + (ip1[1] << 16) + (ip1[2] << 8) + (ip1[3]);
        return ipArr.toString();
    }
}
