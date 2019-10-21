package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.repository.CMDSerialNumberRepository;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.repository.TimingTaskRepository;
import com.rengu.project.integrationoperations.thread.TCPThread;
import com.rengu.project.integrationoperations.util.JsonUtils;
import com.rengu.project.integrationoperations.util.SocketConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rengu.project.integrationoperations.util.SocketConfig.BinaryToDecimal;

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
    private final HostRepository hostRepository;// 当前连接的最大数·
    private final TimingTaskRepository timingTaskRepository;
    private final SchedulerFactoryBean schedulerFactoryBean;
    private SimpMessagingTemplate simpMessagingTemplate;
    DynamicJobService jobService;

    @Autowired
    private SysLogService sysLogService;

    public WebSendToCService(CMDSerialNumberRepository cmdSerialNumberRepository,
                             SchedulerFactoryBean schedulerFactoryBean, TimingTaskRepository timingTaskRepository,
                             HostRepository hostRepository) { //

        this.cmdSerialNumberRepository = cmdSerialNumberRepository;
        this.hostRepository = hostRepository;
        this.timingTaskRepository = timingTaskRepository;
//		this.jobService = jobService;
        this.schedulerFactoryBean = schedulerFactoryBean;
    }

    // 系统控制指令帧格式说明（头部固定信息）
    private void sendSystemControlCmdFormat(ByteBuffer byteBuffer, int dataLength, short purPoseAddress,
                                            short sourceAddress, byte regionID, byte themeID, short messageCategory, long sendingDateTime,
                                            int seriesNumber, int packageSum, int currentNum, int dataLengthSum, short version, int retain1,
                                            short retain2) {
        // 十六进制转换成十进制
        byteBuffer.putInt(2122389735); // 报文头 7E8118E7
        byteBuffer.putInt(dataLength); // 当前包数据长度
        // 获取当前pid (无法获取目标的pid，暂时拿源地址的PID)
        // String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        byteBuffer.putShort(shorts); // 目的地址(设备ID号)
        byteBuffer.putShort(sourceAddress); // 源地址(设备ID号)
        byteBuffer.put(regionID); // 域ID(预留)
        byteBuffer.put(themeID); // 主题ID(预留)
        byteBuffer.putShort(messageCategory); // 信息类别号 (各种交换的信息格式报分配唯一的编号)
        // byteBuffer.putLong(getTimes()); // 发报日期时间
        byteBuffer.putLong((long) 0);
        byteBuffer.putInt(seriesNumber); // 序列号 (同批数据的序列号相同，不同批数据的序列号不同)
        byteBuffer.putInt(1); // 包总数 (当前发送的数据，总共分成几个包发送。默认一包)
        byteBuffer.putInt(currentNum); // 当前包号 (当前发送的数据包序号。从1开始，当序列号不同时，当前包号清零，从1开始。)
        byteBuffer.putInt(1536); // 数据总长度
        // 高八位 主版本号 低八位 副版本号 0001 1000
        byteBuffer.putShort((short) 24); // 版本号
        byteBuffer.putInt(retain1); // 保留字段
        byteBuffer.putShort(retain2); // 保留字段
    }

    /**
     * 3.4.6.3 设备自检指令
     *
     * @throws IOException
     */
    public void sendDeviceCheckCMD(DeviceCheckCMD deviceCheckCMD, String host, String updateAll, int serialNumber)
            throws IOException {
        // 群发消息
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            // 调用序号自增
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendDeviceCheckCMD(deviceCheckCMD, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(84);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 84, backups, backups, backups, backups, (short) 12290, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(0); // 信息长度 待定
            byteBuffer.putLong(Long.parseLong(deviceCheckCMD.getTaskFlowNo()));// 任务流水号
            byteBuffer.put(Byte.parseByte(deviceCheckCMD.getCheckType())); // 自检类型

            byteBuffer.putShort(Short.parseShort(deviceCheckCMD.getCheckPeriod())); // 自检周期
            byte num = (byte) (Integer.parseInt(deviceCheckCMD.getCheckNum()));
            byteBuffer.put(num); // 自检数量
            byteBuffer.putInt(Integer.parseInt(deviceCheckCMD.getSingleMachineCode())); // 自检单机代码
            // 雷达
            byteBuffer.putShort(Short.parseShort(deviceCheckCMD.getRadarSelfChecking())); // 自检频率
            byteBuffer.put(Byte.parseByte(deviceCheckCMD.getRadarBranChoose()));// 带宽选择
            byteBuffer.put(Byte.parseByte(deviceCheckCMD.getRadarSelfChoose()));// 自检模式选择
            // 敌我
            byteBuffer.putShort(Short.parseShort(deviceCheckCMD.getEnemyAndUsSelfChecking())); // 自检频率
            byteBuffer.put(Byte.parseByte(deviceCheckCMD.getEnemyAndUsBranChoose()));// 带宽选择
            byteBuffer.put(Byte.parseByte(deviceCheckCMD.getEnemyAndUsSelfChoose()));// 自检模式选择

            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            // 帧尾
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
            SysLogEntity sysLogEntity = new SysLogEntity();
            String action = JsonUtils.toJson(deviceCheckCMD);
            sysLogEntity.setUserAction("设备自检");
            sysLogEntity.setActionDescription(action);
            sysLogEntity.setCreateTime(new Date());
            sysLogEntity.setHost(host);
            sysLogService.saveLog(sysLogEntity);
        }
    }

    /**
     * 3.4.6.4 系统校时指令
     *
     * @throws IOException
     */
    public void sendSystemTiming(SystemSendTimingCMD systemSendTimingCMD, String host, String updateAll,
                                 int serialNumber) throws IOException {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendSystemTiming(systemSendTimingCMD, allHost.getHost(), "0", serialNumbers);
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
            sendSystemControlCmdFormat(byteBuffer, 77, shorts, shorts, backups, backups, (short) 12291, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(systemSendTimingCMD.getTimeNow()));
            byteBuffer.put(Byte.parseByte(systemSendTimingCMD.getTimingPattern()));
            byteBuffer.putLong(Long.parseLong(systemSendTimingCMD.getTime()));
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
            // 发送系统校时指令日志
            SysLogEntity sysLogEntity = new SysLogEntity();
            String str = JsonUtils.toJson(systemSendTimingCMD);
            sysLogEntity.setUserAction("系统校时指令");
            sysLogEntity.setActionDescription(str);
            sysLogEntity.setCreateTime(new Date());
            sysLogEntity.setHost(host);
            sysLogService.saveLog(sysLogEntity);
        }
    }

    /**
     * 3.4.6.5 设备复位
     *
     * @throws IOException
     */
    public void sendDeviceRestoration(DeviceRestorationSendCMD deviceRestorationSendCMD, String host, String updateAll,
                                      int serialNumber) throws IOException {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                try {
                    sendDeviceRestoration(deviceRestorationSendCMD, allHost.getHost(), "0", serialNumbers);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }

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
            sendSystemControlCmdFormat(byteBuffer, 69, shorts, shorts, backups, backups, (short) 12292, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(deviceRestorationSendCMD.getTimeNow())); // 任务流水号
            byteBuffer.put(Byte.parseByte(deviceRestorationSendCMD.getExecutePattern())); // 执行方式
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
            // 记录操作日
            String type = JsonUtils.toJson(deviceRestorationSendCMD);
            SysLogEntity sysLogEntity = new SysLogEntity();
            sysLogEntity.setUserAction("远程开关机指令");
            sysLogEntity.setCreateTime(new Date());
            sysLogEntity.setHost(host);
            sysLogEntity.setActionDescription(type);
            sysLogService.saveLog(sysLogEntity);
        }
    }

    /**
     * 3.4.6.6 软件版本远程更新
     *
     * @throws IOException
     */
    public void sendSoftwareUpdateCMD(String timeNow, String cmd, String softwareID, String host, String updateAll,
                                      int serialNumber) throws IOException {
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
            sendSystemControlCmdFormat(byteBuffer, 72, shorts, shorts, backups, backups, (short) 12293, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(2); // 信息长度
            byteBuffer.putLong(Long.parseLong(timeNow)); // 任务流水号
            byteBuffer.putShort(Short.parseShort(cmd)); // 指令操作码
            byteBuffer.putShort(Short.parseShort(softwareID)); // 软件ID号
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 3.4.6.7 设备网络参数更新指令
     *
     * @throws IOException
     */
    public void sendDeviceNetworkCMD(SendDeviceNetWorkParam sendDeviceNetWorkParam, String host, String updateAll,
                                     int serialNumber) throws IOException {
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
            sendSystemControlCmdFormat(byteBuffer, 156, shorts, shorts, backups, backups, (short) 12294, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(3); // 信息长度
            byteBuffer.putLong(Long.parseLong(sendDeviceNetWorkParam.getTaskFlowNo())); // 任务流水号
            byteBuffer.putShort(Short.parseShort(sendDeviceNetWorkParam.getCmd())); // 指令操作码
            byteBuffer.putShort(Short.parseShort(sendDeviceNetWorkParam.getNetworkID())); // 网络终端ID号

            // 1
            String networkIP1 = sendDeviceNetWorkParam.getNetworkIP1();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP1)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP1()));// MAC地址1
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage1())); // 网络端口信息1

            // 2
            // byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP2()));
            // // 网络IP地址2
            String networkIP2 = sendDeviceNetWorkParam.getNetworkIP2();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP2)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP2()));// MAC地址2
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage2())); // 网络端口信息2

            // 3
            // byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP3()));
            // // 网络IP地址3
            String networkIP3 = sendDeviceNetWorkParam.getNetworkIP3();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP3)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP3()));// MAC地址3
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage3())); // 网络端口信息3

            // 4
            // byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP4()));
            // // 网络IP地址4
            String networkIP4 = sendDeviceNetWorkParam.getNetworkIP4();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP4)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP4()));// MAC地址4
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage4())); // 网络端口信息4

            // 5
            // byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP5()));
            // // 网络IP地址5
            String networkIP5 = sendDeviceNetWorkParam.getNetworkIP5();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP5)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP5()));// MAC地址5
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage5())); // 网络端口信息5

            // 6
            // byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkIP6()));
            // // 网络IP地址6
            String networkIP6 = sendDeviceNetWorkParam.getNetworkIP6();
            byteBuffer.putInt(Integer.parseInt(networkIP1(networkIP6)));
            byteBuffer.put(getMac(sendDeviceNetWorkParam.getNetworkMacIP6()));// MAC地址6
            byteBuffer.putInt(Integer.parseInt(sendDeviceNetWorkParam.getNetworkMessage6())); // 网络端口信息6
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
            getBigPackageTheTail(byteBuffer);
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 3.4.6.8 设备工作流程控制指令
     *
     * @throws IOException
     */
    public void sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, SystemControlCMD systemControlCMD,
                                      Integer count, String host, String updateAll, int serialNumber) throws IOException {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendDeviceWorkFlowCMD(deviceWorkFlowCMD, systemControlCMD, count, allHost.getHost(), "0",
                        serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1094);// 1094
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 1094, shorts, shorts, backups, backups, (short) 12295, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
            byteBuffer.putInt(1); // 信息长度
            byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getTaskFlowNo())); // 任务流水号
            byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getCmd())); // 指令有效标记
            // 分机控制指令（雷达侦查指令）
            for (int i = 1; i <= count; i++) {
                byteBuffer.putShort(SocketConfig.header); // 包头
                byte pulse = Byte.parseByte(deviceWorkFlowCMD.getPulse()); // 内外秒脉冲选择
                byteBuffer.put(pulse);
                byteBuffer.put(backups); // 备份
                // 分机控制字
                byte[] con = extensionControl(deviceWorkFlowCMD.getExtensionControlCharacter());
                byteBuffer.put(con[0]);
                byteBuffer.put(con[1]);
                byteBuffer.put(backups); // 备份
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getThreshold())); // 检测门限调节
                byteBuffer.putShort((Short.parseShort(deviceWorkFlowCMD.getOverallpulse())));// 上传脉冲数
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getMinamplitude()));// 最小幅度
                byte c = (byte) (Integer.parseInt(deviceWorkFlowCMD.getMaxamplitude()) & 0xff);
                byteBuffer.put(c);// 最大幅度
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMinPulsewidth()));// 最小脉宽
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMaxPulsewidth()));// 最大脉宽
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMaximumFrequency()));// 筛选最大频率
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMinimumFrequency()));// 筛选最小频率
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMaximumFrequency()));// 屏蔽最大频率
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMinimumFrequency()));// 屏蔽最小频率
                // 默认值更新标记
                if (deviceWorkFlowCMD.getDefalutUpdate().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (deviceWorkFlowCMD.getDefalutUpdate().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                byteBuffer.putShort(shorts); // 备份
                byteBuffer.put(backups); // 备份
                byteBuffer.putShort(shorts); // 备份
                // 小包尾
                getPackageTheTail(byteBuffer);
                // 雷达系统控制指令
                byteBuffer.putShort(SocketConfig.header);// 包头1ACF
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPattern()));// 工作模式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPeriod())); // 工作周期
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getWorkPeriodNum()));// 工作周期数
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getInitialFrequency())); // 起始频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getTerminationFrequency())); // 终止频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getSteppedFrequency()));// 频率步进
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBandWidthSelection()));// 带宽选择
                byteBuffer.putShort(shorts);// 备份
                byteBuffer.put(backups); // 备份
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection1()));// 天线1选择(页面不展示)
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection2()));// 天线2选择(页面不展示)
                byteBuffer.putShort(shorts);// 备份
                // 射频一衰减(最新要求改成射频2：6-18Hz射频衰减1)
                // 0:不衰减 1：衰减30db
                if (systemControlCMD.getRadioFrequencyAttenuation2().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (systemControlCMD.getRadioFrequencyAttenuation2().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                // 射频一长电缆均衡衰减控制
//				StringBuilder stringBuilders = new StringBuilder();
//				String balancedAttenuationRF1 = stringBuilders.reverse()
//						.append(systemControlCMD.getAttenuationControl1()).toString();
//				byte bytesAttenuationRF1 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));
//				byteBuffer.put(bytesAttenuationRF1);
                byteBuffer.put(backups); // 射频一长电缆均衡衰减控制
                byteBuffer.putShort(shorts); // 备份
                // 射频二衰减(改名为射频1：6-18Hz射频衰减)
                // 射频一衰减(最新要求改成射频2：6-18Hz射频衰减)
                // 0:不衰减 1：衰减30db
                if (systemControlCMD.getRadioFrequencyAttenuation1().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (systemControlCMD.getRadioFrequencyAttenuation1().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                // 射频二长电缆均衡衰减控制(最新)
//				StringBuilder stringBuilderAttenuationRF2 = new StringBuilder();
//				String balancedAttenuationRF2 = stringBuilderAttenuationRF2.reverse()
//						.append(systemControlCMD.getAttenuationControl2()).toString();
//				byte bytesAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF2));
//				byteBuffer.put(bytesAttenuationRF2);
                byteBuffer.put(backups);// 射频二长电缆均衡衰减控制
                byteBuffer.putShort(shorts);
                // (最新为中频2衰减0.5dB)
                byte mid2 = (byte) ((byte) Double.parseDouble(systemControlCMD.getMidCut2()) * 0.5);
                byteBuffer.put(mid2); // 中频衰减1
                byteBuffer.putShort(shorts); // 备份
                byteBuffer.put(backups);
                // 中频1衰减(最新)
                byte mid1 = (byte) ((byte) Double.parseDouble(systemControlCMD.getMidCut1()) * 0.5);
                byteBuffer.put(mid1);
                // 衰减码控制方式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAttenuationCodeControlMode()));
                // 自检模式选择
                byteBuffer.put(Byte.parseByte(systemControlCMD.getRadarSelfChoose()));
                // 备份
                byteBuffer.put(backups);
                // 自检源衰减
                byteBuffer.put(Byte.parseByte(systemControlCMD.getSelfCheckingSourceAttenuation()));
                // 脉内引导批次开关
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumberSwitch()));
                // 脉内引导批次号
                byteBuffer.put(backups);
                // byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumber()));
                // 故障检测门限
                byteBuffer.put(Byte.parseByte(systemControlCMD.getFaultDetectionThreshold()));
                // 定时时间码

                // 转换2进制
                // 定时时间码
                String time = systemControlCMD.getTimingTimeCode();
                // 转换月
                String monthString = time.substring(0, 2);
                // 转换日
                String dayString = time.substring(2, 4);
                // 转换时
                String hourString = time.substring(4, 6);
                // 转换分
                String minString = time.substring(6, 8);
                // 转换秒
                String secondString = time.substring(8, 10);

                // 转2进制月
                String monthBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(monthString));
                int monthLeftNum = 4 - monthBinaryString.length();
                for (int j = 0; j < monthLeftNum; j++) {
                    monthBinaryString = "0" + monthBinaryString;
                }
                // 转2进制日期
                String dayBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(dayString));
                int dayLeftNum = 5 - dayBinaryString.length();
                for (int j = 0; j < dayLeftNum; j++) {
                    dayBinaryString = "0" + dayBinaryString;
                }
                // 转2进制小时
                String hourBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(hourString));
                int hourLeftNum = 5 - hourBinaryString.length();
                for (int j = 0; j < hourLeftNum; j++) {
                    hourBinaryString = "0" + hourBinaryString;
                }
                // 转2进制分钟
                String minBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(minString));
                int minLeftNum = 6 - minBinaryString.length();
                for (int j = 0; j < minLeftNum; j++) {
                    minBinaryString = "0" + minBinaryString;
                }
                // 转2进制秒
                String secondBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(secondString));
                int secondLeftNum = 11 - secondBinaryString.length();
                for (int j = 0; j < secondLeftNum; j++) {
                    secondBinaryString = "0" + secondBinaryString;
                }
                String binaryString = monthBinaryString + dayBinaryString + hourBinaryString + minBinaryString
                        + secondBinaryString;
                int timer = Integer.parseUnsignedInt(binaryString, 2);
                byteBuffer.putInt(timer);

                // 单次执行指令集所需时间
                byteBuffer.putShort(shorts);
                // byteBuffer.putShort(Short.parseShort(systemControlCMD.getTimeRequired()));
                // 小包尾
                getPackageTheTail(byteBuffer);
            }
            // 512字节 多余补0
            int a = 512;
            byte[] bytes = new byte[a - count * 80];
            byteBuffer.put(bytes);
            for (int i = 1; i <= count; i++) {
                // 敌我系统控制指令
                byteBuffer.putShort(SocketConfig.header); // 包头 1ACF
                byteBuffer.put(backups); // 信息包序号
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getWorkPattern1())); // 工作模式（0：自检 1：工作）
                // 带宽选择解析
                // String bandwidthChoose = deviceWorkFlowCMD.getBandwidthChoose();
                // byteBuffer.put((byte)
                // BinaryToDecimals(Long.parseLong(bandwidthChoose(bandwidthChoose))));
                // byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getPulseChoice())); // 秒脉冲选择
                if (deviceWorkFlowCMD.getPulseChoice().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (deviceWorkFlowCMD.getPulseChoice().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getSelfChoose()));// //自检模式选择
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getPointSelection())); // 自检频点选择
                String time = deviceWorkFlowCMD.getTimeCode(); // 解析时间（同步时间）
                if (time.isEmpty()) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:SSS:dd:MM:yyyy");
                    time = simpleDateFormat.format(new Date());
                }
                byte hour = Byte.parseByte(time.substring(0, 2));
                byteBuffer.put(hour);
                byte minute = Byte.parseByte(time.substring(3, 5));
                byteBuffer.put(minute);
                // 秒>毫秒>int>16进制
                byteBuffer.putShort(Short.parseShort(time.substring(6, 9)));
                byte day = Byte.parseByte(time.substring(10, 12));
                byteBuffer.put(day);
                byte month = Byte.parseByte(time.substring(13, 15));
                byteBuffer.put(month);
                short year = Short.parseShort(time.substring(16));
                byteBuffer.putShort(year);
                // 分机控制
                String hierarchicalControl = deviceWorkFlowCMD.getHierarchicalControl();
                byte[] temp = hierarchicalControl(hierarchicalControl);
                byteBuffer.put(temp[0]);
                byteBuffer.put(temp[1]);
                byteBuffer.put(temp[2]);
                byteBuffer.put(temp[3]);
//				byteBuffer.putInt(BinaryToDecimal((int) Long.parseLong(hierarchicalControl(hierarchicalControl))));
                byteBuffer.putInt(shorts); // 740全脉冲个数 备份
                byteBuffer.putInt(shorts); // 837.5 PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1030())); // 1030 PDW个数
                byteBuffer.putInt(shorts); // 1059PDW个数备份
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1090())); // 1090 PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1464())); // 1464PDW个数
                byteBuffer.putInt(Integer.parseInt(deviceWorkFlowCMD.getPDW1532())); // 1532PDW个数
                byteBuffer.put(backups); // 740中频个数
                byteBuffer.put(backups); // 1030中频个数
                byteBuffer.put(backups); // 1090中频个数
                byteBuffer.put(backups); // 中频采集时间 未知
                byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getFaultDetection())); // 故障检测门限（默认0x1111111111111111111111111)
                byteBuffer.put(backups);// 网络包计数
                byteBuffer.put(backups);// 备份
                // 包尾
                getPackageTheTail(byteBuffer);
            }
            // 512字节 多余补0
            int c = 512;
            byte[] byte1 = new byte[c - count * 64];
            byteBuffer.put(byte1);
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer); // 帧尾
            // 发送信息
            sendMessage(host, byteBuffer);
        }
    }

    /**
     * 设备工作流程控制指令(雷达分机指令)(单发)
     *
     * @param host
     * @param updateAll
     * @param serialNumber
     * @throws IOException
     */

    // 1。设备工作流程控制指令(雷达分机指令)（单发）
    public void sendExtensionInstructionsCMD(ArrayList<RadarDeviceCMD> deviceWorkFlowCMDs, String host,
                                             String updateAll, int serialNumber) throws IOException {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendExtensionInstructionsCMD(deviceWorkFlowCMDs, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            // todo 1.放包头 checked sendSystemControlCmdFormat
            // todo 2. 放长度 计算总长度 固定值1000多
            // todo 3. 放流水号 getTaskFlowNo
            // todo 4. 放指令有效标记 静态值0、1、2
            // todo 5. 放第一个512 下面的代码
            // todo 6. 放第二个512 这个看具体的协议
            ByteBuffer byteBuffer = ByteBuffer.allocate(1094);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 大包头
            sendSystemControlCmdFormat(byteBuffer, 1094, shorts, shorts, backups, backups, (short) 12295, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
//			byteBuffer.putInt(1); // 信息长度
//			byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getTaskFlowNo())); // 任务流水号
//			byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getCmd())); // 指令有效标记
            byte[] syss = new byte[14];
            byteBuffer.put(syss);
            for (RadarDeviceCMD deviceWorkFlowCMD : deviceWorkFlowCMDs) {
                byteBuffer.putShort(SocketConfig.header); // 小包头
                byte pulse = Byte.parseByte(deviceWorkFlowCMD.getPulse());// 内外秒脉冲选择
                byteBuffer.put(pulse);
                byteBuffer.put(backups);
                // 分机控制字
                byte[] con = extensionControl(deviceWorkFlowCMD.getExtensionControlCharacter()); // 分机控制字
                byteBuffer.put(con[0]);
                byteBuffer.put(con[1]);
                byteBuffer.put(backups);
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getThreshold()));// 检测门限调节
                byteBuffer.putShort((Short.parseShort(deviceWorkFlowCMD.getOverallpulse())));// 需要上传的全脉冲数
                byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getMinamplitude()));// 最小幅度
                byte c = (byte) (Integer.parseInt(deviceWorkFlowCMD.getMaxamplitude()) & 0xff);// 最大幅度
                byteBuffer.put(c);
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMinPulsewidth())); // 最小脉宽
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getMaxPulsewidth()));// 最大脉宽
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMaximumFrequency()));// 筛选最大频率
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getFilterMinimumFrequency()));// 筛选最小频率
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMaximumFrequency()));// 屏蔽最大频率
                byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getShieldingMinimumFrequency()));// 屏蔽最小频率
                // 默认值更新标记
                if (deviceWorkFlowCMD.getDefalutUpdate().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (deviceWorkFlowCMD.getDefalutUpdate().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                byteBuffer.putShort(shorts);
                byteBuffer.put(backups);
                byteBuffer.putShort(shorts);
                // 包尾
                getPackageTheTail(byteBuffer);
            }

            // 512字节 多余补0
            int b = 512;
            int c = b - deviceWorkFlowCMDs.size() * 32;
            byte[] bytes = new byte[c];
            byteBuffer.put(bytes);
//			byte[] sys = new byte[48];
//			byteBuffer.put(sys);
            byte[] enm = new byte[512];
            byteBuffer.put(enm);
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和// 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer); // 帧尾
            sendMessage(host, byteBuffer);
            SysLogEntity sysLogEntity = new SysLogEntity();
            for (RadarDeviceCMD deviceWorkFlowCMD : deviceWorkFlowCMDs) {
                String type = JsonUtils.toJson(deviceWorkFlowCMD);
                sysLogEntity.setActionDescription(type);
                sysLogEntity.setUserAction("雷达分机指令");
                sysLogEntity.setCreateTime(new Date());
                sysLogEntity.setHost(host);
            }
            sysLogService.saveLog(sysLogEntity);
        }
    }

    /**
     * 2设备工作流程控制指令(雷达系统指令)(单发)
     *
     * @param host
     * @param updateAll
     * @param serialNumber
     * @throws IOException
     */
    public void sendSystemInstructionsCMD(ArrayList<SystemControlCMD> systemControlCMDs, String host, String updateAll,
                                          int serialNumber) throws IOException {
        // String message = null;
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendSystemInstructionsCMD(systemControlCMDs, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1094);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 大包头
            sendSystemControlCmdFormat(byteBuffer, 1094, shorts, shorts, backups, backups, (short) 12295, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
//			byteBuffer.putInt(1); // 信息长度
//			byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getTaskFlowNo())); // 任务流水号
//			byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getCmd())); // 指令有效标记
            byte[] byteSys = new byte[14];
            byteBuffer.put(byteSys);
            for (SystemControlCMD systemControlCMD : systemControlCMDs) {
                // 雷达系统控制指令
                byteBuffer.putShort(SocketConfig.header);// 包头1ACF
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPattern()));// 工作模式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkPeriod())); // 工作周期
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getWorkPeriodNum()));// 工作周期数
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getInitialFrequency())); // 起始频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getTerminationFrequency())); // 终止频率
                byteBuffer.putShort(Short.parseShort(systemControlCMD.getSteppedFrequency()));// 频率步进
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBandWidthSelection()));// 带宽选择
                byteBuffer.putShort(shorts);// 备份
                byteBuffer.put(backups); // 备份
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection1()));// 天线1选择(页面不展示)
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAntennaSelection2()));// 天线2选择(页面不展示)
                byteBuffer.putShort(shorts);// 备份
                // 射频一衰减(最新要求改成射频2：6-18Hz射频衰减1)
                // 0:不衰减 1：衰减30db
                if (systemControlCMD.getRadioFrequencyAttenuation2().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (systemControlCMD.getRadioFrequencyAttenuation2().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                // 射频一长电缆均衡衰减控制
                byteBuffer.put(backups); // 射频一长电缆均衡衰减控制
                byteBuffer.putShort(shorts); // 备份
                // 射频二衰减(改名为射频1：6-18Hz射频衰减)
                // 射频一衰减(最新要求改成射频2：6-18Hz射频衰减)
                // 0:不衰减 1：衰减30db
                if (systemControlCMD.getRadioFrequencyAttenuation1().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (systemControlCMD.getRadioFrequencyAttenuation1().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                // 射频二长电缆均衡衰减控制(最新)
                byteBuffer.put(backups);// 射频二长电缆均衡衰减控制
                byteBuffer.putShort(shorts);
                // (最新为中频2衰减0.5dB)
                byte mid2 = (byte) ((byte) Double.parseDouble(systemControlCMD.getMidCut2()));
                byteBuffer.put(mid2); // 中频衰减1
                byteBuffer.putShort(shorts); // 备份
                byteBuffer.put(backups);
                // 中频1衰减(最新)
                byte mid1 = (byte) ((byte) Double.parseDouble(systemControlCMD.getMidCut1()));
                byteBuffer.put(mid1);
                // 衰减码控制方式
                byteBuffer.put(Byte.parseByte(systemControlCMD.getAttenuationCodeControlMode()));
                // 自检模式选择
                byteBuffer.put(Byte.parseByte(systemControlCMD.getRadarSelfChoose()));
                // 备份
                byteBuffer.put(backups);
                // 自检源衰减
                byteBuffer.put(Byte.parseByte(systemControlCMD.getSelfCheckingSourceAttenuation()));
                // 脉内引导批次开关
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumberSwitch()));
                // 脉内引导批次号
//				byteBuffer.put(backups);
                byteBuffer.put(Byte.parseByte(systemControlCMD.getBatchNumber()));
                // 故障检测门限
                byteBuffer.put(Byte.parseByte(systemControlCMD.getFaultDetectionThreshold()));
                // 定时时间码
                String time = systemControlCMD.getTimingTimeCode();
                // 转换月
                String monthString = time.substring(0, 2);
                // 转换日
                String dayString = time.substring(2, 4);
                // 转换时
                String hourString = time.substring(4, 6);
                // 转换分
                String minString = time.substring(6, 8);
                // 转换秒
                String secondString = time.substring(8, 10);
                // 转2进制月
                String monthBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(monthString));
                int monthLeftNum = 4 - monthBinaryString.length();
                for (int i = 0; i < monthLeftNum; i++) {
                    monthBinaryString = "0" + monthBinaryString;
                }
                // 转2进制日期
                String dayBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(dayString));
                int dayLeftNum = 5 - dayBinaryString.length();
                for (int i = 0; i < dayLeftNum; i++) {
                    dayBinaryString = "0" + dayBinaryString;
                }
                // 转2进制小时
                String hourBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(hourString));
                int hourLeftNum = 5 - hourBinaryString.length();
                for (int i = 0; i < hourLeftNum; i++) {
                    hourBinaryString = "0" + hourBinaryString;
                }
                // 转2进制分钟
                String minBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(minString));
                int minLeftNum = 6 - minBinaryString.length();
                for (int i = 0; i < minLeftNum; i++) {
                    minBinaryString = "0" + minBinaryString;
                }
                // 转2进制秒
                String secondBinaryString = Integer.toBinaryString(Integer.parseUnsignedInt(secondString));
                int secondLeftNum = 11 - secondBinaryString.length();
                for (int i = 0; i < secondLeftNum; i++) {
                    secondBinaryString = "0" + secondBinaryString;
                }
                String binaryString = monthBinaryString + dayBinaryString + hourBinaryString + minBinaryString
                        + secondBinaryString;
                int timer = Integer.parseUnsignedInt(binaryString, 2);
                byteBuffer.putInt(timer);
                // 单次执行指令集所需时间
                byteBuffer.putShort(shorts);
                // byteBuffer.putShort(Short.parseShort(systemControlCMD.getTimeRequired()));
                // 小包尾
                getPackageTheTail(byteBuffer); // FC1D
            }
            // 512字节 多余补0
            int c = 512;
            int cc = c - systemControlCMDs.size() * 48;
            byte[] byte1 = new byte[cc];
            byteBuffer.put(byte1);
            byte[] enm = new byte[512];
            byteBuffer.put(enm);
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer); // 帧尾
            sendMessage(host, byteBuffer);
            SysLogEntity sysLogEntity = new SysLogEntity();
            for (SystemControlCMD systemControlCMD : systemControlCMDs) {
                String systemControl = JsonUtils.toJson(systemControlCMD);
                sysLogEntity.setActionDescription(systemControl);
                sysLogEntity.setCreateTime(new Date());
                sysLogEntity.setHost(host);
                sysLogEntity.setUserAction("雷达系统指令");
            }
            sysLogService.saveLog(sysLogEntity);
        }
    }

    /**
     * 3 敌我系统控制(单发)
     *
     * @param host
     * @param updateAll
     * @param serialNumber
     * @throws IOException
     */
    public void sendEnemyAndUsCMD(ArrayList<EnemyAndUsCMD> enemyAndUsCMDs, String host, String updateAll,
                                  int serialNumber) throws IOException {
        if (updateAll.equals("1")) {
            List<AllHost> list = hostRepository.findAll();
            int serialNumbers = addSerialNum();
            for (AllHost allHost : list) {
                sendEnemyAndUsCMD(enemyAndUsCMDs, allHost.getHost(), "0", serialNumbers);
            }
        } else {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1094);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            // 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
            int serialNumber1;
            if (serialNumber == 0) {
                serialNumber1 = addSerialNum();
            } else {
                serialNumber1 = serialNumber;
            }
            // 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
            sendSystemControlCmdFormat(byteBuffer, 1094, shorts, shorts, backups, backups, (short) 12295, 0,
                    serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
            // 报文内容
//			byteBuffer.putInt(1); // 信息长度
//			byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getTaskFlowNo())); // 任务流水号
//			byteBuffer.putShort(Short.parseShort(deviceWorkFlowCMD.getCmd())); // 指令有效标记
            byte[] enemy = new byte[14];
            byteBuffer.put(enemy);
            byte[] sys = new byte[512];
            byteBuffer.put(sys);
            for (EnemyAndUsCMD enemyAndUsCMD : enemyAndUsCMDs) {
                byteBuffer.putShort(SocketConfig.header); // 包头 1ACF
                byteBuffer.put(backups); // 信息包序号
                byteBuffer.put(Byte.parseByte(enemyAndUsCMD.getWorkPattern1())); // 工作模式（0：自检 1：工作）
                // 带宽选择解析
                String bandwidthChoose = enemyAndUsCMD.getBandwidthChoose();
                byte[] temp1 = bandwidthChoose(bandwidthChoose);
                byteBuffer.put(temp1[0]);

                // byteBuffer.put(Byte.parseByte(deviceWorkFlowCMD.getPulseChoice())); // 秒脉冲选择
                if (enemyAndUsCMD.getPulseChoice().equals("0")) {
                    byteBuffer.put((byte) 0);
                } else if (enemyAndUsCMD.getPulseChoice().equals("1")) {
                    byteBuffer.put((byte) 1);
                }
                byteBuffer.put(Byte.parseByte(enemyAndUsCMD.getSelfChoose()));// //自检模式选择
                byteBuffer.put(Byte.parseByte(enemyAndUsCMD.getPointSelection())); // 自检频点选择
                String time = enemyAndUsCMD.getTimeCode(); // 解析时间（同步时间）
                if (time.isEmpty()) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:SSS:dd:MM:yyyy");
                    time = simpleDateFormat.format(new Date());
                }
                byte hour = Byte.parseByte(time.substring(0, 2));
                byteBuffer.put(hour);
                byte minute = Byte.parseByte(time.substring(3, 5));
                byteBuffer.put(minute);
                // 秒>毫秒>int>16进制
                byteBuffer.putShort(Short.parseShort(time.substring(6, 9)));
                byte day = Byte.parseByte(time.substring(10, 12));
                byteBuffer.put(day);
                byte month = Byte.parseByte(time.substring(13, 15));
                byteBuffer.put(month);
                short year = Short.parseShort(time.substring(16));
                byteBuffer.putShort(year);
                // 分机控制
                String hierarchicalControl = enemyAndUsCMD.getHierarchicalControl();
                byte[] temp = hierarchicalControl(hierarchicalControl);
                byteBuffer.put(temp[0]);
                byteBuffer.put(temp[1]);
                byteBuffer.put(temp[2]);
                byteBuffer.put(temp[3]);
//				byteBuffer.putInt(BinaryToDecimal((int) Long.parseLong(hierarchicalControl(hierarchicalControl))));
                byteBuffer.putInt(shorts); // 740全脉冲个数 备份
                byteBuffer.putInt(shorts); // 837.5 PDW个数
                byteBuffer.putInt(Integer.parseInt(enemyAndUsCMD.getPdw1030())); // 1030 PDW个数
                byteBuffer.putInt(shorts); // 1059PDW个数备份
                byteBuffer.putInt(Integer.parseInt(enemyAndUsCMD.getPdw1090())); // 1090 PDW个数
                byteBuffer.putInt(Integer.parseInt(enemyAndUsCMD.getPdw1464())); // 1464PDW个数
                byteBuffer.putInt(Integer.parseInt(enemyAndUsCMD.getPdw1532())); // 1532PDW个数
                byteBuffer.put(backups); // 740中频个数
                byteBuffer.put(backups); // 1030中频个数
                byteBuffer.put(Byte.parseByte(enemyAndUsCMD.getMid1090())); // 1090中频个数
                byteBuffer.put(backups); // 中频采集时间 未知
                // byteBuffer.putLong(Long.parseLong(deviceWorkFlowCMD.getFaultDetection())); //
                // 故障检测门限（默认0x1111111111111111111111111)
                short a = 4369;
                for (int j = 0; j < 4; j++) {
                    byteBuffer.putShort(a);
                }
                byteBuffer.put(backups);// 网络包计数
                byteBuffer.put(backups);// 备份
                // 包尾
                getPackageTheTail(byteBuffer);
            }

            // 512字节 多余补0
            int c = 512;
            int cc = c - enemyAndUsCMDs.size() * 64;
            byte[] byte1 = new byte[c - cc];
            byteBuffer.put(byte1);
            byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和 (暂时预留)
            getBigPackageTheTail(byteBuffer); // 帧尾
            // 发送信息
            sendMessage(host, byteBuffer);
            SysLogEntity sysLogEntity = new SysLogEntity();
            for (EnemyAndUsCMD enemyAndUsCMD : enemyAndUsCMDs) {
                String enemyAndUs = JsonUtils.toJson(enemyAndUsCMD);
                sysLogEntity.setActionDescription(enemyAndUs);
                sysLogEntity.setCreateTime(new Date());
                sysLogEntity.setHost(host);
                sysLogEntity.setUserAction("敌我侦察指令");
            }
            sysLogService.saveLog(sysLogEntity);
        }
    }

    /**
     * 统一发送信息
     *
     * @throws IOException
     */
    private static void sendMessage(String host, ByteBuffer byteBuffer) throws IOException {
        Socket socket = (Socket) TCPThread.map.get(host);
        SocketChannel socketChannel = socket.getChannel();
        socketChannel.write(ByteBuffer.wrap(byteBuffer.array()));
        byteBuffer.clear();
    }

    // 分机控制字(解析String为bit)
    private byte[] extensionControl(String eccs) {
//		/**
//		 * 因为二进制的bit0位置是最右，而接收到web端的值bit0的位置是最左，所以先将数据反转
//		 */
//
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("000");
        // 11-12合路选择
        String mergeToChoose = eccs.substring(7, 8);
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
        // 9-10校正表接
        String revise = eccs.substring(6, 7);
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
        // 8校正
        stringBuilder.append(eccs, 5, 6);
        // 6-7：分选预处理模式
        String preProcessModel = eccs.substring(4, 5);
        switch (preProcessModel) {
            case "0":
                stringBuilder.append("00");
                break;
            case "1":
                stringBuilder.append("01");
                break;
            default:
                throw new SystemException(SystemStatusCodeEnum.ExtensionControlCharacter_ERROR);
        }
        // 3-5传输原始全脉冲
        String allPulse = eccs.substring(3, 4);
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
        // 2合批开关
        stringBuilder.append(eccs, 2, 3);
        // 1.工作状态
        stringBuilder.append(eccs, 1, 2);
        // 0看门狗
        stringBuilder.append(eccs, 0, 1);
        String result = stringBuilder.toString();
        byte[] bytes = new byte[2];
        bytes[1] = (byte) BinaryToDecimal(Integer.parseInt(result.substring(0, 8)));
        bytes[0] = (byte) BinaryToDecimal(Integer.parseInt(result.substring(8, 16)));
        return bytes;
    }

    // 封装包尾信息
    private void getPackageTheTail(ByteBuffer byteBuffer) {
        byte[] bytes = SocketConfig.hexToByte(SocketConfig.end);
        byteBuffer.put(bytes);
    }

    // 封装大包尾信息
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
    private byte[] bandwidthChoose(String bandwidthChooses) {
        StringBuilder stringBuilder = new StringBuilder();

        // bit7
        stringBuilder.append(bandwidthChooses, 3, 4);
        // bit6
        stringBuilder.append(bandwidthChooses, 2, 3);

        stringBuilder.append("00");

        String bit3 = bandwidthChooses.substring(1, 2);
        switch (bit3) {
            case "3":
                stringBuilder.append("11");
                break;
            case "1":
                stringBuilder.append("01");
                break;
        }
        String bit1 = bandwidthChooses.substring(0, 1);
        switch (bit1) {
            case "3":
                stringBuilder.append("11");
                break;
            case "1":
                stringBuilder.append("01");
                break;
        }
        String result = stringBuilder.toString();
        byte[] bytes = new byte[1];
        bytes[0] = (byte) BinaryToDecimal(Integer.parseInt(result.substring(0, 8)));
        return bytes;
    }

    // 敌我控制指令的分机控制
    private static byte[] hierarchicalControl(String hierarchicalControl) {
        // 新实现
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("00000000000");

        // 17-20 1532上传选
        String bit17 = hierarchicalControl.substring(5, 6);
        switch (bit17) {
            case "0":
                stringBuilder.append("0000");
                break;
            case "1":
                stringBuilder.append("0001");
                break;
            case "2":
                stringBuilder.append("0010");
                break;
            case "3":
                stringBuilder.append("0011");
                break;
        }
        // 13-16 1532上传选
        String bit13 = hierarchicalControl.substring(4, 5);
        switch (bit13) {
            case "0":
                stringBuilder.append("0000");
                break;
            case "1":
                stringBuilder.append("0001");
                break;
            case "2":
                stringBuilder.append("0010");
                break;
            case "3":
                stringBuilder.append("0011");
                break;
        }
        // 9-12 1090上传选
        String bit9 = hierarchicalControl.substring(3, 4);
        switch (bit9) {
            case "0":
                stringBuilder.append("0000");
                break;
            case "1":
                stringBuilder.append("0001");
                break;
            case "2":
                stringBuilder.append("0010");
                break;
            case "3":
                stringBuilder.append("0011");
                break;
        }
        // 5-8 1030上传选
        String bit5 = hierarchicalControl.substring(2, 3);
        switch (bit5) {
            case "0":
                stringBuilder.append("0000");
                break;
            case "1":
                stringBuilder.append("0001");
                break;
            case "2":
                stringBuilder.append("0010");
                break;
            case "3":
                stringBuilder.append("0011");
                break;
        }
        // 1-4 740上传选
        String bit1 = hierarchicalControl.substring(1, 2);
        switch (bit1) {
            case "0":
                stringBuilder.append("0000");
                break;
            case "1":
                stringBuilder.append("0001");
                break;
            case "2":
                stringBuilder.append("0010");
                break;
            case "3":
                stringBuilder.append("0011");
                break;
        }
        stringBuilder.append(hierarchicalControl, 0, 1);
        String result = stringBuilder.toString();
        byte[] bytes = new byte[4];
        bytes[3] = (byte) BinaryToDecimal(Integer.parseInt(result.substring(0, 8)));
        bytes[2] = (byte) BinaryToDecimal(Integer.parseInt(result.substring(8, 16)));
        bytes[1] = (byte) BinaryToDecimal(Integer.parseInt(result.substring(16, 24)));
        bytes[0] = (byte) BinaryToDecimal(Integer.parseInt(result.substring(24, 32)));
        return bytes;
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

    // 网络ip地址String类型放入4byte
    private static String networkIP1(String networkIP1) {
        int[] ip1 = new int[4];
        // 先找到IP地址字符串中.的位置
        int position1 = networkIP1.indexOf(".");
        int position2 = networkIP1.indexOf(".", position1 + 1);
        int position3 = networkIP1.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip1[0] = Integer.parseInt(networkIP1.substring(0, position1));
        ip1[1] = Integer.parseInt(networkIP1.substring(position1 + 1, position2));
        ip1[2] = Integer.parseInt(networkIP1.substring(position2 + 1, position3));
        ip1[3] = Integer.parseInt(networkIP1.substring(position3 + 1));
        Integer ipArr = (ip1[0] << 24) + (ip1[1] << 16) + (ip1[2] << 8) + (ip1[3]);
        return ipArr.toString();
    }

    /**
     *
     * @param timeNow       报文内容 校对时间
     * @param time          定时发送时间
     * @param timingPattern 报文内容
     * @param host          发送ip
     * @param updateAll     群发
     * @param serialNumber
     */
//	public List<TimingTasks> addTimeSendTask(String timeNow, String time, String sendTime, String timingPattern,
//											 String host, String updateAll, int serialNumber) throws SchedulerException {
//		List<TimingTasks> tks = new ArrayList<>();
//		if (updateAll.equals("1")) {
//			List<AllHost> list = hostRepository.findAll();
//			int serialNumbers = addSerialNum();
//			for (AllHost allHost : list) {
//				addTimeSendTask(timeNow, time, sendTime, timingPattern, allHost.getHost(), "0", serialNumbers);
//			}
//		} else {
//			ByteBuffer byteBuffer = ByteBuffer.allocate(77);
//			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//			// 如果等于0 代表该数据并非是群发,因为同批数据序号无需自增，代表群发的消息序号共享同一个序号
//			int serialNumber1;
//			if (serialNumber == 0) {
//				serialNumber1 = addSerialNum();
//			} else {
//				serialNumber1 = serialNumber;
//			}
//			// 头部固定信息 凡是为0的数据 都只是暂定数据 待后期修改
//			sendSystemControlCmdFormat(byteBuffer, 77, shorts, shorts, backups, backups, (short) 12291, 0,
//					serialNumber1, 0, serialNumber1, 0, shorts, 0, shorts);
//			// 报文内容
//			byteBuffer.putInt(1); // 信息长度
//			byteBuffer.putLong(Long.parseLong(timeNow));
//			// long aaa = System.currentTimeMillis();
//			byteBuffer.put(Byte.parseByte(timingPattern));
//			byteBuffer.putLong(Long.parseLong(time));
//			byteBuffer.putInt(getByteCount(byteBuffer)); // 校验和
//			getBigPackageTheTail(byteBuffer);
//
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//			Date sendingDateStr = null;
//			String cron = "";
//			try {
//				sendingDateStr = sdf.parse(sendTime);
//
//				cron = CronDateUtils.getCron(sendingDateStr);
//			} catch (ParseException e) {
//				System.err.println("定时格式不对");
//			}
//			String params = TopNTool.getString(byteBuffer);
//
//			TimingTasks tasks = new TimingTasks();
//			tasks.setJobGroup("bwjs");
//			tasks.setJobName(tasks.getId());
//			tasks.setParams(params);
//			tasks.setState(1);
//			tasks.setHost(host);
//			tasks.setCron(cron);
//			TimingTasks ua = timingTaskRepository.save(tasks);
////	            System.out.println(refresh(ua));
//			tks.add(ua);
//		}
//		return tks;
//	}

}
