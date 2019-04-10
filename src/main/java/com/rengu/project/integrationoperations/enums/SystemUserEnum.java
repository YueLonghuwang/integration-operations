package com.rengu.project.integrationoperations.enums;

/**
 *
 * @author hanchangming
 * @date 2019-03-26
 */
public enum SystemUserEnum {
    // 初始管理员用户
    ADMIN("admin", "admin", SystemRoleEnum.ADMIN, SystemRoleEnum.USER),
    USER1("user1", "user1", SystemRoleEnum.ADMIN, SystemRoleEnum.USER),
    USER2("user2", "user2", SystemRoleEnum.ADMIN, SystemRoleEnum.USER);
    private String username;
    private String password;
    private SystemRoleEnum[] systemRoleEnums;

    SystemUserEnum(String username, String password, SystemRoleEnum... systemRoleEnums) {
        this.username = username;
        this.password = password;
        this.systemRoleEnums = systemRoleEnums;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public SystemRoleEnum[] getSystemRoleEnums() {
        return systemRoleEnums;
    }
}
