package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.WebSendToCService;
import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:30
 */
@RestController
@RequestMapping(path = "/deployment")
public class WebSendToCController {
    private final WebSendToCService webSendToCService;
    private final WebReceiveToCService receiveInformationService;
    @Autowired
    public WebSendToCController(WebSendToCService webSendToCService, WebReceiveToCService receiveInformationService) {
        this.webSendToCService = webSendToCService;
        this.receiveInformationService = receiveInformationService;
    }

    // 发送系统校时
    @PostMapping("/sendSystemTiming/communication")
    public ResultEntity sendSystemTiming(@NotNull String timeNow,@NotNull String timingPattern,@NonNull String time, @NonNull String host,@NonNull String updateAll) throws IOException {
        webSendToCService.sendSystemTiming(timeNow,time,timingPattern, host,updateAll);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
    }

    // 设备复位
    @PostMapping("/sendDeviceRestoration/communication")
    public ResultEntity sendDeviceRestoration(String timeNow, String executePattern, String host,@NonNull String updateAll){
        webSendToCService.sendDeviceRestoration(timeNow,executePattern,host,updateAll);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 设备自检指令
    @PostMapping("/sendDeviceCheckCMD/communication")
    public ResultEntity sendDeviceCheckCMD(DeviceCheckCMD deviceCheckCMD,String host,@NonNull String updateAll){
        webSendToCService.sendDeviceCheckCMD(deviceCheckCMD,host,updateAll);
     return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 软件版本更新
    @PostMapping("/sendSoftwareUpdateCMD/communication")
    public ResultEntity sendSoftwareUpdateCMD(String timeNow, String cmd, String softwareID, String host,@NonNull String updateAll){
        webSendToCService.sendSoftwareUpdateCMD(timeNow,cmd,softwareID,host,updateAll);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 设备网络参数更新指令
    @PostMapping("/sendDeviceNetworkCMD/communication")
    public ResultEntity sendDeviceNetworkCMD(SendDeviceNetWorkParam sendDeviceNetWorkParam,String host,@NonNull String updateAll){
        webSendToCService.sendDeviceNetworkCMD(sendDeviceNetWorkParam,host,updateAll);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 设备工作流程控制指令
    @PostMapping("/sendDeviceWorkFlowCMD/communication")
    public ResultEntity sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD,int count,String host,@NonNull String updateAll){
        webSendToCService.sendDeviceWorkFlowCMD(deviceWorkFlowCMD,count,host,updateAll);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功");
    }
    // 查询所有
    @GetMapping
    public ResultEntity findAll(){
      return new ResultEntity(SystemStatusCodeEnum.SUCCESS, receiveInformationService.findAll());
    }
}
