package com.rengu.project.integrationoperations.util;

import com.rengu.project.integrationoperations.thread.TCPThread;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Author: XYmar
 * Date: 2019/9/12 10:36
 */
@DisallowConcurrentExecution
@Component
public class DynamicJob implements Job {
    private Logger logger = LoggerFactory.getLogger(DynamicJob.class);
    /**
     * 核心方法,Quartz Job真正的执行逻辑.
     * @param executorContext executorContext JobExecutionContext中封装有Quartz运行所需要的所有信息
     * @throws JobExecutionException execute()方法只允许抛出JobExecutionException异常
     */
    @Override
    public void execute(JobExecutionContext executorContext) throws JobExecutionException {
        //JobDetail中的JobDataMap是共用的,从getMergedJobDataMap获取的JobDataMap是全新的对象
        JobDataMap map = executorContext.getMergedJobDataMap();
        String jarPath = map.getString("jarPath");
        String parameter = map.getString("parameter");
        String vmParam = map.getString("vmParam");
        String host =map.getString("host");
        logger.info("Running Job name : {} ", map.getString("name"));
        logger.info("Running Job description : " + map.getString("JobDescription"));
        logger.info("Running Job group: {} ", map.getString("group"));
        logger.info("Running Job cron : " + map.getString("cronExpression"));
        logger.info("Running Job host : {} ", host);
        logger.info("Running Job parameter : {} ", parameter);
        logger.info("Running Job vmParam : {} ", vmParam);
        long startTime = System.currentTimeMillis();
        sendMessage(host,TopNTool.getByteBuffer(parameter));
        long endTime = System.currentTimeMillis();
        logger.info(">>>>>>>>>>>>> Running Job has been completed , cost time :  " + (endTime - startTime) + "ms\n");
    }
    //打印Job执行内容的日志
    private void logProcess(InputStream inputStream, InputStream errorStream) throws IOException {
        String inputLine;
        String errorLine;
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
        while ((inputLine = inputReader.readLine()) != null) logger.info(inputLine);
        while ((errorLine = errorReader.readLine()) != null) logger.error(errorLine);
    }

    /**
     * 统一发送信息
     */
    private static void sendMessage(String host, ByteBuffer byteBuffer) {
        Socket socket = (Socket) TCPThread.map.get(host);
        SocketChannel socketChannel = socket.getChannel();
        try {
            socketChannel.write(ByteBuffer.wrap(byteBuffer.array()));
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
