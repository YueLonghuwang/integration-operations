package com.rengu.project.integrationoperations.thread;

import com.rengu.project.integrationoperations.entity.AllHost;
import com.rengu.project.integrationoperations.entity.SysErrorLogEntity;
import com.rengu.project.integrationoperations.entity.SysLogEntity;
import com.rengu.project.integrationoperations.entity.TcpInAndOut;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.repository.SysLogRepository;
import com.rengu.project.integrationoperations.service.SysErrorLogService;
import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author yaojiahao
 * @data 2019/4/29 15:30
 */

@Slf4j
@Component
public class TCPThread {
    public static final Map<String, Object> map = new ConcurrentHashMap<>();
    private static final int Integer = 0;
    // 接收报文
    private final WebReceiveToCService receiveInformationService;
    private final HostRepository hostRepository;
    private static Selector selector;
    private static ByteBuffer site1LeftBuffer = ByteBuffer.allocate(5000);
    private static ByteBuffer site2LeftBuffer = ByteBuffer.allocate(5000);
    private static ByteBuffer site3LeftBuffer = ByteBuffer.allocate(5000);
    public static BlockingQueue<TcpInAndOut> queue = new LinkedBlockingQueue<>();
    private Set<String> set = new HashSet<>();
    private final SysErrorLogService sysErrorLogService;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private SysLogRepository sysLogRepository;

    public TCPThread(WebReceiveToCService receiveInformationService, HostRepository hostRepository,
                     SysErrorLogService sysErrorLogService) {
        this.receiveInformationService = receiveInformationService;
        this.hostRepository = hostRepository;
        this.sysErrorLogService = sysErrorLogService;
    }

    @Async
    public void monitoringTCP() {
        int portTCP = 5889;
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(); // 新建channel
            // 监听端口
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(portTCP));
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isValid() & key.isReadable()) { // 监听到读事件，对读事件进行处理
                        handleRead(key);
                    }
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听到读事件，读取客户端发送过来的消息
     */
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(3000);
        String host = channel.socket().getInetAddress().getHostAddress(); // 获取设备连接的ip
        SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
        try {
            channel.read(buffer);
        } catch (IOException e) {
            Map<Object, Object> map = new HashMap<>();
            map.put("host", host);
            Map<Object, Object> map1 = new HashMap<>();
            map1.put("data", map);
            AllHost allHost = hostRepository.findByHost(host).get();
            //Map<String, Number>mapFixation= receiveInformationService.receiveFixedInformation(buffer);
            //int sourceHost=Short.toUnsignedInt((short) mapFixation.get("sourceHost").shortValue());
            //String sourceHost2=String.valueOf(sourceHost);
            switch (allHost.getNum()) {
                case 1:
                    // 设备离线
                    sysErrorLogEntity.setHost(host);
                    sysErrorLogEntity.setErrorMsg("设备离线");
                    sysErrorLogEntity.setErrorType("系统异常");
                    sysErrorLogEntity.setCreateTime(new Date());
                    sysErrorLogService.saveError(sysErrorLogEntity);
                    site1LeftBuffer.clear();
                    System.out.println(host + "buff清除");
                    break;
                case 2:
                    sysErrorLogEntity.setHost(host);
                    sysErrorLogEntity.setErrorMsg("设备离线");
                    sysErrorLogEntity.setErrorType("系统异常");
                    sysErrorLogEntity.setCreateTime(new Date());
                    sysErrorLogService.saveError(sysErrorLogEntity);
                    site2LeftBuffer.clear();
                    System.out.println(host + "buff清除");
                    break;
                case 3:
                    sysErrorLogEntity.setHost(host);
                    sysErrorLogEntity.setErrorMsg("设备离线");
                    sysErrorLogEntity.setErrorType("系统异常");
                    sysErrorLogEntity.setCreateTime(new Date());
                    sysErrorLogService.saveError(sysErrorLogEntity);
                    site3LeftBuffer.clear();
                    System.out.println(host + "buff清除");
                    break;
                default:
                    System.out.println("设备序号错误:" + host);
                    break;
            }
            simpMessagingTemplate.convertAndSend("/deviceUnConnect/send", map1);
            System.out.println(host + "已断开连接");
            //断开连接异常
            //receiveInformationService.saveSystemErrorLog(null, host);
            key.cancel();
            channel.socket().close();
            channel.close();
            return;
        }
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        processData(site1LeftBuffer, buffer, host);
    }

    // 解析站点
    private void processData(ByteBuffer leftBuffer, ByteBuffer buffer, String host) {
        ByteBuffer processBuffer;
        if (leftBuffer.position() > 0) {
            int newBuffLen = leftBuffer.position() + buffer.position();
            processBuffer = ByteBuffer.allocate(newBuffLen);
            processBuffer.put(leftBuffer.array(), 0, leftBuffer.position());
            processBuffer.put(buffer.array(), 0, buffer.position());
        } else {
            processBuffer = ByteBuffer.allocate(buffer.position());
            processBuffer.put(buffer.array(), 0, buffer.position());
        }
        ByteBuffer littleBuffer = processBuffer.order(ByteOrder.LITTLE_ENDIAN);
        int t_processPos = 0;
        int t_pack36HeadLen = 48;
        while (true) {
            if ((t_processPos + t_pack36HeadLen) <= littleBuffer.position()) {
                int t_packLen = littleBuffer.getInt(t_processPos + 4);
                if (t_packLen <= littleBuffer.position() - t_processPos) {
                    ByteBuffer t_packet = ByteBuffer.allocate(t_packLen);
                    t_packet.put(littleBuffer.array(), t_processPos, t_packLen);
                    t_processPos += t_packLen;
                    // 加入t_packet，host封装成一个处理单元，加入队列。
                    TcpInAndOut tcpInAndOut = new TcpInAndOut(host, t_packet);
                    queue.offer(tcpInAndOut);
                } else {
                    leftBuffer.clear();
                    if (processBuffer.position() - t_processPos > 0) {
                        leftBuffer.put(processBuffer.array(), t_processPos, processBuffer.position() - t_processPos);
                    }
                    break;
                }
            } else {
                leftBuffer.clear();
                if (processBuffer.position() - t_processPos > 0) {
                    leftBuffer.put(processBuffer.array(), t_processPos, processBuffer.position() - t_processPos);
                }
                break;
            }
        }
    }

    /**
     * 处理客户端连接成功事件
     */
    private void handleAccept(SelectionKey key) throws IOException {
        // 获取
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);
        map.put(socketChannel.socket().getInetAddress().getHostAddress(), socketChannel.socket());
        receiveInformationService.allHost(socketChannel.socket().getInetAddress().getHostAddress());
        set.add(socketChannel.socket().getInetAddress().getHostAddress());
        SocketAddress sendSucess = socketChannel.getRemoteAddress();
        String str = String.valueOf(sendSucess);
        int a = str.indexOf("/") + 1;// 获取ip地址开头
        String str1 = str.substring(a);
        int c = str1.length();
        int b = str1.indexOf(":") + 1;
        int d = (str1.substring(b)).length();
        int ipl = c - d;
        String ip = str.substring(a, ipl);
        Map<Object, Object> map = new HashMap<>();
        map.put("host", ip);
        Map<Object, Object> map1 = new HashMap<>();
        map1.put("data", map);
        map1.put("message", "当前设备连接已成功");
        map1.put("device", set.size());
        // 连接成功后，给前端一个话题，指示灯变成绿色
        simpMessagingTemplate.convertAndSend("/deviceConnectSuccess/send", map1);
        // 记录连接日志
        saveSystemLog(ip);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private SysLogEntity saveSystemLog(String host) {
        SysLogEntity sysLogEntity = new SysLogEntity();
        sysLogEntity.setUserAction(host + "已连接到服务器");
        return sysLogRepository.save(sysLogEntity);
    }

}
