package com.rengu.project.integrationoperations.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/11 10:53
 */
@Component
public class JavaClientUtil {
    public static final int port = 8090;
    public static final String host = "localhost";
    @Autowired
    private final WebSocketUtil webSocketUtil;

    public JavaClientUtil(WebSocketUtil webSocketUtil) {
        this.webSocketUtil = webSocketUtil;
    }

    //  接收消息 使用websocket推送至前端
    public String[] receiveMessage() {
        try {
            //创建一个ServerSocket，这里可以指定连接请求的队列长度
            //new ServerSocket(port,3);意味着当队列中有3个连接请求是，如果Client再请求连接，就会被Server拒绝
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                //从请求队列中取出一个连接
                Socket client = serverSocket.accept();
                // 处理这次连接
                new HandlerThread(client);
            }
        } catch (Exception e) {
            System.out.println("服务器异常: " + e.getMessage());
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
            webSocketUtil.sendMessage(input.toString());
            //  发送
            PrintStream out = new PrintStream(socket.getOutputStream());
            ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = null;
            if (byteBuffer.hasArray()) {
                bytes = byteBuffer.array();
            }
            out.print(bytes);
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

        public HandlerThread(Socket client) {
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
                webSocketUtil.sendMessage(clientInputStr);
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

}
