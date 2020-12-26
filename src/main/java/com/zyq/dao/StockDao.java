package com.zyq.dao;

import com.zyq.entity.Stock;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockDao {
    //根据商品ID查询库存信息的方法
    public Stock checkStock(Integer id);

    public int updateSale(Stock stock);   //返回值变成int    返回的数和version 相对应把?
}
