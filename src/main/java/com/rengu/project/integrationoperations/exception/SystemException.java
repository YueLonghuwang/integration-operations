package com.rengu.project.integrationoperations.exception;

import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import lombok.Getter;

/**
 *
 * @author hanchangming
 * @date 2019-03-25
 */

@Getter
public class SystemException extends RuntimeException {

    private SystemStatusCodeEnum systemStatusCodeEnum;

    public SystemException(SystemStatusCodeEnum systemStatusCodeEnum) {
        super(systemStatusCodeEnum.getMessage());
        this.systemStatusCodeEnum = systemStatusCodeEnum;
    }
}