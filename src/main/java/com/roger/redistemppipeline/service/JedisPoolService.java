package com.roger.redistemppipeline.service;

import com.roger.redistemppipeline.vo.OrderVo;

import java.util.List;

public interface JedisPoolService {

    int saveDataAndRedis(List<OrderVo> orderVoList);

    List<Object> excutePipelineSet(List<OrderVo> orderVoList);
}
