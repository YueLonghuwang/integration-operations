package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.ExtensionControlCMD;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.util.JavaClientUtil;
import com.rengu.project.integrationoperations.util.SocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final UserService userService;
    private final JavaClientUtil javaClientUtil;
    //  public static final int port = 8090;
//              包头
//  public static final short header = 6863;
//              包尾
//  public static final String host = "localhost";
    Socket socket = null;
    private byte backups = 0;

    @Autowired
    public DeploymentService(UserService userService, JavaClientUtil javaClientUtil) {
        this.userService = userService;
        this.javaClientUtil = javaClientUtil;
    }

    //  系统校时 包头 1 1010 1100 1111 包尾 1111 1100 0001 1101
    //  发送系统校时指令
//    public int getUnsignedByte(short data) {      //将data字节型数据转换为0~65535 (0xFFFF 即 WORD)。
//        return data & 0x0FFFF;
//    }

    public void sendSystemTimings(String time, String host) throws IOException {
//        getUnsignedByte((short) 0xFC1D);
        if (time.isEmpty()) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:SSS:dd:MM:yyyy");
            time = simpleDateFormat.format(new Date());
        }
        Socket socket = new Socket("198.168.2.5", 6001);
        //  包头
//        Integer.toBinaryString(header);
//        Integer.toBinaryString(end);
        socket.getOutputStream();
        ByteBuffer byteBuffer = ByteBuffer.allocate(2);
//        byteBuffer.putInt(header);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(byteBuffer.array());
        outputStream.flush();
        //  ByteBuffer byteBuffer=ByteBuffer.wrap();
        //  包尾
        Integer.toBinaryString(64541);
        socket.getOutputStream();

    }
    //  系统控制指令


    // 发送信息至后端
    public String sendMessage(String equipmentId, String message) {
//        Socket socket = new Socket();
        return "SUCCESS";
    }

    // 接收信息至前端
    public String receiveMessage() {
        javaClientUtil.receiveMessage();
        return "SUCCESS";
    }

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
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    socket = null;
                    throw new SystemException(SystemStatusCodeEnum.SOCKET_FINALLY_ERROR);
                }
            }
        }
    }

    //  分机控制指令
    public void sendExtensionControlCMD(ExtensionControlCMD extensionControlCMD, String host) {
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

            /*
              todo
              位移的运算，拿到值后进行位移的运算
              拿到二进制的值，用什么装起来，
             */
            //  包尾
            getPackageTheTail(byteBuffer);
            byteBuffer.put(SocketConfig.hexToByte(SocketConfig.end));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  封装包尾信息
    private void getPackageTheTail(ByteBuffer byteBuffer) {
        byte[] bytes = SocketConfig.hexToByte(SocketConfig.end);
        for (byte aByte : bytes) {
            byteBuffer.put(aByte);
        }
    }

    //  分机控制字
    private void extensionControl(String eccs, ByteBuffer byteBuffer) {
        //  截取分机控制字中的数据》再将二进制文件转换成十进制
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
}
