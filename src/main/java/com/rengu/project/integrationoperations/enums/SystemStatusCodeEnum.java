package com.rengu.project.integrationoperations.enums;

import com.rengu.project.integrationoperations.util.BackEndFrameworkApplicationMessage;

/**
 *
 * @author hanchangming
 * @date 2019-03-25
 */

public enum SystemStatusCodeEnum {

    // 未知错误
    UNKNOWN_ERROR(0, "未知错误"),
    // 成功
    SUCCESS(1, "请求成功"),
    // 角色名已存在
    ROLE_NAME_EXISTED(101, BackEndFrameworkApplicationMessage.ROLE_NAME_EXISTED),
    // 角色名不存在
    ROLE_NAME_NOT_FOUND(102, BackEndFrameworkApplicationMessage.ROLE_NAME_NOT_FOUND),
    // 角色名为空值
    ROLE_NAME_NOT_BLANK(103, BackEndFrameworkApplicationMessage.ROLE_NAME_NOT_BLANK),
    // 角色Id不存在
    ROLE_ID_NOT_FOUND(104, BackEndFrameworkApplicationMessage.ROLE_ID_NOT_FOUND),
    // 禁止修改默认角色
    DEFAULT_ROLE_MODIFY_FORBID(105, BackEndFrameworkApplicationMessage.DEFAULT_ROLE_MODIFY_FORBID),
    // 用户名已存在
    USER_USERNAME_EXISTED(201, BackEndFrameworkApplicationMessage.USER_USERNAME_EXISTED),
    // 用户名已存在
    USER_USERNAME_NOT_BLANK(202, BackEndFrameworkApplicationMessage.USER_USERNAME_NOT_BLANK),
    // 用户密码已存在
    USER_PASSWORD_NOT_BLANK(203, BackEndFrameworkApplicationMessage.USER_PASSWORD_NOT_BLANK),
    // 禁止修改默认用户
    DEFAULT_USER_MODIFY_FORBID(204, BackEndFrameworkApplicationMessage.DEFAULT_USER_MODIFY_FORBID),
    // 用户Id不存在
    USER_ID_NOT_FOUND(205, BackEndFrameworkApplicationMessage.USER_ID_NOT_FOUND),
    // 用户名不存在
    USER_NAME_NOT_FOUND(205, BackEndFrameworkApplicationMessage.USER_ID_NOT_FOUND),
    //  登出成功
    LOGOUT_SUCCESS(206, BackEndFrameworkApplicationMessage.LOGOUT_SUCCESS),


    //  分机控制指令
    ExtensionControlCharacter_ERROR(301, BackEndFrameworkApplicationMessage.ExtensionControlCharacter_ERROR),
    SOCKET_CONNENT_ERROR(302, BackEndFrameworkApplicationMessage.SOCKET_CONNENT_ERROR),
    SOCKET_FINALLY_ERROR(303, BackEndFrameworkApplicationMessage.SOCKET_FINALLY_ERROR),
    EXCEED_THE_LIMIT(304, BackEndFrameworkApplicationMessage.EXCEED_THE_LIMIT);
    private int code;
    private String message;

    SystemStatusCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static SystemStatusCodeEnum getSystemStatusCodeEnum(String message) {
        for (SystemStatusCodeEnum systemStatusCodeEnum : values()) {
            if (systemStatusCodeEnum.getMessage().equals(message)) {
                return systemStatusCodeEnum;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
