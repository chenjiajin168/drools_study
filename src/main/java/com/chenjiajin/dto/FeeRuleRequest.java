package com.chenjiajin.dto;

import lombok.Data;

@Data
public class FeeRuleRequest {

    // 借用时长 单位分钟
    private Integer durations;

    // 超出免费时长的小时数
    private Integer exceedHours;
}
