package com.chenjiajin;

import com.chenjiajin.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 小测试 order.drl
 */
@Slf4j
@SpringBootTest
public class OrderTest {

    @Autowired
    private KieContainer kieContainer;

    @Test
    public void orderTest() {
        // 从KieContainer获取会话对象
        KieSession session = kieContainer.newKieSession();

        // 实体类设置数据
        Order order = new Order();
        order.setAmout(1500);

        // 通过会话对象，把order对象插入到工作内存里面
        session.insert(order);

        //激活规则
        session.fireAllRules();

        //关闭会话
        session.dispose();

        System.err.println("订单金额：" + order.getAmout() + " ,增加积分：" + order.getScore());
    }


}
