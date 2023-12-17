package com.roger.redistemppipeline.service;

import com.roger.redistemppipeline.vo.OrderVo;

import java.util.List;

public interface OrderService {

    int saveOrderByBatch(List<OrderVo> orderVoList);

    List<OrderVo> selectAllOrder();
}
