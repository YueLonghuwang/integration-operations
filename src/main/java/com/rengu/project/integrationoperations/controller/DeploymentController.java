package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.DeploymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    //  根据ID进行websocket通信，发送指令
    @GetMapping("/{equipmentId}/communication")
    public ResultEntity sendMessage(@PathVariable(value = "equipmentId") String equipmentId, String message) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, deploymentService.sendMessage(equipmentId, message));
    }

    // 接收指令
    @GetMapping("/receive/communication")
    public ResultEntity receiveMessage() {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, deploymentService.receiveMessage());
    }

}
