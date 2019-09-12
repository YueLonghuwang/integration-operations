package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.repository.HostRepository;
import com.rengu.project.integrationoperations.service.WebSendToCService;
import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;
import javax.swing.text.html.ImageView;
import javax.validation.constraints.NotNull;
import javax.xml.transform.Result;
import java.io.IOException;

/**
 * web端发送到c++端
 *
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:30
 */
@RestController
@RequestMapping(path = "/deployment")
@Slf4j
public class WebSendToCController {
    private final WebSendToCService webSendToCService;
    private final WebReceiveToCService receiveInformationService;
    // 设置同批数据为同一序号
    private final int serialNumber = 0;

    @Autowired
    public WebSendToCController(WebSendToCService webSendToCService, WebReceiveToCService receiveInformationService) {
        this.webSendToCService = webSendToCService;
        this.receiveInformationService = receiveInformationService;
    }

    // 发送系统校时
    @PostMapping("/sendSystemTiming/communication")
    public ResultEntity sendSystemTiming(@NotNull String timeNow, @NotNull String timingPattern, @NonNull String time, @NonNull String host, @NonNull String updateAll) throws IOException {
        try {
            webSendToCService.sendSystemTiming(timeNow, time, timingPattern, host, updateAll, serialNumber);
        } catch (Exception e) {
            log.info("发送系统校时异常");
        }
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
    }

    //添加定时发送
    @PostMapping("/addSystemTimingTask/communication")
    public ResultEntity addSystemTimingTask(@NotNull String timeNow,@NotNull String timingPattern, @NonNull String time,@NotNull String sendTime, @NonNull String host, @NonNull String updateAll){

        webSendToCService.addTimeSendTask(timeNow, time,sendTime, timingPattern, host, updateAll, serialNumber);

        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "添加定时任务成功");
    }



    // 设备复位
    @PostMapping("/sendDeviceRestoration/communication")
    public ResultEntity sendDeviceRestoration(String timeNow, String executePattern, String host, @NonNull String updateAll) {
        try {
            webSendToCService.sendDeviceRestoration(timeNow, executePattern, host, updateAll, serialNumber);
        } catch (Exception e) {
            log.info("设备复位异常", e);
        }
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
    }

    // 设备自检指令
    @PostMapping("/sendDeviceCheckCMD/communication")
    public ResultEntity sendDeviceCheckCMD(DeviceCheckCMD deviceCheckCMD, String host, @NonNull String updateAll) {
        try {
            webSendToCService.sendDeviceCheckCMD(deviceCheckCMD, host, updateAll, serialNumber);
        } catch (Exception e) {
            log.info("设备自检指令异常", e);
        }
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
    }

    // 软件版本更新
    @PostMapping("/sendSoftwareUpdateCMD/communication")
    public ResultEntity sendSoftwareUpdateCMD(String timeNow, String cmd, String softwareID, String host, @NonNull String updateAll) {
        try {
            webSendToCService.sendSoftwareUpdateCMD(timeNow, cmd, softwareID, host, updateAll, serialNumber);
        } catch (Exception e) {
            log.info("软件版本更新异常", e);
        }
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
    }

    // 设备网络参数更新指令
    @PostMapping("/sendDeviceNetworkCMD/communication")
    public ResultEntity sendDeviceNetworkCMD(SendDeviceNetWorkParam sendDeviceNetWorkParam, String host, @NonNull String updateAll) {
        try {
            webSendToCService.sendDeviceNetworkCMD(sendDeviceNetWorkParam, host, updateAll, serialNumber);
        } catch (Exception e) {
            log.info("设备网络参数更新指令异常", e);
        }
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送指令成功");
    }

    // 设备工作流程控制指令
    @PostMapping("/sendDeviceWorkFlowCMD/communication")
    public ResultEntity sendDeviceWorkFlowCMD(DeviceWorkFlowCMD deviceWorkFlowCMD, SystemControlCMD systemControlCMD, int count, String host, @NonNull String updateAll) {
        try {
            webSendToCService.sendDeviceWorkFlowCMD(deviceWorkFlowCMD, systemControlCMD, count, host, updateAll, serialNumber);
        } catch (Exception e) {
            log.info("设备工作流程控制指令异常", e);
        }
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送控制指令成功");
    }

    //设备工作流程控制指令(雷达分机指令)
    @PostMapping("/sendExtensionInstructionsCMD/communication")
    public ResultEntity sendExtensionInstructions(DeviceWorkFlowCMD deviceWorkFlowCMD, int count, String host, String updateAll,int radarExtensionNum) {
        webSendToCService.sendExtensionInstructionsCMD(deviceWorkFlowCMD, count, host, updateAll, serialNumber,radarExtensionNum);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送雷达分机指令成功");
    }

    //设备工作流程控制指令(雷达系统指令)
    @PostMapping("/sendSystemInstructionsCMD/communication")
    public ResultEntity sendSystemInstructions(SystemControlCMD systemControlCMD, int count, String host, String updateAll,int radarSystemNum) {
        //return new ResultEntity(SystemStatusCodeEnum.SUCCESS, webSendToCService.sendSystemInstructionsCMD(systemControlCMD, count, host, updateAll, serialNumber,radarSystemNum));
        webSendToCService.sendSystemInstructionsCMD(systemControlCMD, count, host, updateAll, serialNumber,radarSystemNum);
         return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送雷达系统指令成功");
    }

    // 查询所有
    @GetMapping
    public ResultEntity findAll() {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, receiveInformationService.findAll());
    }

}
