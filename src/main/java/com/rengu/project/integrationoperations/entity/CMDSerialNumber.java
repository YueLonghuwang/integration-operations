package com.rengu.project.integrationoperations.entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * @author yaojiahao
 * @data 2019/4/30 13:18
 */

@Entity
public class CMDSerialNumber {
    @Id
    private String id = UUID.randomUUID().toString();
    private int serialNumber;

    public void setId(String id) {
        this.id = id;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }


    public String getId() {
        return id;
    }

    public int getSerialNumber() {
        return serialNumber;
    }
}
