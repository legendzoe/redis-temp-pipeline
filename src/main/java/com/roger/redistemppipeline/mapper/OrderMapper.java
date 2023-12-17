package com.roger.redistemppipeline.mapper;

import com.roger.redistemppipeline.vo.OrderVo;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Mapper
public interface OrderMapper {

    int saveOrderByBatch(List<OrderVo> orderVoList);

    List<OrderVo> selectAllOrder();
}
