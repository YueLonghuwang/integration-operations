package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.DeploymentService;
import com.rengu.project.integrationoperations.service.ReceiveInformationService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DeploymentController {
    private final DeploymentService deploymentService;
    private final ReceiveInformationService receiveInformationService;
    @Autowired
    public DeploymentController(DeploymentService deploymentService, ReceiveInformationService receiveInformationService) {
        this.deploymentService = deploymentService;
        this.receiveInformationService = receiveInformationService;
    }

    // 发送系统校时
    @PostMapping("/sendSystemTiming/communication")
    public ResultEntity sendSystemTiming(@NotNull String timeNow,@NotNull String timingPattern,@NonNull String time, @NonNull String host) throws IOException {
        deploymentService.sendSystemTiming(timeNow,time,timingPattern, host);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
    }

    // 发送分机控制指令
  /*  @PostMapping("/sendExtensionControlCMD/communication")
    public ResultEntity sendExtensionControlCMD(ExtensionControlCMD extensionControlCMD, String host) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, deploymentService.sendExtensionControlCMD(extensionControlCMD, host));
    }

    // 发送系统控制指令
    @PostMapping("/sendSystemConrolCMD/communication")
    public ResultEntity sendSystemControlCMD(SystemControlCMD systemControlCMD, String host) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, deploymentService.sendSystemControlCMD(systemControlCMD, host));
    }

    // 群发系统校时
    @PostMapping("/sendAllSendSystemTiming/communication")
    public ResultEntity sendAllSendSystemTiming(@NotNull String taskFlowNo,@NotNull String timingPattern,@NonNull String time) throws IOException {
        deploymentService.sendAllSendSystemTiming(taskFlowNo,time,timingPattern);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"群发时间成功");
    }

    // 群发分机控制指令
    @PostMapping("/sendAllExtensionControl/communication")
    public ResultEntity sendAllExtensionControl(ExtensionControlCMD extensionControlCMD){
        deploymentService.sendAllExtensionControl(extensionControlCMD);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"群发分机控制指令成功");
    }

    // 群发系统控制指令
    @PostMapping("/sendAllSystemControlCMD/communication")
    public ResultEntity sendAllSystemControlCMD(SystemControlCMD systemControlCMD){
        deploymentService.sendAllSystemControlCMD(systemControlCMD);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"群发系统控制指令成功");
    }*/
    // 设备复位
    @PostMapping("/sendDeviceRestoration/communication")
    public ResultEntity sendDeviceRestoration(String timeNow, String executePattern, String host){
        deploymentService.sendDeviceRestoration(timeNow,executePattern,host);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 设备自检指令
    @PostMapping("/sendDeviceCheckCMD/communication")
    public ResultEntity sendDeviceCheckCMD(DeviceCheckCMD deviceCheckCMD,String host){
        deploymentService.sendDeviceCheckCMD(deviceCheckCMD,host);
     return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 软件版本更新
    @PostMapping("/sendSoftwareUpdateCMD/communication")
    public ResultEntity sendSoftwareUpdateCMD(String timeNow, String cmd, String softwareID, String host){
        deploymentService.sendSoftwareUpdateCMD(timeNow,cmd,softwareID,host);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 设备网络参数更新指令
    @PostMapping("/sendDeviceNetworkCMD/communication")
    public ResultEntity sendDeviceNetworkCMD(SendDeviceNetWorkParam sendDeviceNetWorkParam,String host){
        deploymentService.sendDeviceNetworkCMD(sendDeviceNetWorkParam,host);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功") ;
    }
    // 设备工作流程控制指令
    @PostMapping("/sendDeviceWorkFlowCMD/communication")
    public ResultEntity sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD,int count,String host){
        deploymentService.sendDeviceWorkFlowCMD(deviceWorkFlowCMD,count,host);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS,"发送指令成功");
    }
    // 查询所有
    @GetMapping
    public ResultEntity findAll(){
      return new ResultEntity(SystemStatusCodeEnum.SUCCESS, receiveInformationService.findAll());
    }
}
