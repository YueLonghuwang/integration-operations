package com.rengu.project.integrationoperations.entity;

import lombok.Data;

@Data
public class LabelPackageInfo {
    private String sendNodeNum;  //  发方节点号
    private String receiveNodeNum;  //  收方节点号
//  private String feedbackCmdSerialNum; //  反馈指令序号
    private String receiveCmdState; //  指令接收状态
    private String frontEndWorkT;  //  前端工作温度
    private String extensionWorkT;  // 分机工作温度
    private String extensionWorkState; // 分机工作状态
//    private String overallPulseCount; // 全脉冲个数统计
}

