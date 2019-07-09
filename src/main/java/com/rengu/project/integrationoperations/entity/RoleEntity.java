package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.project.integrationoperations.enums.SystemRoleEnum;
import com.rengu.project.integrationoperations.util.BackEndFrameworkApplicationMessage;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author hanchangming
 * @date 2019-03-19
 */

@NoArgsConstructor
@Entity
public class RoleEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();

    public void setId(String id) {
        this.id = id;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDefaultRole(boolean defaultRole) {
        this.defaultRole = defaultRole;
    }

    public String getId() {
        return id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDefaultRole() {
        return defaultRole;
    }

    @NotBlank(message = BackEndFrameworkApplicationMessage.ROLE_NAME_NOT_BLANK)
    private String name;
    private String description;
    private boolean defaultRole = false;

    public RoleEntity(SystemRoleEnum systemRoleEnum) {
        this.name = systemRoleEnum.getName();
        this.description = systemRoleEnum.getDescription();
        this.defaultRole = true;
    }
}
