package com.rengu.project.integrationoperations.controller;


import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 *
 * @author hanchangming
 * @date 2019-03-25
 */

@Slf4j
@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(value = SystemException.class)
    public ResultEntity systemExceptioHandler(HttpServletResponse httpServletResponse, SystemException e) {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResultEntity(e.getSystemStatusCodeEnum(), e.getMessage());
    }

    @ExceptionHandler(value = BindException.class)
    public ResultEntity bindExceptionHandler(HttpServletResponse httpServletResponse, BindException e) {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResultEntity(Objects.requireNonNull(SystemStatusCodeEnum.getSystemStatusCodeEnum(e.getBindingResult().getFieldError().getDefaultMessage())), e.getMessage());
    }
}