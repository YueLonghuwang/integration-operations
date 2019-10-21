package com.rengu.project.integrationoperations.util;

import java.math.BigInteger;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 18:58
 */
public class SocketConfig {
	public static final int port = 8009;
	public static final short header = 6863;
	// public static final String end = "1DFC";
	public static final String end = "FC1D";

	// 十六进制转换成byte[]
	public static byte[] hexToByte(String hex) {
		int m = 0, n = 0;
		int c = 0;
		int byteLen = hex.length() / 2; // 每两个字符描述一个字节
		byte[] ret = new byte[byteLen];
		for (int i = byteLen - 1; i >= 0; i--) {
			m = i * 2 + 1;
			n = m + 1;
			int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
			ret[c] = (byte) intVal;
			c++;
		}
		return ret;

	}

	// 十进制转换成十六进制
	public static char hexToSixten(int six) {
		int i = 0;
		char[] S = new char[100];
		char sixTen = 0;
		if (six == 0) {
			System.out.println(0);
		} else {
			while (six != 0) {
				int t = six % 16;
				if (t > 0 && t < 10) {
					S[i] = (char) (t + '0');
					i++;
				} else {
					S[i] = (char) (t + 'A' - 10);
					i++;
				}
				six = six / 16;
			}
			for (int j = i - 1; j >= 0; j--) {
				sixTen = S[j];
			}
		}
		return sixTen;

	}

	// 二进制转换成10进制
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

	// 二进制转换成10进制 (Long)
	public static long BinaryToDecimals(long binaryNumber) {
		int decimal = 0;
		int p = 0;
		while (true) {
			if (binaryNumber == 0) {
				break;
			} else {
				long temp = binaryNumber % 10;
				decimal += temp * Math.pow(2, p);
				binaryNumber = binaryNumber / 10;
				p++;
			}
		}
		return decimal;
	}

	public static void main(String[] args) {
		/*
		 * Map map = new ConcurrentHashMap(); for (int i = 0; i < 10; i++) {
		 * System.out.println(i); map.put("5", "66"); map.put("9", "100"); } if
		 * (map.get("5") != null) { System.out.println("1322"); }
		 * System.out.println(map.get("5") + "" + map.get("9"));
		 */
//      StringBuilder stringBuilder = new StringBuilder();
//      stringBuilder.append("01");
//      stringBuilder.append("01");
//      String s=stringBuilder.toString();
//      System.out.println(stringBuilder.reverse());
		int c = Integer.parseInt("010100000000");
	}

	public static String binary(byte[] bytes, int radix) {
		return new BigInteger(1, bytes).toString(radix);// 这里的1代表正数
	}

}
