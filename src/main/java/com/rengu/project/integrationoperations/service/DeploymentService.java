package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.util.JavaClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: yaojiahao
 * @Date: 2019/4/12 13:32
 */
@Service
public class DeploymentService {
    private final UserService userService;
    private final JavaClientUtil javaClientUtil;

    @Autowired
    public DeploymentService(UserService userService, JavaClientUtil javaClientUtil) {
        this.userService = userService;
        this.javaClientUtil = javaClientUtil;
    }

    /**
     * Todo
     * 根据用户ID，以及设备ID判断该用户对设备操作
     * 是否要发送心跳？
     * 如何接收设备的信息，接收设备的信息是要封装成实体类吗？
     */
//
//     public  receiveMessage(@PathVariable(value = "userId")String userId, String message){
//           if(!userService.hasUserByUsername(userService.getUserById(userId).getUsername())){
//
//             }
//        }
//
    // 发送信息至后端
    public String sendMessage(String equipmentId, String message) {
        javaClientUtil.sendMessage(message);
        return "SUCCESS";
    }

    // 接收信息至前端
    public String receiveMessage() {
        javaClientUtil.receiveMessage();
        return "SUCCESS";
    }
}
