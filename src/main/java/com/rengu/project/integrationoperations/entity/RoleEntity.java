package com.rengu.project.integrationoperations.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rengu.project.integrationoperations.enums.SystemRoleEnum;
import com.rengu.project.integrationoperations.util.BackEndFrameworkApplicationMessage;
import lombok.Data;
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

@Data
@NoArgsConstructor
@Entity
public class RoleEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
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
