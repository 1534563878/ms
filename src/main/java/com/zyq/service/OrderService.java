package com.zyq.service;

public interface OrderService {
    int kill(Integer id ,Integer userid ,String md5);

    String getMd5(Integer id, Integer userid);
}
