package com.zyq.service;

import com.zyq.dao.StockDao;
import com.zyq.dao.Stock_OrderDao;
import com.zyq.dao.UserDao;
import com.zyq.entity.Stock;
import com.zyq.entity.Stock_Order;
import com.zyq.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Transactional  //开启事务  避免脏读不可重复读 和幻读
@Slf4j
public class OrderServiceImpl implements OrderService {
    Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    @Autowired
     StockDao stockDao ;
    @Autowired
     Stock_OrderDao stock_orderDao ;
    @Autowired
    UserDao  userDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
        public int kill(Integer id, Integer userid, String md5 ) {

        if (!stringRedisTemplate.hasKey("kill"+id)){
            throw new RuntimeException("活动结束了");
        }
        //验证签名
        String hashKey = "key"+userid+"_"+id;
        String s = stringRedisTemplate.opsForValue().get(hashKey);
        if (s==null) throw new RuntimeException("没用携带验证签名");
        if (!s.equals(md5)) throw new RuntimeException("请求不合法");
        //根据ID 校验库存
        Stock stock = checkStock(id);
        //扣除库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }



    //生成MD5 加密文件   （隐藏接口）
    @Override
    public String getMd5(Integer id, Integer userid) {
        //检验用户的合法性
         User user = userDao.findById(id);
         if (user == null){
             throw new RuntimeException("没用查询到该用户");
         }
         log.info("用户信息",user.toString());

        //检验商品 的合法性
        Stock stock = stockDao.checkStock(id);
        if (stock == null) {
            throw new RuntimeException("没用查询到该商品");
        }
        log.info("商品信息",stock.toString());
        String hashKey = "key"+userid+"_"+id;
        //生成MD5
        String key = DigestUtils.md5DigestAsHex((userid+id+"!Q*jS#").getBytes());
        stringRedisTemplate.opsForValue().set(hashKey,key,3600, TimeUnit.SECONDS);
        log.info("Redis写入: [{}] [{}]" ,hashKey , key);
        return key;

    }


    // 校验库存
    public Stock checkStock(Integer id) {
        Stock stock = stockDao.checkStock(id);
        if (stock.getSale().equals(stock.getCount())) {   //    已售等于库存的时候就代表没货了
            throw new RuntimeException("库存不足");
        }
        return stock;
    }
    //扣除库存
    public void updateSale(Stock stock){
       // stock.setSale(stock.getSale()+1);  //  扣除库存就是已售加1   一般都是库存不动  已售变化
        //在sql文件完成销量的加1 和版本号的加1  并且根据商品id和版本号同时查询更新商品

        int updateRows = stockDao.updateSale(stock);

        if (updateRows == 0) {   //
            throw new RuntimeException("抢购失败");
        }
    }
    //创建订单
    public Integer createOrder(Stock stock){
        stockDao.updateSale(stock);
        Stock_Order order = new Stock_Order();   //创建了一个订单   然后获取订单的ID NAME , 和DATE
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setCreateDate(new Date());
        stock_orderDao.creatOrder(order);       //调用了创建订单的方法
        return order.getId();   //获得
    }
}
