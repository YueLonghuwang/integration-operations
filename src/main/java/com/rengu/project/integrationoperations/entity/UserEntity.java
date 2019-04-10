package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.project.integrationoperations.enums.SystemUserEnum;
import com.rengu.project.integrationoperations.util.BackEndFrameworkApplicationMessage;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 *
 * @author hanchangming
 * @date 2019-03-19
 */

@Data
@NoArgsConstructor
@Entity
public class UserEntity implements UserDetails {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @NotBlank(message = BackEndFrameworkApplicationMessage.USER_USERNAME_NOT_BLANK)
    private String username;
    @NotBlank(message = BackEndFrameworkApplicationMessage.USER_PASSWORD_NOT_BLANK)
    private String password;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;
    private boolean defaultUser = false;


    @ManyToMany(fetch = FetchType.EAGER)
    private Set<RoleEntity> roles;

    public UserEntity(SystemUserEnum systemUserEnum) {
        this.username = systemUserEnum.getUsername();
        this.password = new BCryptPasswordEncoder().encode(systemUserEnum.getPassword());
        this.defaultUser = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (RoleEntity roleEntity : roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + roleEntity.getName()));
        }
        return grantedAuthorities;
    }
    @Override
    public String toString() {
        return this.username;
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }
}
