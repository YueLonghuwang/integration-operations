package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.ExtensionControlCMD;
import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.entity.SystemControlCMD;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.DeploymentService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:30
 */
@RestController
@RequestMapping(path = "/deployment")
public class DeploymentController {
    private final DeploymentService deploymentService;

    @Autowired
    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @PostMapping("/sendSystemTiming/communication")
    public ResultEntity sendMessage(@NonNull String time, @NonNull String host) {
        deploymentService.sendSystemTiming(time, host);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
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
