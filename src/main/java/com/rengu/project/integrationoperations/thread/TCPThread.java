package com.rengu.project.integrationoperations.thread;

import com.rengu.project.integrationoperations.entity.AllHost;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.util.SocketConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yaojiahao
 * @data 2019/4/29 15:30
 */

@Slf4j
@Component
public class TCPThread {
    private final HostRepository hostRepository;

    @Autowired
    public TCPThread(HostRepository hostRepository) {
        this.hostRepository = hostRepository;
    }

    // 接收Socket
    @Async
    public void TCPIronFriendOrFoeReceiver() {
        int ironFriendOrFoePort = 5886;
        log.info("监听铁塔敌我端口: " + ironFriendOrFoePort);
        try {
            ServerSocket serverSocket = new ServerSocket(ironFriendOrFoePort);
            while (true) {
                Socket client = serverSocket.accept();
                scoketIronFriendOrFoeHandler(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void TCPIronRadarReceiver() {
        Set<String> setHost = new HashSet<>();
        int ironRadarPort = 5887;
        log.info("监听铁塔雷达端口: " + ironRadarPort);
        try {
            ServerSocket serverSocket = new ServerSocket(ironRadarPort);
            while (true) {
                Socket client = serverSocket.accept();
                setHost.add(client.getInetAddress().getHostAddress());
                if (setHost.size() == 3) {
                    allHost(setHost);
                }
                scoketIronRadarHandler(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  拿到所有的host
    private void allHost(Set<String> set) {
        List<AllHost> list = hostRepository.findAll();
        //  如果数据库的ip地址有三个，并且当前ip有修改，那么修改当前IP,并且存入数据库
        Set<String> set1 = new HashSet<>(set);
        if (list.size() == 3) {
            for (AllHost allHost : list) {
                set.add(allHost.getHost());
            }
            if (set.size() > 3) {
                for (AllHost allHost : list) {
                    hostRepository.deleteById(allHost.getId());
                }
                for (String s : set1) {
                    AllHost allHost = new AllHost();
                    allHost.setHost(s);
                    hostRepository.save(allHost);
                }
            }
        } else {
            for (String host : set) {
                AllHost allHost = new AllHost();
                allHost.setHost(host);
                hostRepository.save(allHost);
            }
        }
    }

    //  接收铁塔敌我报文
    @Async
    public void scoketIronFriendOrFoeHandler(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        reciveAndConvertIronFriendOrFoe(byteArrayOutputStream.toByteArray());
    }

    //  接收铁塔雷达报文
    @Async
    public void scoketIronRadarHandler(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, byteArrayOutputStream);
        reciveAndConvertIronRadar(byteArrayOutputStream.toByteArray());
    }

    private void reciveAndConvertIronFriendOrFoe(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(600);
        byteBuffer.put(bytes);
        // 判断包
        int header = SocketConfig.BinaryToDecimal(byteBuffer.getShort());
    }

    private void reciveAndConvertIronRadar(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(500);
        byteBuffer.put(bytes);
        // 判断包
        int header = SocketConfig.BinaryToDecimal(byteBuffer.getShort());
    }
}
