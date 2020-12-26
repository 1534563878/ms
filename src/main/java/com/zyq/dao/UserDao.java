package com.zyq.dao;

import com.zyq.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao {

    User findById(Integer id);
}
