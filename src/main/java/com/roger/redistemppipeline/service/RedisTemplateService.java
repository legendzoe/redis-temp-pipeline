package com.roger.redistemppipeline.service;

import com.roger.redistemppipeline.vo.OrderVo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisTemplateService {

    List<Object> excutePipelineSet(List<OrderVo> orderVoList);

    List<Boolean> checkDataInRedis(List<OrderVo> orderVoList);


    Set<String> getDataInRedis(List<OrderVo> orderVoList);
}
