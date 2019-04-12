package com.rengu.project.integrationoperations.util;

import com.rengu.project.integrationoperations.entity.RoleEntity;
import com.rengu.project.integrationoperations.entity.UserEntity;
import com.rengu.project.integrationoperations.enums.SystemRoleEnum;
import com.rengu.project.integrationoperations.enums.SystemUserEnum;
import com.rengu.project.integrationoperations.service.RoleService;
import com.rengu.project.integrationoperations.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author hanchangming
 * @date 2019-03-25
 */

@Slf4j
@Component
public class BackEndFrameworkApplicationInit implements ApplicationRunner {

    private final RoleService roleService;
    private final UserService userService;

    @Autowired
    public BackEndFrameworkApplicationInit(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // 建立系统角色
        List<RoleEntity> roleEntityList = new ArrayList<>();
        for (SystemRoleEnum systemRoleEnum : SystemRoleEnum.values()) {
            if (!roleService.hasRoleByName(systemRoleEnum.getName())) {
                RoleEntity roleEntity = new RoleEntity(systemRoleEnum);
                roleEntityList.add(roleEntity);
            }
        }
        if (!roleEntityList.isEmpty()) {
            roleService.saveRoles(roleEntityList);
            log.info("系统成功初始化" + roleEntityList.size() + "个角色");
        }
        //  建立系统用户
        List<UserEntity> userEntityList = new ArrayList<>();
        for (SystemUserEnum systemUserEnum : SystemUserEnum.values()) {
            if (!userService.hasUserByUsername(systemUserEnum.getUsername())) {
                UserEntity userEntity = new UserEntity(systemUserEnum);
                Set<RoleEntity> roleEntitySet = new HashSet<>();
                for (SystemRoleEnum systemRoleEnum : systemUserEnum.getSystemRoleEnums()) {
                    roleEntitySet.add(roleService.getRoleByName(systemRoleEnum.getName()));
                }
                userEntity.setRoles(roleEntitySet);
                userEntityList.add(userEntity);
            }
        }
        if (!userEntityList.isEmpty()) {
            userService.saveUsers(userEntityList);
            log.info("系统成功初始化" + userEntityList.size() + "个用户");
        }
        // 建立系统角色
//        List<RoleEntity> roleEntityList = new ArrayList<>();
//        for (SystemRoleEnum systemRoleEnum : SystemRoleEnum.values()) {
//            if (!roleService.hasRoleByName(systemRoleEnum.getName())) {
//                RoleEntity roleEntity = new RoleEntity(systemRoleEnum);
//                roleEntityList.add(roleEntity);
//            }
//        }
//        if (!roleEntityList.isEmpty()) {
//            roleService.saveRoles(roleEntityList);
//            log.info("系统成功初始化" + roleEntityList.size() + "个角色。");
//        }
//        // 建立系统用户
//        List<UserEntity> userEntityList = new ArrayList<>();
//        for (SystemUserEnum systemUserEnum : SystemUserEnum.values()) {
//            if (!userService.hasUserByUsername(systemUserEnum.getUsername())) {
//                UserEntity userEntity = new UserEntity(systemUserEnum);
//                Set<RoleEntity> roleEntitySet = new HashSet<>();
//                for (SystemRoleEnum systemRoleEnum : systemUserEnum.getSystemRoleEnums()) {
//                    roleEntitySet.add(roleService.getRoleByName(systemRoleEnum.getName()));
//                }
//                userEntity.setRoles(roleEntitySet);
//                userEntityList.add(userEntity);
//            }
//        }
//        if (!userEntityList.isEmpty()) {
//            userService.saveUsers(userEntityList);
//            log.info("系统成功初始化" + userEntityList.size() + "个用户。");
//        }
    }
}