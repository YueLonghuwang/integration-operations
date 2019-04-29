package com.rengu.project.integrationoperations.util;

import java.util.*;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 18:58
 */
public class SocketConfig {
    public static final int port = 8009;
    public static final short header = 6863;
    public static final String end = "FC1D";

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
    public static void main(String [] args){
        Set<String> set=new HashSet<>();
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
    }
}
