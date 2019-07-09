package com.rengu.project.integrationoperations.entity;

import lombok.Data;

/**
 * author : yaojiahao
 * Date: 2019/7/9 13:16
 **/

public class DeviceNetWorkParam {
    private int messageLength; // 信息长度
    private byte[] taskFlowNo; // 任务流水号
    private short cmd; // 指令操作码
    private short networkID; // 网络终端ID号
    private int networkIP1; // 网络IP地址1
    private byte[] networkMacIP1; // 网络MAC地址1
    private int networkMessage1; // 网络端口信息1
    private int networkIP2; // 网络IP地址2
    private byte[] networkMacIP2; // 网络MAC地址2
    private int networkMessage2; // 网络端口信息2
    private int networkIP3; // 网络IP地址3
    private byte[] networkMacIP3; // 网络MAC地址3
    private int networkMessage3; // 网络端口信息3
    private int networkIP4; // 网络IP地址4
    private byte[] networkMacIP4; // 网络MAC地址4
    private int networkMessage4; // 网络端口信息4

    public int getMessageLength() {
        return messageLength;
    }

    public byte[] getTaskFlowNo() {
        return taskFlowNo;
    }

    public short getCmd() {
        return cmd;
    }

    public short getNetworkID() {
        return networkID;
    }

    public int getNetworkIP1() {
        return networkIP1;
    }

    public byte[] getNetworkMacIP1() {
        return networkMacIP1;
    }

    public int getNetworkMessage1() {
        return networkMessage1;
    }

    public int getNetworkIP2() {
        return networkIP2;
    }

    public byte[] getNetworkMacIP2() {
        return networkMacIP2;
    }

    public int getNetworkMessage2() {
        return networkMessage2;
    }

    public int getNetworkIP3() {
        return networkIP3;
    }

    public byte[] getNetworkMacIP3() {
        return networkMacIP3;
    }

    public int getNetworkMessage3() {
        return networkMessage3;
    }

    public int getNetworkIP4() {
        return networkIP4;
    }

    public byte[] getNetworkMacIP4() {
        return networkMacIP4;
    }

    public int getNetworkMessage4() {
        return networkMessage4;
    }

    public void setMessageLength(int messageLength) {
        this.messageLength = messageLength;
    }

    public void setTaskFlowNo(byte[] taskFlowNo) {
        this.taskFlowNo = taskFlowNo;
    }

    public void setCmd(short cmd) {
        this.cmd = cmd;
    }

    public void setNetworkID(short networkID) {
        this.networkID = networkID;
    }

    public void setNetworkIP1(int networkIP1) {
        this.networkIP1 = networkIP1;
    }

    public void setNetworkMacIP1(byte[] networkMacIP1) {
        this.networkMacIP1 = networkMacIP1;
    }

    public void setNetworkMessage1(int networkMessage1) {
        this.networkMessage1 = networkMessage1;
    }

    public void setNetworkIP2(int networkIP2) {
        this.networkIP2 = networkIP2;
    }

    public void setNetworkMacIP2(byte[] networkMacIP2) {
        this.networkMacIP2 = networkMacIP2;
    }

    public void setNetworkMessage2(int networkMessage2) {
        this.networkMessage2 = networkMessage2;
    }

    public void setNetworkIP3(int networkIP3) {
        this.networkIP3 = networkIP3;
    }

    public void setNetworkMacIP3(byte[] networkMacIP3) {
        this.networkMacIP3 = networkMacIP3;
    }

    public void setNetworkMessage3(int networkMessage3) {
        this.networkMessage3 = networkMessage3;
    }

    public void setNetworkIP4(int networkIP4) {
        this.networkIP4 = networkIP4;
    }

    public void setNetworkMacIP4(byte[] networkMacIP4) {
        this.networkMacIP4 = networkMacIP4;
    }

    public void setNetworkMessage4(int networkMessage4) {
        this.networkMessage4 = networkMessage4;
    }

}
