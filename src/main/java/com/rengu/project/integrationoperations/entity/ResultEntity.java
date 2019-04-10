package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import lombok.Data;

import java.util.Date;

/**
 *
 * @author hanchangming
 * @date 2019-03-25
 */

@Data
public class ResultEntity {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private int code;
    private String message;
    private Object data;

    public ResultEntity(SystemStatusCodeEnum systemStatusCodeEnum, Object data) {
        this.code = systemStatusCodeEnum.getCode();
        this.message = systemStatusCodeEnum.getMessage();
        this.data = data;
    }
}
