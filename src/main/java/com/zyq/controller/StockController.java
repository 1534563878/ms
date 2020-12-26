package com.zyq.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.zyq.service.OrderService;
import com.zyq.service.OrderServiceImpl;
import com.zyq.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/stock")
public class StockController {
    Logger log = LoggerFactory.getLogger(StockController.class);
    //开发秒杀方法
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    //创建令牌桶实例
    private RateLimiter rateLimiter = RateLimiter.create(40);//每秒生成40个令牌

    @GetMapping("/md5")
    public String getMd5(Integer id ,Integer userid){
        String md5;
        try {
            md5 = orderService.getMd5(id,userid);
        }catch (Exception e){
            e.printStackTrace();
            return "获取Md5失败"+e.getMessage();
        }
        return "md5信息为"+md5;
    }
    //乐观锁防止超卖+ 令牌桶限流算法 +MD5接口隐藏 +单用户访问频率限制
    @GetMapping("/kill")
    public String kill(Integer id,Integer userid,String md5) {
        if (!rateLimiter.tryAcquire(2, TimeUnit.SECONDS)) {
            log.info("抛弃请求 抢购失败");
            return "当前请求被限流直接抛弃，无法调用后续秒杀逻辑";
        } else {
            try {
                //对用户访问限制
                int count = userService.saveUserCount(userid);
                log.info("用户一小时访问的次数",count);
                //进行调用次数判断
                boolean isBanned = userService.getUserCount(userid);
                if (isBanned){
                    log.info("购买失败 超过频率");
                    return "购买失败 超过频率";
                }
                //根据秒杀id 去调秒杀业务
                int orderId = orderService.kill(id,userid,md5);
                return "秒杀成功，订单ID为+" + String.valueOf(orderId); //String.valueOf(int i) : 将 int 变量 i 转换成字符串
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
    }
}
/*   @GetMapping("/sale")
    public String sale(Integer id){
//        rateLimiter.acquire();//获取令牌
        //2 设置超时时间  ，超过了就抛弃
        if (!rateLimiter.tryAcquire(5, TimeUnit.SECONDS)){
            System.out.println("当前请求被限流直接抛弃，无法调用后续秒杀逻辑");
            return "抢购失败";
        }else {
            System.out.println("处理业务");
            orderService.kill(1);
        }
        return "抢购";
    }*/
