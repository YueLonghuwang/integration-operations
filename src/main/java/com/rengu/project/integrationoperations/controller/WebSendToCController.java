package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.configuration.LogConfig;
import com.rengu.project.integrationoperations.entity.*;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.SysErrorLogService;
import com.rengu.project.integrationoperations.service.SysLogService;
import com.rengu.project.integrationoperations.service.WebSendToCService;
import com.rengu.project.integrationoperations.service.WebReceiveToCService;
import lombok.NonNull;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Date;

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
    private final SysErrorLogService sysErrorLogService;

    // 设置同批数据为同一序号
    private final int serialNumber = 0;

    @Autowired
    public WebSendToCController(WebSendToCService webSendToCService, WebReceiveToCService receiveInformationService, SysErrorLogService sysErrorLogService) {
        this.webSendToCService = webSendToCService;
        this.receiveInformationService = receiveInformationService;
        this.sysErrorLogService = sysErrorLogService;
    }

    // 发送系统校时
    @PostMapping("/sendSystemTiming/communication")
    @LogConfig("发送系统校时")
    public ResultEntity sendSystemTiming(@NotNull String timeNow, @NotNull String timingPattern, @NonNull String time, @NonNull String host, @NonNull String updateAll) throws IOException {
        try {
            webSendToCService.sendSystemTiming(timeNow, time, timingPattern, host, updateAll, serialNumber);
        } catch (Exception e) {
            log.info("发送系统校时异常");
            SysErrorLogEntity sysErrorLogEntity = new SysErrorLogEntity();
            sysErrorLogEntity.setHost(host);
            sysErrorLogEntity.setErrorMsg("发送系统校时异常");
            sysErrorLogEntity.setErrorType("系统异常");
            sysErrorLogEntity.setCreateTime(new Date());
            sysErrorLogService.saveError(sysErrorLogEntity);
        }
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
    }

    // 设备复位
    @PostMapping("/sendDeviceRestoration/communication")
    @LogConfig("设备复位")
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
    @LogConfig("设备自检指令")
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
    @LogConfig("软件版本更新")
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
    @LogConfig("设备网络参数更新指令")
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
    @LogConfig("设备工作流程控制指令")
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
    @LogConfig("设备工作流程控制指令(雷达分机指令)")
    public ResultEntity sendExtensionInstructions(DeviceWorkFlowCMD deviceWorkFlowCMD, int count, String host, String updateAll,int radarExtensionNum) {
        webSendToCService.sendExtensionInstructionsCMD(deviceWorkFlowCMD, count, host, updateAll, serialNumber,radarExtensionNum);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, "发送雷达分机指令成功");
    }

    //设备工作流程控制指令(雷达系统指令)
    @PostMapping("/sendSystemInstructionsCMD/communication")
    @LogConfig("设备工作流程控制指令(雷达系统指令)")
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
