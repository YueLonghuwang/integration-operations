package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.service.ReceiveInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReceiveInfoController {

    private final ReceiveInfoService receiveInfoService;
    @Autowired
    public ReceiveInfoController(ReceiveInfoService receiveInfoService) {
        this.receiveInfoService = receiveInfoService;
    }
}
