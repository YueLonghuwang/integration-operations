package com.rengu.project.integrationoperations.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rengu.project.integrationoperations.entity.UserEntity;
import com.rengu.project.integrationoperations.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/10 11:39
 */
@Component
@Slf4j
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {


    @Autowired
    private ObjectMapper objectMapper;

    @Lazy
    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserEntity userEntity = userService.getUserByUsername(request.getParameter("username"));
        log.info("登录成功");
        log.info("username=>" + request.getParameter("username"));
        Map<String, Object> map = new HashMap<>();
        map.put("code", "0");
        map.put("msg", "登录成功");
        map.put("token", userEntity);
        map.put("username", request.getParameter("username"));
        map.put("password", request.getParameter("password"));
        userEntity.getRoles();
//        Cookie cookie=new Cookie("username",request.getParameter("username"));
//        response.addCookie(cookie);
//        Cookie cookie1=new Cookie("password",new BCryptPasswordEncoder().encode(request.getParameter("password")));
//        response.addCookie(cookie1);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(map));
    }
}
