package com.zyq.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public int saveUserCount(Integer userId) {
        //根据不同用户id生成调用次数的key
        String limitKey = "Limit"+"_"+userId;
        //获取redis中指定key的调用次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = -1;
        if (limitNum == null) {  //当还没有被调用的时候      还没有下订单
            stringRedisTemplate.opsForValue().set(limitKey,"0",3600, TimeUnit.SECONDS);
        }else {
            limit = Integer.parseInt(limitNum)+1;  //下订单了；
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit; //返回调用的次数
    }

    @Override
    public boolean getUserCount(Integer userId) {
        String limitKey = "Limit"+"_"+userId;  //根据userid对应key 获取相应次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);  //获取redis中调用的次数
        if (limitNum == null) {
            System.out.println("用户没有抢购商品");
            return true;
        }
        return Integer.parseInt(limitNum)>10;  //false 代表超过
    }
}
