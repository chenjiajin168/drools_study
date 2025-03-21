package com.chenjiajin;

import com.chenjiajin.config.DroolsHelper;
import com.chenjiajin.config.DynamicRuleLoader;
import com.chenjiajin.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 动态加载DRL, 规则覆盖本地规则
 * 性能测试 + 优化
 */
@Slf4j
@SpringBootTest
public class DynamicRuleLoaderTest {

    @Autowired
    private KieContainer kieContainer;

    @Test  // 动态加载DRL, 规则覆盖本地规则
    public void test1() {

        //封装传入对象
        // 实体类设置数据
        Order order = new Order();
        order.setAmout(99);
        log.info("传入参数：{}", order);
        // 从KieContainer获取会话对象
        KieSession kieSession = kieContainer.newKieSession();
        // 设置订单对象
        kieSession.insert(order);
        // 触发规则
        kieSession.fireAllRules();
        // 中止会话
        kieSession.dispose();
        log.info("计算结果：{}", order);


        // 动态加载规则
        Order order2 = new Order();
        order2.setAmout(99);
        log.info("传入参数2：{}", order2);
        //获取最新订单费用规则
        KieSession kieSession2 = DynamicRuleLoader.loadForRule("import com.chenjiajin.dto.Order\n" +
                "\n" +
                "//规则一：100元以下 不加分\n" +
                "rule \"order_rule_1\"\n" +
                "    when\n" +
                "        $order:Order(amout < 100)\n" +
                "    then\n" +
                "        $order.setScore(0);\n" +
                "        System.err.println(\"成功匹配到新的规则一!!!：100元以下 不加分\");\n" +
                "end");
        kieSession2.insert(order2);
        kieSession2.fireAllRules();
        kieSession2.dispose();
        log.info("计算结果2：{}", order2);


    }

    @Test  // 性能测试 + 优化
    public void test2() {

        long startTime1 = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            Order order = new Order();
            order.setAmout(99);
            KieSession kieSession = kieContainer.newKieSession();
            kieSession.insert(order);
            kieSession.fireAllRules();
            kieSession.dispose();
        }
        System.err.println("本地规则 " + (System.currentTimeMillis() - startTime1) / 1000);
        // 10000000(一千万次) 21秒

        long startTime2 = System.currentTimeMillis();
        KieSession kieSession = kieContainer.newKieSession();
        for (int i = 0; i < 10000000; i++) {
            Order order2 = new Order();
            order2.setAmout(99);
            kieSession.insert(order2);
            kieSession.fireAllRules();
        }
        kieSession.dispose();
        System.err.println("本地规则+复用KieSession " + (System.currentTimeMillis() - startTime2) / 1000);
        // 10000000(一千万次) 16秒
        // 可以看到性能提升非常小, 且需要考虑KieSession线程不安全的问题









        long startTime3 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Order order3 = new Order();
            order3.setAmout(99);
            KieSession kieSession2 = DroolsHelper.loadForRule("import com.chenjiajin.dto.Order\n" +
                    "\n" +
                    "//规则一：100元以下 不加分\n" +
                    "rule \"order_rule_1\"\n" +
                    "    when\n" +
                    "        $order:Order(amout < 100)\n" +
                    "    then\n" +
                    "        $order.setScore(0);\n" +
                    "end");
            kieSession2.insert(order3);
            kieSession2.fireAllRules();
            kieSession2.dispose();
        }
        System.err.println("每次动态加载新的KieSession " + (System.currentTimeMillis() - startTime3) / 1000);
        // 1000 13秒 可以看到该方法的性能非常差!
        // 虽然实现了从数据库动态查询规则, 但是性能比固定的规则差了10000倍


        long startTime4 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Order order4 = new Order();
            order4.setAmout(99);
            KieSession kieSession2 = DynamicRuleLoader.loadForRule("import com.chenjiajin.dto.Order\n" +
                    "\n" +
                    "//规则一：100元以下 不加分\n" +
                    "rule \"order_rule_1\"\n" +
                    "    when\n" +
                    "        $order:Order(amout < 100)\n" +
                    "    then\n" +
                    "        $order.setScore(0);\n" +
                    "end");
            kieSession2.insert(order4);
            kieSession2.fireAllRules();
            kieSession2.dispose();
        }
        System.err.println("动态加载新的KieSession+判断是否新的规则才复用 " + (System.currentTimeMillis() - startTime4) / 1000);
        // 1000000(一百万次) 15秒
        // 可以看到性能提升了1000倍 但还是比固定的规则少10倍的速度



    }


}
