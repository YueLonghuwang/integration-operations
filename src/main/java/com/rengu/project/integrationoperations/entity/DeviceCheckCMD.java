package com.rengu.project.integrationoperations.entity;

import antlr.collections.List;

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

    //新加(雷达子系统自检信息)
    private String radarSelfChecking; //自检频率
    private String radarBranChoose;//带宽选择
    private String radarSelfChoose;//自检模式选择
    //敌我子系统自检详细信息
    private String enemyAndUsSelfChecking; //自检频率
    private String enemyAndUsBranChoose;//带宽选择
    private String enemyAndUsSelfChoose;//自检模式选择


    public String getRadarBranChoose() {
        return radarBranChoose;
    }

    public void setRadarBranChoose(String radarBranChoose) {
        this.radarBranChoose = radarBranChoose;
    }

    public String getEnemyAndUsBranChoose() {
        return enemyAndUsBranChoose;
    }

    public void setEnemyAndUsBranChoose(String enemyAndUsBranChoose) {
        this.enemyAndUsBranChoose = enemyAndUsBranChoose;
    }

    public String getRadarSelfChecking() {
        return radarSelfChecking;
    }

    public void setRadarSelfChecking(String radarSelfChecking) {
        this.radarSelfChecking = radarSelfChecking;
    }



    public String getRadarSelfChoose() {
        return radarSelfChoose;
    }

    public void setRadarSelfChoose(String radarSelfChoose) {
        this.radarSelfChoose = radarSelfChoose;
    }

    public String getEnemyAndUsSelfChecking() {
        return enemyAndUsSelfChecking;
    }

    public void setEnemyAndUsSelfChecking(String enemyAndUsSelfChecking) {
        this.enemyAndUsSelfChecking = enemyAndUsSelfChecking;
    }


    public String getEnemyAndUsSelfChoose() {
        return enemyAndUsSelfChoose;
    }

    public void setEnemyAndUsSelfChoose(String enemyAndUsSelfChoose) {
        this.enemyAndUsSelfChoose = enemyAndUsSelfChoose;
    }

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
