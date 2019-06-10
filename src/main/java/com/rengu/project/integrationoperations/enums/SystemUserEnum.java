package com.rengu.project.integrationoperations.enums;

/**
 *
 * @author hanchangming
 * @date 2019-03-26
 */
public enum SystemUserEnum {
    // 初始管理员用户
    ADMIN("admin", "admin", SystemRoleEnum.ADMIN, SystemRoleEnum.USER),
    ADMIN1("admin1", "admin1", SystemRoleEnum.ADMIN, SystemRoleEnum.USER),
    USER1("user1", "user1", SystemRoleEnum.USER, SystemRoleEnum.USER),
    USER2("user2", "user2", SystemRoleEnum.USER, SystemRoleEnum.USER),
    USER3("user3", "user3", SystemRoleEnum.USER, SystemRoleEnum.USER),
    USER4("user4", "user4", SystemRoleEnum.USER, SystemRoleEnum.USER),
    USER5("user5", "user5", SystemRoleEnum.USER, SystemRoleEnum.USER),
    USER6("user6", "user6", SystemRoleEnum.USER, SystemRoleEnum.USER);
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
