package com.rengu.project.integrationoperations.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 18:58
 */
public class SocketConfig {
    public static final int port = 8009;
    public static final short header = 6863;
    public static final String end = "1DFC";

    //  十六进制转换成byte[]
    public static byte[] hexToByte(String hex) {
        int m = 0, n = 0;
        int byteLen = hex.length() / 2; // 每两个字符描述一个字节
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = (byte) intVal;
        }
        return ret;

    }

    //  二进制转换成10进制
    public static int BinaryToDecimal(int binaryNumber) {
        int decimal = 0;
        int p = 0;
        while (true) {
            if (binaryNumber == 0) {
                break;
            } else {
                int temp = binaryNumber % 10;
                decimal += temp * Math.pow(2, p);
                binaryNumber = binaryNumber / 10;
                p++;
            }
        }
        return decimal;
    }

    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        String a = scanner.next();
//        switch (a) {
//            case "start":
//                System.out.println("开始了");
//                break;
//            case "hao":
//            case "jia":
//                System.out.println("haoJiaHao");
//                break;
//            case "stop":
//                System.out.println("暂停");
//                break;
//
//            default:
        Map map = new ConcurrentHashMap();
        for (int i=0;i<10;i++){
            System.out.println(i);
            map.put("5","66");
            map.put("9","100");
        }
        if(map.get("5")!=null){
            System.out.println("1322");
        }
        System.out.println(map.get("5")+""+map.get("9"));
  /*      String a="0110110111";

       int c= BinaryToDecimal(Integer.parseInt(a));
       System.out.println(c);
       ByteBuffer byteBuffer=ByteBuffer.allocate(20);
       byte b=20;
       byteBuffer.put(b);
       byte d=byteBuffer.get(0);
       System.out.println(d);*/
   /*     Set<String> set=new HashSet<>();
        set.add("192.168.1.101");
        set.add("192.168.1.102");
        set.add("192.168.1.103");
        Set<String> set1 = new HashSet<>(set);
        List<String> list=new ArrayList<>();
        list.add("192.168.1.101");
        list.add("192.168.1.104");
        list.add("192.168.1.103");
        set.addAll(list);
        for (String s : set) {
            System.out.println(s);
        }
        System.out.println(set1.size());
        System.out.println(set.size());
        byte[] bytes = new byte[5];
        bytes[0] = 5;
        bytes[1] = 5;
        bytes[2] = 5;
        bytes[3] = 5;
        bytes[4] = 7;
        ByteBuffer byteBuffer=ByteBuffer.allocate(5);
        byteBuffer.put(bytes);
        byte b=byteBuffer.get(1);
        System.out.println(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        StringBuilder stringBuilder=new StringBuilder();
        for (int i=bytes.length-1;i>=0;i--) {
            String tString = Integer.toBinaryString((bytes[i] & 0xFF) + 0x100).substring(1);
            stringBuilder.append(tString);
        }
        System.out.println(stringBuilder.toString().substring(38,39));
        System.out.println(stringBuilder.toString());*/
//        byte b= (byte) (254&0xff);
//        System.out.println(b);
        //  输出测试
    }

    public static String binary(byte[] bytes, int radix) {
        return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
    }

}
