package com.roger.redistemppipeline.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.roger.redistemppipeline.service.RedisTemplateService;
import com.roger.redistemppipeline.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTemplateServiceImpl implements RedisTemplateService {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Object> excutePipelineSet(List<OrderVo> orderVoList) {
        Instant start = Instant.now();
        List<Object> resultList = redisTemplate.executePipelined(new SessionCallback<List<OrderVo>>() {
            @Override
            public <K, V> List<OrderVo> execute(RedisOperations<K, V> operations) throws DataAccessException {
                SetOperations<String, String> listValueOperations = (SetOperations<String, String>) operations.opsForSet();
                orderVoList.forEach(orderVo -> {

                    List<String> orderList = JSONObject.parseArray(orderVo.getOrderIdList(), String.class);
                    orderList.forEach(orderId -> {
//                        String[] arrays = orderList.toArray(new String[orderList.size()]);

                        listValueOperations.add(orderVo.getUserId(), orderId);
                    });
                    operations.expire((K) orderVo.getUserId(), 6000L, TimeUnit.SECONDS);
                });
                return null;
            }
        });
        //获取redis的返回值
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();
        System.out.println("redisTemplate 方法共耗时： " + time + "毫秒");
        return resultList;
    }

    /**
     * 将数据带入到redis中通过命令进行查找,找出不一致的数据
     */
    /*@Override
    public List<Object> checkDataInRedis(List<OrderVo> orderVoList) {
        List<Object> objectList = redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                SetOperations<String, String> listValueOperations = (SetOperations<String, String>) operations.opsForSet();
                orderVoList.forEach(orderVo -> {
                    List<String> orderList = JSONObject.parseArray(orderVo.getOrderIdList(), String.class);
                    //尝试转换为array来进行处理
                    String[] strArray = orderList.toArray(new String[orderList.size()]);
                    listValueOperations.isMember(orderVo.getOrderIdList(), strArray);

                });
                return null;
            }
        });
        return objectList;
    }*/

    /*@Override
    public List<Object> checkDataInRedis(List<OrderVo> orderVoList) {
        List<Object> objectList = redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                SetOperations<String, String> listValueOperations = (SetOperations<String, String>) operations.opsForSet();
                orderVoList.forEach(orderVo -> {
                    List<String> orderList = JSONObject.parseArray(orderVo.getOrderIdList(), String.class);
                    //尝试转换为array来进行处理
                    orderList.forEach(order -> {

                        listValueOperations.isMember(orderVo.getUserId(), order);
                    });
                    *//*String[] strArray = orderList.toArray(new String[orderList.size()]);
                    listValueOperations.isMember(orderVo.getOrderIdList(), strArray);*//*

                });
                return null;
            }
        });
        return objectList;
    }*/
    public List<Boolean> checkDataInRedis(List<OrderVo> orderVoList) {
        List<Object> objectList = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                orderVoList.forEach(orderVo -> {
                    List<String> orderList = JSONObject.parseArray(orderVo.getOrderIdList(), String.class);
                    orderList.forEach(order -> {

                        connection.commands().sIsMember(keySerializer.serialize(orderVo.getUserId()), valueSerializer.serialize(order));
                    });
                });
                return null;
            }
        });
        List<Boolean> booleanList = Obj2List(objectList);

        return booleanList;
    }



    @Override
    public Set<String> getDataInRedis(List<OrderVo> orderVoList) {
        List<Object> objectList = redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                SetOperations<String, String> listValueOperations = (SetOperations<String, String>) operations.opsForSet();
                orderVoList.forEach(orderVo -> {
                    listValueOperations.members(orderVo.getUserId());
                });

                return null;
            }
        });
        Set<String> set = Collections.singleton(Obj2List(objectList).toString());

        return set;
    }

    private <T> List<T> Obj2List(List<Object> objList) {
        Object obj = objList;
        List list = Arrays.asList(obj);
        if (list.size() == 1) {
            List<T> finalList = (List<T>) list.get(0);
            return finalList;
        }
        return list;
    }
}
