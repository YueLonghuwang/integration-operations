package com.rengu.project.integrationoperations.entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * 前端设备（1，2，3号设备）
 * @author yaojiahao
 * @data 2019/4/29 19:14
 */
@Entity
public class AllHost {
    @Id
    private String id = UUID.randomUUID().toString();
    private String host;
    private int num;

    public void setId(String id) {
        this.id = id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    public int getNum() {
        return num;
    }

}
