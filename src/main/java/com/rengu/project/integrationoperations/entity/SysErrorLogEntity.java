package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/10/8 12:51
 */
@Entity
@Data

public class SysErrorLogEntity implements Serializable {
    @Id
    private String id= UUID.randomUUID().toString();
    //private String userId;  //操作员id
    private String host;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss",timezone = "GMT-8")
    private Date createTime=new Date();
    private String errorType;
    private String errorMsg;

}
