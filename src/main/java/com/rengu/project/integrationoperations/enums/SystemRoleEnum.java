package com.rengu.project.integrationoperations.enums;

/**
 *
 * @author hanchangming
 * @date 2019-03-25
 */
public enum SystemRoleEnum {

    // 普通用户
    USER("user", "普通用户"),
    // 管理员用户
    ADMIN("admin", "管理员用户");

    private String name;
    private String description;

    SystemRoleEnum(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
