package com.rengu.project.integrationoperations.entity;

/**
 * author : yaojiahao
 * Date: 2019/7/9 16:45
 **/


public class DeviceCheckCMD {
    private String messageLength; // 信息长度
    private String taskFlowNo; // 任务流水号
    private String checkType; // 自检类型
    private String checkPeriod; // 自检周期
    private String checkNum; // 自检数量
    private String SingleMachineCode; // 被检单机代码
    public void setMessageLength(String messageLength) {
        this.messageLength = messageLength;
    }

    public void setTaskFlowNo(String taskFlowNo) {
        this.taskFlowNo = taskFlowNo;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

    public void setCheckPeriod(String checkPeriod) {
        this.checkPeriod = checkPeriod;
    }

    public void setCheckNum(String checkNum) {
        this.checkNum = checkNum;
    }

    public void setSingleMachineCode(String singleMachineCode) {
        SingleMachineCode = singleMachineCode;
    }

    public String getMessageLength() {
        return messageLength;
    }

    public String getTaskFlowNo() {
        return taskFlowNo;
    }

    public String getCheckType() {
        return checkType;
    }

    public String getCheckPeriod() {
        return checkPeriod;
    }

    public String getCheckNum() {
        return checkNum;
    }

    public String getSingleMachineCode() {
        return SingleMachineCode;
    }

}
