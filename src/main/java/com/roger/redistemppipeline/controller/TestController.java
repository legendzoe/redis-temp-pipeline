package com.roger.redistemppipeline.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class TestController {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/hi")
    public String test() {
        redisTemplate.opsForValue().set("test", "hello word");
        Object test1 = redisTemplate.opsForValue().get("test");
        return test1.toString();
    }

    @GetMapping("/pipeline")
    public String pipeline() {
        Map<String, List<String>> userDataMap = new HashMap<>();

        for (int i = 0; i < 30000; i++) {
            userDataMap.put("user_" + i, createOrderList());
        }
        redisTemplate.executePipelined(new SessionCallback<Object>() {


            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                ValueOperations<String, String> valueOperations = (ValueOperations<String, String>) operations.opsForValue();
                userDataMap.forEach((k, v) -> {
                    v.stream().collect(Collectors.joining(","));
                    valueOperations.set(k, v.stream().collect(Collectors.joining(",")));
                });
                return null;
            }
        });
//        System.out.println(userDataMap);
        return userDataMap.toString();
    }



    public static String getUUID() {
        String replaceUUID = UUID.randomUUID().toString().replace("-", "");
        return replaceUUID;
    }

    public List<String> createOrderList() {
        //随机数
        int randonNo = (int)(1+Math.random() * 5000);
        List<String> orderList = new ArrayList<>();
        for (int i = 0; i < randonNo; i++) {
            orderList.add(getUUID());
        }
        return orderList;
    }
}
