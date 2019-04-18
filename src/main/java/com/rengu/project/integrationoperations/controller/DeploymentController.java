package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.ExtensionControlCMD;
import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.entity.SystemControlCMD;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.DeploymentService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:30
 */
@Controller
@RequestMapping("/deployment")
public class DeploymentController {
    private final DeploymentService deploymentService;

    @Autowired
    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public static void main(String[] args) {

    }

    @GetMapping("/sendSystemTiming/communication")
    public ResultEntity sendMessage(@NonNull String time, @NonNull String host) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, deploymentService.sendSystemTiming(time, host));
    }

    @PostMapping("/sendExtensionControlCMD/communication")
    public ResultEntity sendMessage(ExtensionControlCMD extensionControlCMD, String host) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, deploymentService.sendExtensionControlCMD(extensionControlCMD, host));
    }

    @PostMapping("/sendSystemConrolCMD/communication")
    public ResultEntity sendMessage(SystemControlCMD systemControlCMD, String host) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, deploymentService.sendSystemControlCMD(systemControlCMD, host));

    }
}
