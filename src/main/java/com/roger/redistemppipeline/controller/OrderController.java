package com.roger.redistemppipeline.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.roger.redistemppipeline.service.OrderService;
import com.roger.redistemppipeline.service.RedisTemplateService;
import com.roger.redistemppipeline.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/order/")
public class OrderController {

    @Autowired
    OrderService orderService;
    @Autowired
    RedisTemplateService redisTemplateService;


    @GetMapping("/saveOrderByBatch")
    @Transactional
    public int saveOrderByBatch() {
        //组装order数据，初始化map
        Map<String, List<String>> userOrderMap = init();
        List<OrderVo> orderVoList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(userOrderMap)) {
            userOrderMap.forEach((k, v) -> {
                OrderVo orderVo = new OrderVo(k , JSON.toJSONString(v));
                orderVoList.add(orderVo);
            });
        }

        Integer saveId = orderService.saveOrderByBatch(orderVoList);

        //同时保存到redis，使用pipeline管道
        List<Object> results = redisTemplateService.excutePipelineSet(orderVoList);

        return saveId;
    }

    /**
     * 找出两者不一样的数据，(将数据放到redis里面找，通过命令sysmember来找速度快)
     * @return
     */
    @GetMapping("/checkAndCompare")
    public String checkAndCompare() {
        //找寻DB
        List<OrderVo> orderVoList = selectAllOrder();
//        将数据带入redis中进行排查查找，找出与redis不一样的数据
        List<Boolean> result = redisTemplateService.checkDataInRedis(orderVoList);
        //尝试定位到用户名以及对应的订单号
        Map<String, List<String>> orderUserMap = new HashMap<>();
        orderVoList.forEach(orderVo -> {
            orderUserMap.put(orderVo.getUserId(), JSONArray.parseArray(orderVo.getOrderIdList(), String.class));
        });
        for (int i = 0; i < result.size(); i++) {
            if (!result.get(i)) {// 9561,9578,9582,数据不对的下标
                result.get(i);
                int count = 0;
                for (int j = 0; j < orderVoList.size(); j++) {
                    List<String> stringList = JSONArray.parseArray(orderVoList.get(j).getOrderIdList(), String.class);
                    count += stringList.size();
                    //第一次进来时候不需要加1，若数量大于等于报错的i，就已经定位出问题了
                    if (stringList.size() >= i) {
                        System.out.println("redis中没有找到从第三方返回的用户订单号： " + stringList.get(i) + ", 用户ID： " + orderVoList.get(j).getUserId());
                    } else if (count > i) {
                        System.out.println("找到你了");
                        Collections.reverse(stringList);
                        System.out.println("redis中没有找到从第三方返回的用户订单号： " + stringList.get(count - i - 1) + ", 用户ID： " + orderVoList.get(j).getUserId());
                        break;
                    }
                }

            }
        }
        return null;
    }


    @GetMapping("/getDataInRedis")
    public String getDataInRedis() {

        List<OrderVo> orderVoList = selectAllOrder();
        Set<String> set = redisTemplateService.getDataInRedis(orderVoList);
        if (set.size() > 0) {
            System.out.println("nice");
        }
        return null;
    }



    @GetMapping("/selectAllOrder")
    public List<OrderVo> selectAllOrder() {
        List<OrderVo> orderVoList = orderService.selectAllOrder();
        if (!CollectionUtils.isEmpty(orderVoList)) {
            return orderVoList;
        }
        return new ArrayList<>();
    }

    /**
     * 初始化map数据,k:用户ID，v:其对应的orderId,JSON格式
     * @return
     */
    private Map<String, List<String>> init() {
        Map<String, List<String>> userOrderMap = new HashMap<>();
        //模拟假设在5分钟内有500个用户
        for (int i = 0; i < 20; i++) {
            userOrderMap.put("user_" + i, createOrderList());
        }
        return userOrderMap;
    }

    public List<String> createOrderList() {
        //随机数
        int randonNo = (int)(1+Math.random() * 10);
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
