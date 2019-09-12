package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.UUID;

/**
 * @author yyc
 * @date 2019/9/12 8:45
 * 插入日志到数据库
 */
@Entity
@Data
public class SysLogEntity {
    @Id
    private String id= UUID.randomUUID().toString();
    //private String userId;  //操作员id
    private String userAction; //用户操作
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss",timezone = "GMT-8")
    private Date createTime=new Date();
}
