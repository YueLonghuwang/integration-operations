package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.ExtensionControlCMD;
import com.rengu.project.integrationoperations.entity.LabelDataFormat;
import com.rengu.project.integrationoperations.entity.SystemControlCMD;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.util.SocketConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:32
 */
@Service
public class DeploymentService {
    private Socket socket = null;
    private byte backups = 0;

    //  发送时间
    public void sendSystemTiming(String time, String host) {
        try {
            socket = new Socket(host, SocketConfig.port);
            ByteBuffer byteBuffer = ByteBuffer.allocate(12);
            //  包头
            byteBuffer.putShort(SocketConfig.header);
            //  解时间
            if (time.isEmpty()) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss:dd:MM:yyyy");
                time = simpleDateFormat.format(new Date());
            }
            byte hour = Byte.parseByte(time.substring(0, 2));
            byteBuffer.put(hour);
            byte minute = Byte.parseByte(time.substring(3, 5));
            byteBuffer.put(minute);
            //  秒>毫秒>int>16进制
            String millisecond = Integer.toHexString(Integer.parseInt(time.substring(6, 8)) * 1000);
            byte[] byteMS = SocketConfig.hexToByte(millisecond);
            for (byte byteM : byteMS) {
                byteBuffer.put(byteM);
            }
            if (byteMS.length == 0) {
                short s = 0;
                byteBuffer.putShort(s);
            }
            byte day = Byte.parseByte(time.substring(9, 11));
            byteBuffer.put(day);
            byte month = Byte.parseByte(time.substring(12, 14));
            byteBuffer.put(month);
            short year = Short.parseShort(time.substring(15));
            byteBuffer.putShort(year);
            //  包尾
            getPackageTheTail(byteBuffer);
            OutputStream outputStream = socket.getOutputStream();
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
            ByteBuffer byteBuffer = ByteBuffer.allocate(32);
            //  包头
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
            byteBuffer.put(Byte.parseByte(extensionControlCMD.getMaxamplitude()));
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
            short backupsShort = 0;
            byteBuffer.putShort(backupsShort);
            byteBuffer.put(backups);
            byteBuffer.putShort(backupsShort);
            //  包尾
            getPackageTheTail(byteBuffer);
            OutputStream outputStream = null;
            if (byteBuffer.hasArray()) {
                outputStream = socket.getOutputStream();
                outputStream.write(byteBuffer.array());
            }
            assert outputStream != null;
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
        bytes[0] = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(reverse.substring(0, 5)));
        bytes[1] = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(reverse.substring(5)));
        byteBuffer.put(bytes[0]);
        byteBuffer.put(bytes[1]);
    }

    //  发送系统控制指令
    public String sendSystemControlCMD(SystemControlCMD systemControlCMD, String host) {
        try {
            socket = new Socket(host, SocketConfig.port);
            ByteBuffer byteBuffer = ByteBuffer.allocate(48);
            byteBuffer.putShort(SocketConfig.header);
            byteBuffer.put(Byte.parseByte(systemControlCMD.getWorkCycle()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getWorkCycleAmount()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getBeginFrequency()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getEndFrequency()));
            byteBuffer.putShort(Short.parseShort(systemControlCMD.getSteppedFrequency()));
            byteBuffer.put(Byte.parseByte(systemControlCMD.getSteppedFrequency()));
            short shorts = 0;
            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            byteBuffer.put(Byte.parseByte(systemControlCMD.getChooseAntenna1()));
            byteBuffer.put(Byte.parseByte(systemControlCMD.getChooseAntenna2()));
            byteBuffer.putShort(shorts);
            //  射频一衰减
            StringBuilder stringBuilder = new StringBuilder();
            String attenuationRF1 = stringBuilder.reverse().append(systemControlCMD.getAttenuationRF1()).toString();
            byte bytes = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(attenuationRF1));
            byteBuffer.put(bytes);
            //  射频一长电缆均衡衰减控制
            StringBuilder stringBuilders = new StringBuilder();
            //  反转数组的原因是因为二级制从第0位开始是从右边开始的，而传过来的值第0位在最左边，所以需要反转
//            String balancedAttenuationRF1 = stringBuilders.reverse().append(systemControlCMD.getBalancedAttenuationRF1()).toString();
//            byte bytesAttenuationRF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(balancedAttenuationRF1));
            byte bytesAttenuationRF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(systemControlCMD.getBalancedAttenuationRF1()));
            byteBuffer.put(bytesAttenuationRF1);
            byteBuffer.putShort(shorts);
            //  射频二控制衰减
            StringBuilder stringBuilder2 = new StringBuilder();
//            String attenuationRF2 = stringBuilder2.reverse().append(systemControlCMD.getAttenuationRF2()).toString();
//            byte byteAttenuationRF2 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(attenuationRF2));
            byte byteAttenuationRF2 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(systemControlCMD.getBalancedAttenuationRF2()));
            byteBuffer.put(byteAttenuationRF2);
            //  射频二长电缆均衡衰减控制
            StringBuilder stringBuilderAttenuationRF2 = new StringBuilder();
            String balancedAttenuationRF2 = stringBuilderAttenuationRF2.reverse().append(systemControlCMD.getBalancedAttenuationRF2()).toString();
            byte bytesAttenuationRF2 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(balancedAttenuationRF2));
            byteBuffer.put(bytesAttenuationRF2);
            byteBuffer.putShort(shorts);
            //  中频一衰减
            byte bytesAttenuationMF1 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(systemControlCMD.getAttenuationMF1()));
            byteBuffer.put(bytesAttenuationMF1);
            byteBuffer.putShort(shorts);
            byteBuffer.put(backups);
            //  中频二衰减
            byte bytesAttenuationMF2 = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(systemControlCMD.getAttenuationMF2()));
            byteBuffer.put(bytesAttenuationMF2);
            byteBuffer.put(Byte.parseByte(systemControlCMD.getAttenuationControlWay()));
            byteBuffer.putShort(shorts);
            //  自检源衰减
            byteBuffer.put(backups);
            byteBuffer.put(Byte.parseByte(systemControlCMD.getGuidanceSwitch()));
            //  脉内引导批次开关
            byteBuffer.put(backups);
            //  故障检测门限
            byteBuffer.put(backups);
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
            bytes1[0] = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(thisTime.substring(0, 8)));
            bytes1[1] = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(thisTime.substring(8, 16)));
            bytes1[2] = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(thisTime.substring(16, 24)));
            bytes1[3] = (byte) SocketConfig.BinaryToDecimal(Integer.parseInt(thisTime.substring(24)));
            for (byte b : bytes1) {
                byteBuffer.put(b);
            }
            //  单次执行指令集所需时间
            byteBuffer.putShort(shorts);
            getPackageTheTail(byteBuffer);
            OutputStream outputStream = socket.getOutputStream();
            if (byteBuffer.hasArray()) {
                outputStream.write(byteBuffer.array());
            }
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

    // 标签包数据格式
    public void labelDataFormat(LabelDataFormat labelDataFormat, String host) {
        try {
            socket = new Socket(host, SocketConfig.port);
            // 图三与图四对接不上，猜测是少了 分机4故障状态 所以字节长度调节为546
            ByteBuffer byteBuffer = ByteBuffer.allocate(546);
            byteBuffer.putShort(SocketConfig.header);
            // 表13网络接口数据类型定义
            short s = 0;
            byteBuffer.putShort(s);
            // 系统控制信息

            //  GPS数据
            long longs = 0;
            byteBuffer.putLong(longs);
            byteBuffer.putLong(longs);
            byteBuffer.putLong(longs);
            byteBuffer.putLong(longs);
            byteBuffer.putLong(longs);
            byteBuffer.putLong(longs);
            byteBuffer.putLong(longs);
            byteBuffer.putLong(longs);
            //  数据信息

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  系统控制信息
    public void systemControlCmd(ByteBuffer byteBuffer) {
        short header = 21496;
        byteBuffer.putShort(header);
        //  信息包序号

        //  时间码
//         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss:dd:MM:yyyy");
//         String   time = simpleDateFormat.format(new Date());
//        byte hour = Byte.parseByte(time.substring(0, 2));
//        byteBuffer.put(hour);
//        byte minute = Byte.parseByte(time.substring(3, 5));
//        byteBuffer.put(minute);
        //  秒>毫秒>int>16进制
//        String millisecond = Integer.toHexString(Integer.parseInt(time.substring(6, 8)) * 200);
//        byte[] byteMS = SocketConfig.hexToByte(millisecond);
//        for (byte byteM : byteMS) {
//            byteBuffer.put(byteM);
//        }
//        if (byteMS.length == 0) {
//            short s = 0;
//            byteBuffer.putShort(s);
//        }
//        byte day = Byte.parseByte(time.substring(9, 11));
//        byteBuffer.put(day);
//        byte month = Byte.parseByte(time.substring(12, 14));
//        byteBuffer.put(month);
//        short year = Short.parseShort(time.substring(15));
//        byteBuffer.putShort(year);
        //  工作方式

    }

    //  封装包尾信息
    private void getPackageTheTail(ByteBuffer byteBuffer) {
        byte[] bytes = SocketConfig.hexToByte(SocketConfig.end);
        for (byte aByte : bytes) {
            byteBuffer.put(aByte);
        }
    }


}
