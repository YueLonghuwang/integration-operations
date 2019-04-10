package com.rengu.project.integrationoperations.service;

import com.rengu.project.integrationoperations.entity.RoleEntity;
import com.rengu.project.integrationoperations.entity.UserEntity;
import com.rengu.project.integrationoperations.enums.SystemRoleEnum;
import com.rengu.project.integrationoperations.enums.SystemStatusCodeEnum;
import com.rengu.project.integrationoperations.exception.SystemException;
import com.rengu.project.integrationoperations.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 *
 * @author hanchangming
 * @date 2019-03-21
 */

@Slf4j
@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //  按用户名加载用户
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntityOptional = userRepository.findByUsername(username);
        if (!userEntityOptional.isPresent()) {
            System.out.println(username);
            throw new UsernameNotFoundException("该用户名不存在：" + username);
        }
        return userEntityOptional.get();
    }

    // 根据用户名判断用户是否存在
    public boolean hasUserByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }

    // 保存多个用户
    public List<UserEntity> saveUsers(List<UserEntity> userEntityList) {
        return userRepository.saveAll(userEntityList);
    }

    // 保存用户
    public UserEntity saveUser(UserEntity userEntity, RoleEntity... roleEntities) {
        if (hasUserByUsername(userEntity.getUsername())) {
            throw new SystemException(SystemStatusCodeEnum.USER_USERNAME_EXISTED);
        }
        userEntity.setPassword(new BCryptPasswordEncoder().encode(userEntity.getPassword()));
        userEntity.setRoles(new HashSet<>(Arrays.asList(roleEntities)));
        return userRepository.save(userEntity);
    }

    // 根据ID删除用户
    public void deleteUserById(String userId) {
        UserEntity userEntity = getUserById(userId);
        if (userEntity.isDefaultUser()) {
            throw new SystemException(SystemStatusCodeEnum.DEFAULT_USER_MODIFY_FORBID);
        }
        userRepository.delete(userEntity);
    }

    public UserEntity removeRolesById(String userId, RoleEntity roleEntity) {
        if (roleEntity.getName().equals(SystemRoleEnum.USER.getName())) {
            throw new SystemException(SystemStatusCodeEnum.DEFAULT_ROLE_MODIFY_FORBID);
        }
        UserEntity userEntity = getUserById(userId);
        userEntity.getRoles().remove(roleEntity);
        return userRepository.save(userEntity);
    }

    // 根据id修改用户信息
    public UserEntity updateUserById(String userId, UserEntity userArgs) {
        UserEntity userEntity = getUserById(userId);
        if (userEntity.isDefaultUser()) {
            throw new SystemException(SystemStatusCodeEnum.DEFAULT_USER_MODIFY_FORBID);
        }
        if (!userArgs.getUsername().equals(userEntity.getUsername()) && hasUserByUsername(userArgs.getUsername())) {
            throw new SystemException(SystemStatusCodeEnum.USER_USERNAME_EXISTED);
        }
        BeanUtils.copyProperties(userArgs, userEntity, "id", "createTime", "password", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "defaultUser", "roles");
        return userRepository.save(userEntity);
    }

    public UserEntity updateUserPasswordById(String userId, String password) {
        UserEntity userEntity = getUserById(userId);
        userEntity.setPassword(new BCryptPasswordEncoder().encode(password));
        return userRepository.save(userEntity);
    }

    public UserEntity addRolesById(String userId, RoleEntity roleEntity) {
        UserEntity userEntity = getUserById(userId);
        userEntity.getRoles().add(roleEntity);
        return userRepository.save(userEntity);
    }

    // 根据id查询用户
    public UserEntity getUserById(String userId) {
        Optional<UserEntity> userEntityOptional = userRepository.findById(userId);
        if (!userEntityOptional.isPresent()) {
            throw new SystemException(SystemStatusCodeEnum.USER_ID_NOT_FOUND);
        }
        return userEntityOptional.get();
    }

    public Page<UserEntity> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
