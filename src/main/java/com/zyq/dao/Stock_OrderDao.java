package com.zyq.dao;

import com.zyq.entity.Stock_Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface Stock_OrderDao {
    public void creatOrder(Stock_Order order);
}
