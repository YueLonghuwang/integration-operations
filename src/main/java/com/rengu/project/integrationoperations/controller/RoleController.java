package com.rengu.project.integrationoperations.controller;

import com.rengu.project.integrationoperations.entity.ResultEntity;
import com.rengu.project.integrationoperations.entity.RoleEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author hanchangming
 * @date 2019-03-21
 */

@Slf4j
@RestController
@RequestMapping(path = "/roles")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResultEntity saveRole(@Validated RoleEntity roleEntity) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, roleService.saveRole(roleEntity));
    }

    @DeleteMapping(value = "/{roleId}")
    public ResultEntity deleteRoleById(@PathVariable(value = "roleId") String roleId) {
        roleService.deleteRoleById(roleId);
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, null);
    }

    @PutMapping(value = "/{roleId}")
    public ResultEntity updateRoleById(@PathVariable(value = "roleId") String roleId, @Validated RoleEntity roleEntity) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, roleService.updateRoleById(roleId, roleEntity));
    }

    @GetMapping(value = "/{roleId}")
    public ResultEntity getRoleById(@PathVariable(value = "roleId") String roleId) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, roleService.getRoleById(roleId));
    }

    @GetMapping
    public ResultEntity getRoles(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return new ResultEntity(SystemStatusCodeEnum.SUCCESS, roleService.getRoles(pageable));
    }
}
