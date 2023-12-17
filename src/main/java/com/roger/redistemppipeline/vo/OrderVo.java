package com.roger.redistemppipeline.vo;

import lombok.Data;

@Data
public class OrderVo {

    private Long id;
    private String userId;
    private String orderIdList;


    public OrderVo(String userId, String orderIdList) {
        this.userId = userId;
        this.orderIdList = orderIdList;
    }

    public OrderVo() {
    }
}
