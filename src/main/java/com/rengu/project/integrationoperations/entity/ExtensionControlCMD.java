package com.rengu.project.integrationoperations.entity;

import lombok.Data;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/16 12:56
 */
@Data
public class ExtensionControlCMD {
    private String pulse;  //  内外秒脉冲选择
    private String extensionControlCharacter;  // 分机控制字
    private String threshold;  //检测门限调节
    private String overallpulse;  //  需要上传的全脉冲数
    private String minamplitude; // 最小幅度
    private String maxamplitude;  // 最大幅度
    private String minPulsewidth; // 最小脉宽
    private String maxPulsewidth; // 最大脉宽
    private String filterMaximumFrequency; // 筛选最大频率
    private String filterMinimumFrequency; // 筛选最小频率
    private String shieldingMaximumFrequency; // 屏蔽最大频率
    private String shieldingMinimumFrequency; // 屏蔽最小频率
    private String defalutUpdate;  // 默认值更新标记
}
