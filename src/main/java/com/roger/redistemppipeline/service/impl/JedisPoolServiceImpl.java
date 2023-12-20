package com.roger.redistemppipeline.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.roger.redistemppipeline.mapper.OrderMapper;
import com.roger.redistemppipeline.service.JedisPoolService;
import com.roger.redistemppipeline.vo.OrderVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class JedisPoolServiceImpl implements JedisPoolService {

    @Resource
    JedisPool jedisPool;

    @Resource
    private OrderMapper orderMapper;

    @Override
    public int saveDataAndRedis(List<OrderVo> orderVoList) {
        return orderMapper.saveOrderByBatch(orderVoList);

    }

    @Override
    public List<Object> excutePipelineSet(List<OrderVo> orderVoList) {
        Instant start = Instant.now();
        Pipeline pipeline = jedisPool.getResource().pipelined();
        orderVoList.forEach(orderVo -> {
            List<String> orderList = JSONObject.parseArray(orderVo.getOrderIdList(), String.class);
            String[] array = orderList.toArray(new String[orderList.size()]);
//            orderList.forEach(orderId -> pipeline.set(orderVo.getUserId(), orderId));
            pipeline.sadd(orderVo.getUserId(), array);
        });

        pipeline.sync();
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();
        System.out.println("Jedis excutePipelineSet 方法共耗时： " + time + "毫秒");
        return null;
    }
}
