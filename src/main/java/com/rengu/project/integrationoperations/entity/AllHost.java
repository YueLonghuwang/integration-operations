package com.rengu.project.integrationoperations.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * @author yaojiahao
 * @data 2019/4/29 19:14
 */
@Entity
@Data
public class AllHost {
    @Id
    private String id = UUID.randomUUID().toString();
    private String host;
}
