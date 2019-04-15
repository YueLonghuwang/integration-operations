package com.rengu.project.integrationoperations.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 14:40
 */
//@Entity
@Data
public class EquipmentEntity implements Serializable {
    //        @Id
    private String id = UUID.randomUUID().toString();
    //        @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime = new Date();
    private String hostAddress;
    private String description;
    private String deployPath;
}
