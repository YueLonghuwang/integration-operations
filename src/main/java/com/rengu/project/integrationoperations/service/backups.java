package com.rengu.project.integrationoperations.service;

/**
 * author : yaojiahao
 * Date: 2019/7/19 9:23
 **/

import com.rengu.project.integrationoperations.entity.DeviceWorkFlowCMD;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.thread.TCPThread;
import com.rengu.project.integrationoperations.util.SocketConfig;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * 备份用的 实际用不到
 */
/*public class backups {

    //  分机控制指令
    public String sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, String host) throws SystemException {
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
            int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);
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
    //  发送系统控制指令
    public String sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, String host) {
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
            StringBuilder stringBuilder = new StringBuilder();
            String attenuationRF1 = stringBuilder.reverse().append(deviceWorkFlowCMD.getAttenuationRF1()).toString();
            byte bytes = (byte) BinaryToDecimal(Integer.parseInt(attenuationRF1));
            byte b = 0;
            byteBuffer.put(b);

            //  射频一长电缆均衡衰减控制
//            StringBuilder stringBuilders = new StringBuilder();
            //  反转数组的原因是因为二级制从第0位开始是从右边开始的，而传过来的值第0位在最左边，所以需要反转
//            String balancedAttenuationRF1 = stringBuilders.reverse().append(deviceWorkFlowCMD.getBalancedAttenuationRF1()).toString();
//            byte bytesAttenuationRF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));

            //(最新)
            byte bytesAttenuationRF1 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getBalancedAttenuationRF1()));
            byteBuffer.put(bytesAttenuationRF1);
            byteBuffer.put(b);

            byteBuffer.putShort(shorts);
            //  射频二控制衰减
//            StringBuilder stringBuilder2 = new StringBuilder();
//            String attenuationRF2 = stringBuilder2.reverse().append(deviceWorkFlowCMD.getAttenuationRF2()).toString();
//            byte byteAttenuationRF2 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(attenuationRF2));

            //(最新)
            byte byteAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getBalancedAttenuationRF2()));
            byteBuffer.put(byteAttenuationRF2);
            byteBuffer.put(b);

            //射频二长电缆均衡衰减控制(最新)
            StringBuilder stringBuilderAttenuationRF2 = new StringBuilder();
            String balancedAttenuationRF2 = stringBuilderAttenuationRF2.reverse().append(deviceWorkFlowCMD.getBalancedAttenuationRF2()).toString();
            byte bytesAttenuationRF2 = (byte) BinaryToDecimal(Integer.parseInt(balancedAttenuationRF2));
            byteBuffer.put(bytesAttenuationRF2);
            byteBuffer.put(b);

            byteBuffer.putShort(shorts);
            //中频一衰减(最新)
            byte bytesAttenuationMF1 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getAttenuationMF1()));
            byteBuffer.put(bytesAttenuationMF1);
            byteBuffer.put(b);

            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            //中频二衰减(最新)
            byte bytesAttenuationMF2 = (byte) BinaryToDecimal(Integer.parseInt(deviceWorkFlowCMD.getAttenuationMF2()));
            byteBuffer.put(bytesAttenuationMF2);
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

            String time = deviceWorkFlowCMD.getTimingCode();
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
            }
            int d = 0;
            byteBuffer.putInt(d);

            //  单次执行指令集所需时间
            byteBuffer.putShort(shorts);
            getPackageTheTail(byteBuffer);
            byteBuffer.putInt(0); // 校验和 (暂时预留)
            int a = getByteCount(byteBuffer);
            byteBuffer.putInt(a);
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

    //  群发系统控制指令
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
    }
}*/
