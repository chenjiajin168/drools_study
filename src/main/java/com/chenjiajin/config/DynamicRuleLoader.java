package com.chenjiajin.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态加载DRL字符串生成KieSession，但每次调用都会重新编译，性能受影响。
 * 1. 计算DRL字符串的哈希值（如SHA-256），作为缓存键。
 * 2. 检查缓存中是否存在该键对应的KieContainer。
 * 3. 如果存在，直接使用该KieContainer创建KieSession。
 * 4. 如果不存在，执行编译流程，生成KieContainer，并存入缓存。
 * 5. 处理并发情况，确保同一时间只有一个线程编译同一DRL。
 * 6. 管理缓存的生命周期，避免内存泄漏。
 *
 * 总结，改进步骤：
 * 1. 为每个DRL字符串计算唯一键（如SHA-256）。
 * 2. 使用ConcurrentHashMap缓存KieContainer，键为唯一键。
 * 3. 在加载规则时，先检查缓存，存在则直接获取KieContainer，否则编译并缓存。
 * 4. 编译时，文件名使用唯一键，避免冲突（虽然此时文件名可能不再关键，因为缓存不依赖路径）。
 * 注意：文件名是否影响编译结果？比如，两个相同内容但不同文件名的DRL是否会被视为不同规则？在Drools中，文件名通常不影响规则的处理，只要内容相同，规则应该相同。但为了保险起见，使用基于内容的哈希作为文件名，可以保证相同内容生成相同的资源路径，可能帮助Drools内部优化，但主要依赖应用层缓存。
 *
 * 因此，最终代码修改为：
 * - 添加缓存ConcurrentHashMap<String, KieContainer>
 * - 使用computeIfAbsent来原子性地编译或获取缓存
 * - 文件名使用SHA-256哈希
 * - 处理可能的异常，如编译错误
 *
 * 此外，需要考虑KieContainer的生命周期管理，比如在不需要时如何从缓存中移除，避免内存泄漏。但在简单场景中，可能不考虑，或添加定时清理机制。
 * 最后，测试缓存是否有效：多次调用loadForRule传入相同的DRL字符串，观察是否只有第一次编译，后续直接使用缓存。
 */
public class DynamicRuleLoader {
    // 缓存：键为DRL内容的SHA-256哈希，值为编译后的KieContainer
    private static final ConcurrentHashMap<String, KieContainer> CONTAINER_CACHE = new ConcurrentHashMap<>();

    public static KieSession loadForRule(String drlStr) {
        String contentHash = calculateSHA256(drlStr); // 计算唯一哈希
        KieContainer kieContainer = CONTAINER_CACHE.computeIfAbsent(contentHash, key -> {
            // 若缓存未命中，执行编译
            return compileRule(drlStr, key);
        });
        return kieContainer.newKieSession();
    }

    // 编译DRL并生成KieContainer
    private static KieContainer compileRule(String drlStr, String hashKey) {
        KieServices kieServices = KieServices.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        // 使用哈希值作为文件名，确保相同内容路径一致
        String resourcePath = "src/main/resources/rules/" + hashKey + ".drl";
        kieFileSystem.write(resourcePath, drlStr);

        // 编译规则
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("规则编译错误: " + results.getMessages());
        }

        // 生成KieContainer并返回
        return kieServices.newKieContainer(
                kieServices.getRepository().getDefaultReleaseId()
        );
    }

    // 计算字符串的SHA-256哈希
    private static String calculateSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算哈希失败", e);
        }
    }
}