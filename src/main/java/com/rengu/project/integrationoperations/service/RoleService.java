package com.rengu.project.integrationoperations.service;


import com.rengu.project.integrationoperations.entity.RoleEntity;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author hanchangming
 * @date 2019-03-21
 */

@Slf4j
@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    // 根据角色名称查询角色是否存在
    public boolean hasRoleByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return roleRepository.existsByName(name);
    }

    // 保存多个角色
    public List<RoleEntity> saveRoles(List<RoleEntity> roleEntityList) {
        return roleRepository.saveAll(roleEntityList);
    }

    // 保存角色
    public RoleEntity saveRole(RoleEntity roleEntity) {
        if (hasRoleByName(roleEntity.getName())) {
            throw new SystemException(SystemStatusCodeEnum.ROLE_NAME_EXISTED);
        }
        return roleRepository.save(roleEntity);
    }

    // 根据Id删除角色
    public void deleteRoleById(String roleId) {
        RoleEntity roleEntity = getRoleById(roleId);
        if (roleEntity.isDefaultRole()) {
            throw new SystemException(SystemStatusCodeEnum.DEFAULT_ROLE_MODIFY_FORBID);
        }
        roleRepository.delete(roleEntity);
    }

    // 根据Id更新角色信息
    public RoleEntity updateRoleById(String roleId, RoleEntity roleArgs) {
        RoleEntity roleEntity = getRoleById(roleId);
        if (roleEntity.isDefaultRole()) {
            throw new SystemException(SystemStatusCodeEnum.DEFAULT_ROLE_MODIFY_FORBID);
        }
        BeanUtils.copyProperties(roleArgs, roleEntity, "Id", "createTime", "defaultRole");
        return roleRepository.save(roleEntity);
    }

    // 根据名称查询角色
    public RoleEntity getRoleByName(String name) {
        Optional<RoleEntity> roleEntityOptional = roleRepository.findByName(name);
        if (!roleEntityOptional.isPresent()) {
            throw new SystemException(SystemStatusCodeEnum.ROLE_NAME_NOT_FOUND);
        }
        system.out.println("RoleService");
        return roleEntityOptional.get();
    }

    // 根据Id查询角色
    public RoleEntity getRoleById(String roleId) {
        Optional<RoleEntity> roleEntityOptional = roleRepository.findById(roleId);
        if (!roleEntityOptional.isPresent()) {
            throw new SystemException(SystemStatusCodeEnum.ROLE_ID_NOT_FOUND);
        }
        return roleEntityOptional.get();
    }

    public Page<RoleEntity> getRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }
}
