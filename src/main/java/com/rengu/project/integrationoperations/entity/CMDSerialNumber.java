package com.rengu.project.integrationoperations.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * @author yaojiahao
 * @data 2019/4/30 13:18
 */

@Entity
@Data
public class CMDSerialNumber {
    @Id
    private String id = UUID.randomUUID().toString();
    private int serialNumber;
}
