package com.rengu.project.integrationoperations.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * author : yaojiahao
 * Date: 2019/7/8 9:57
 **/

@Component
public class FixedInformation {
    // 头部固定信息
    private void sendSystemControlCmdFormat(ByteBuffer byteBuffer) {
        String frameHeader = "7E8118E7";
        byte[] bytes = SocketConfig.hexToByte(frameHeader);
        // 报文头
        byteBuffer.putInt(2122389735);
    }
}
