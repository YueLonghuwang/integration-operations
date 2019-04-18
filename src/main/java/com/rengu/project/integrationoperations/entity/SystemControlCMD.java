package com.rengu.project.integrationoperations.entity;

import lombok.Data;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 12:54
 */
@Data
public class SystemControlCMD {
    private String workPattern;  //  工作模式 0:自检 1:频域搜索 2:驻留 3:空域搜索 4:中频采集 5:敌我1030 6:敌我1090 其他:无效
    private String workCycle; //  工作周期 单位50ms
    private String workCycleAmount; //  工作周期数,默认为1
    private String beginFrequency; //  起始频率 1MHz
    private String endFrequency;  //  终止频率 1MHz
    private String steppedFrequency; // 频率步进 单位MHZ
    private String chooseBandwidth;  // 带宽选择
    private String chooseAntenna1;  //  天线一选择
    private String chooseAntenna2;  //  天线二选择
    private String attenuationRF1;  // 射频一衰减
    private String attenuationRF2;  // 射频二衰减
    private String balancedAttenuationRF1;  // 射频一长电缆均衡衰减控制
    private String balancedAttenuationRF2;  // 射频二长电缆均衡衰减控制
    private String attenuationMF1;  // 中频一衰减
    private String attenuationMF2;  //  中频二衰减
    private String attenuationControlWay;  // 衰减码控制方式
    private String selfInspectionAttenuation;  // 自检源衰减
    private String guidanceSwitch;  // 脉内引导批次号开关
    private String guidance;  //  脉内引导批次
    private String faultDetect; //  故障检测门限
    private String timingCode;  // 定时时间码
    private String onceExecuteCMDTimeNeeded;  // 单次执行指令集所需时间
}
