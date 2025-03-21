package com.chenjiajin;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 小测试 demo.drl
 */
@Slf4j
@SpringBootTest
public class DemoTest {

    @Autowired
    private KieContainer kieContainer;


    @Test
    public void demoRuleTest() {
        // 开启会话
        KieSession kieSession = kieContainer.newKieSession();
        // 触发规则
        kieSession.fireAllRules();
        // 中止会话
        kieSession.dispose();
    }


}
