package com.chenjiajin;

import com.chenjiajin.dto.FeeRuleRequest;
import com.chenjiajin.dto.FeeRuleResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 实战测试 FeeRule.drl
 */
@Slf4j
@SpringBootTest
public class DeeRuleTest {

    @Autowired
    private KieContainer kieContainer;

    @Test
    public void FeeRuleTest() {
        //1 开启会话
        KieSession kieSession = kieContainer.newKieSession();

        //2 创建传入数据对象 设置数据
        FeeRuleRequest feeRuleRequest = new FeeRuleRequest();
        //feeRuleRequest.setDurations(1);
        //feeRuleRequest.setDurations(5 + 5 * 60);
        feeRuleRequest.setDurations(5 + 25 * 60);


        //3 创建返回数据对象
        FeeRuleResponse feeRuleResponse = new FeeRuleResponse();
        kieSession.setGlobal("feeRuleResponse", feeRuleResponse);

        //4 对象传入到会话对象里面
        kieSession.insert(feeRuleRequest);

        //5 触发规则
        kieSession.fireAllRules();

        //6 中止会话
        kieSession.dispose();


        Double totalAmount = feeRuleResponse.getTotalAmount();
        System.err.println("totalAmount = " + totalAmount);

        Double freePrice = feeRuleResponse.getFreePrice();
        System.err.println("freePrice = " + freePrice);

        String freeDescription = feeRuleResponse.getFreeDescription();
        System.err.println("freeDescription = " + freeDescription);

        Double exceedPrice = feeRuleResponse.getExceedPrice();
        System.err.println("exceedPrice = " + exceedPrice);

        String exceedDescription = feeRuleResponse.getExceedDescription();
        System.err.println("exceedDescription = " + exceedDescription);
    }


}
