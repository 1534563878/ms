package com.zyq.entity;

import java.util.Date;

public class Stock_Order {
    private Integer id;
    private Integer sid;
    private String  name;
    private Date    createDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSid() {
        return sid;
    }

    public void setSid(Integer sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Stock_Order(Integer id, Integer sid, String name, Date createDate) {
        this.id = id;
        this.sid = sid;
        this.name = name;
        this.createDate = createDate;
    }

    public Stock_Order() {
    }
}
