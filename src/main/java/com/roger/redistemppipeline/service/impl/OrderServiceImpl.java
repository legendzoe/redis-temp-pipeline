package com.roger.redistemppipeline.service.impl;

import com.roger.redistemppipeline.mapper.OrderMapper;
import com.roger.redistemppipeline.service.OrderService;
import com.roger.redistemppipeline.vo.OrderVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Override
    public int saveOrderByBatch(List<OrderVo> orderVoList) {
        return orderMapper.saveOrderByBatch(orderVoList);
    }

    @Override
    public List<OrderVo> selectAllOrder() {
        return orderMapper.selectAllOrder();
    }
}
