package com.rengu.project.integrationoperations.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SystemSendTimingCMD implements Serializable {
    private String timeNow;
    private String time;
    private String timingPattern;
}
