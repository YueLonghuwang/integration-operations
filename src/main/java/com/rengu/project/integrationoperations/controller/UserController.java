package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.entity.RoleEntity;
import com.rengu.project.integrationoperations.entity.UserEntity;
import com.rengu.project.integrationoperations.enums.SystemRoleEnum;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.RoleService;
import com.rengu.project.integrationoperations.service.UserService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author hanchangming
 * @date 2019-03-21
 */

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    //  新增用户
    @PostMapping
    public ResultEntity saveUser(@Validated UserEntity userEntity) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.saveUser(userEntity, roleService.getRoleByName(SystemRoleEnum.USER.getName())));
    }

    //  删除用户
    @DeleteMapping(value = "/{userId}")
    public ResultEntity deleteUserById(@PathVariable(value = "userId") String userId) {
        userService.deleteUserById(userId);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
    }

    //  判断是不是管理员登陆，并且删除普通用户
    @PreAuthorize(value = "hasRole('admin')")
    @DeleteMapping(value = "/{userId}/roles/{roleId}")
    public ResultEntity removeRolesById(@PathVariable(value = "userId") String userId, @PathVariable(value = "roleId") String roleId) {
        RoleEntity roleEntity = roleService.getRoleById(roleId);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.removeRolesById(userId, roleEntity));
    }

    //  更新用户
    @PutMapping(value = "/{userId}")
    public ResultEntity updateUserById(@PathVariable(value = "userId") String userId, @Validated UserEntity userArgs) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.updateUserById(userId, userArgs));
    }

    //  更新用户密码
    @PatchMapping(value = "/{userId}/password")
    public ResultEntity updateUserPasswordById(@PathVariable(value = "userId") String userId, @NonNull String password) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.updateUserPasswordById(userId, password));
    }

    //  根据用户名修改密码
    @PatchMapping(value = "/changePassword")
    public ResultEntity updateUserPasswordByUserName(@NonNull String username, @NonNull String password) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.updateUserPasswordByUserName(username, password));
    }
    //  通过管理员更新用户信息
    @PreAuthorize(value = "hasRole('admin')")
    @PatchMapping(value = "/{userId}/roles/{roleId}")
    public ResultEntity addRolesById(@PathVariable(value = "userId") String userId, @PathVariable(value = "roleId") String roleId) {
        RoleEntity roleEntity = roleService.getRoleById(roleId);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.addRolesById(userId, roleEntity));
    }

    //  通过用户ID查询数据
    @GetMapping(value = "/{userId}")
    public ResultEntity getUserById(@PathVariable(value = "userId") String userId) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.getUserById(userId));
    }

    //  通过管理员查询
    @PreAuthorize(value = "hasRole('admin')")
    @GetMapping
    public ResultEntity getUsers(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, userService.getUsers(pageable));
    }

}
