package com.rengu.project.integrationoperations.util;

import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 *
 * @Author: yaojiahao
 * @Date: 2019/4/11 10:53
 */


@Slf4j
@Component
public class JavaClientUtil {
    private static final int port = 8090;
    private static final String host = "localhost";
    private final WebReceiveToCService webReceiveToCService;
    // 通道管理器(Selector)
    private static Selector selector;
    private String msg ;
    //    private final WebSocketUtil webSocketUtil;
    @Autowired
    public JavaClientUtil(WebReceiveToCService receiveInformationService) {
        this.webReceiveToCService = receiveInformationService;
//        this.webSocketUtil = webSocketUtil;
    }

    //  接收消息 使用websocket推送至前端
    public String[] receiveMessage() {
        List<String> list = new ArrayList<>();
        try {
            //创建一个ServerSocket，这里可以指定连接请求的队列长度
            //new ServerSocket(port,3);意味着当队列中有3个连接请求是，如果Client再请求连接，就会被Server拒绝
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                //从请求队列中取出一个连接
                Socket client = serverSocket.accept();
                list.add(client.getInetAddress().getHostAddress());
                // 处理这次连接
                new HandlerThread(client);
            }
        } catch (Exception e) {
            System.out.println("服务器异常: " + e.getMessage());
        }
        for (String a : list) {
            System.out.println(a);
        }
        return null;
    }

    //  发送指令
    public void sendMessage(String message) {
        System.out.println("Client Start...");
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //  发送
            PrintStream out = new PrintStream(socket.getOutputStream());
            ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = null;
            if (byteBuffer.hasArray()) {
                bytes = byteBuffer.array();
            }
            out.print(Arrays.toString(bytes));
            out.close();
        } catch (IOException e) {
            System.out.println("客户端异常:" + e.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    socket = null;
                    System.out.println("客户端 finally 异常:" + e.getMessage());
                }
            }
        }
    }

    private class HandlerThread implements Runnable {
        private Socket socket;

        HandlerThread(Socket client) {
            socket = client;
            new Thread(this).start();
        }

        public void run() {
            String clientInputStr = null;
            try {
                // 读取客户端数据
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                clientInputStr = input.readLine();//这里要注意和客户端输出流的写方法对应,否则会抛 EOFException
                // 处理客户端数据
                System.out.println("客户端发过来的内容:" + clientInputStr);
                input.close();
            } catch (Exception e) {
                System.out.println("服务器 run 异常: " + e.getMessage());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        socket = null;
                        System.out.println("服务端 finally 异常:" + e.getMessage());
                    }
                }
            }
        }
    }



    // 二进制换16
    private static void toHexTable(int num) {
        char[] cha = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        //用数组存放获取到的16进制结果
        char[] arr = new char[8];
        int pos = arr.length;
        while (num != 0) { //右移后，会有很多0, 0000-0000 0000-0000 0000-0000 0000-1100,只取有效位即可，2进制0对应的10进制也是0
            int temp = num & 15;
            arr[--pos] = cha[temp];//直接从角标中获取值cha[temp]即为其16进制数，数组倒着放--pos，就不用反转数组cha[temp]
            num = num >>> 4;
        }
        System.out.println("pos:" + pos);
        //获取到cha[temp]后反转即可打印正确的char
        for (int i = pos; i < arr.length; i++) {
            System.out.print(arr[i] + ",");
        }
    }
}
