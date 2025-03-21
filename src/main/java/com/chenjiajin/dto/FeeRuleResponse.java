package com.chenjiajin.dto;

import lombok.Data;

@Data
public class FeeRuleResponse {

    // 总金额
    private Double totalAmount;

    // 免费价格
    private Double freePrice;

    // 免费描述
    private String freeDescription;

    // 超出免费分钟的价格
    private Double exceedPrice;

    // 超出免费分钟描述
    private String exceedDescription;
}
