package com.rengu.project.integrationoperations.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeviceRestorationSendCMD implements Serializable {
    private String timeNow;
    private String executePattern;
}
