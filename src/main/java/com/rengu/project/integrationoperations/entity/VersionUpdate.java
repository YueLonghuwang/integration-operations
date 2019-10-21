package com.rengu.project.integrationoperations.entity;

/**
 * @author yyc
 * @date 2019/9/13 13:10
 * 版本更新信息表
 */
public class VersionUpdate {
    private String messageLength; // 信息长度
    private String taskFlowNo; // 任务流水号
    private String operationType; //操作类型
    private String totalPackageNumber; //总包数
    private String crrentPacketNumber;//当前包数
    private String state;//状态指示
    private String softwareVersion;//软件版本信息

    public String getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(String messageLength) {
        this.messageLength = messageLength;
    }

    public String getTaskFlowNo() {
        return taskFlowNo;
    }

    public void setTaskFlowNo(String taskFlowNo) {
        this.taskFlowNo = taskFlowNo;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getTotalPackageNumber() {
        return totalPackageNumber;
    }

    public void setTotalPackageNumber(String totalPackageNumber) {
        this.totalPackageNumber = totalPackageNumber;
    }

    public String getCrrentPacketNumber() {
        return crrentPacketNumber;
    }

    public void setCrrentPacketNumber(String crrentPacketNumber) {
        this.crrentPacketNumber = crrentPacketNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }
}
