package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;

import java.util.Date;

/**
 * @author hanchangming
 * @date 2019-03-25
 */

public class ResultEntity {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private int code;

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private String message;

    public Date getCreateTime() {
        return createTime;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    private Object data;

    public ResultEntity(SystemStatusCodeEnum systemStatusCodeEnum, Object data) {
        this.code = systemStatusCodeEnum.getCode();
        this.message = systemStatusCodeEnum.getMessage();
        this.data = data;
    }
}
