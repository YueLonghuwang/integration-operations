package com.rengu.project.integrationoperations.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/10 11:41
 */
@Component
@Slf4j
public class MyAuthenticationFailHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public MyAuthenticationFailHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.info("登录失败:" + exception.getMessage());
        log.info("username=>" + request.getParameter("username"));
        log.info("password=>" + request.getParameter("password"));
        Map<String, Object> map = new HashMap<>();
        if (exception.getMessage().equals("Bad credentials")) {
            map.put("code", "1001");
            map.put("msg", "用户名或密码错误");
            map.put("data", exception.getMessage());
            response.setContentType("application/json;charset=UTF-8");
        } else {
            map.put("code", "1002");
            map.put("msg", "该用户已经在别处登录");
            map.put("data", exception.getMessage());
            response.setContentType("application/json;charset=UTF-8");
        }
        response.getWriter().write(objectMapper.writeValueAsString(map));
    }
}
