package com.roger.redistemppipeline.controller;

import com.alibaba.fastjson2.JSON;
import com.roger.redistemppipeline.service.JedisPoolService;
import com.roger.redistemppipeline.service.OrderService;
import com.roger.redistemppipeline.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/jedis")
public class JedisTestController {

    @Autowired
    JedisPool jedis;
    @Autowired
    OrderService orderService;
    @Autowired
    JedisPoolService jedisPoolService;

    @GetMapping("/saveOrderByBatch")
    @Transactional
    public int saveOrderByBatch() {
        //获取当前时间
        Instant start = Instant.now();
        //组装order数据，初始化map
        Map<String, List<String>> userOrderMap = init();
        List<OrderVo> orderVoList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(userOrderMap)) {
            userOrderMap.forEach((k, v) -> {
                OrderVo orderVo = new OrderVo(k , JSON.toJSONString(v));
                orderVoList.add(orderVo);
            });
        }

//        Integer saveId = orderService.saveOrderByBatch(orderVoList);

        //同时保存到redis，使用pipeline管道
//        List<Object> results = redisTemplateService.excutePipelineSet(orderVoList);
        jedisPoolService.excutePipelineSet(orderVoList);

        //获取结束时间
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();
        System.out.println("使用redisTemplate 执行保存方法共耗时： " + time + "毫秒");
        return 1;
    }

    /**
     * 初始化map数据,k:用户ID，v:其对应的orderId,JSON格式
     * @return
     */
    private Map<String, List<String>> init() {
        Map<String, List<String>> userOrderMap = new HashMap<>();
        //模拟假设在5分钟内有500个用户
        for (int i = 0; i < 500; i++) {
            userOrderMap.put("user_" + i, createOrderList());
        }
        return userOrderMap;
    }


    public List<String> createOrderList() {
        //随机数
        int randonNo = (int)(1+Math.random() * 2000);
        List<String> orderList = new ArrayList<>();
        for (int i = 0; i < randonNo; i++) {
            orderList.add(OrderIdSimulate());
        }
        return orderList;
    }

    /**
     * 模拟创建订单数据
     * @return
     */
    public String OrderIdSimulate() {
        String replaceUUID = UUID.randomUUID().toString().replace("-", "");
        return replaceUUID;
    }
}
