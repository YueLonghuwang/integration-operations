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
 * @author hanchangming
 * @date 2019-03-19
 */

@Entity
@Data
public class UserEntity implements UserDetails {

	@Id
	private String id = UUID.randomUUID().toString();
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime = new Date();
	@NotBlank(message = BackEndFrameworkApplicationMessage.USER_USERNAME_NOT_BLANK)
	private String username;
	@NotBlank(message = BackEndFrameworkApplicationMessage.USER_PASSWORD_NOT_BLANK)
	private String password;

	public void setId(String id) {
		this.id = id;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setDefaultUser(boolean defaultUser) {
		this.defaultUser = defaultUser;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setRoles(Set<RoleEntity> roles) {
		this.roles = roles;
	}

	private boolean accountNonExpired = true;

	public String getId() {
		return id;
	}

	public Date getCreateTime() {
		return createTime;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public boolean isDefaultUser() {
		return defaultUser;
	}

	public String getTel() {
		return tel;
	}

	public String getRealName() {
		return realName;
	}

	public Set<RoleEntity> getRoles() {
		return roles;
	}

	private boolean accountNonLocked = true;
	private boolean credentialsNonExpired = true;
	private boolean enabled = true;
	private boolean defaultUser = false;
	private String tel;
	private String realName;

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

	public UserEntity() {

	}
}
