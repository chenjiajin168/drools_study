package com.chenjiajin.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 * 动态加载项目外部的DRL规则
 */
public class DroolsHelper {

    public static KieSession loadForRule(String drlStr) {
        KieServices kieServices = KieServices.Factory.get();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write("src/main/resources/rules/" + drlStr.hashCode() + ".drl", drlStr);

        // 将KieFileSystem加入到KieBuilder
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        // 编译此时的builder中所有的规则
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
        }

        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        return kieContainer.newKieSession();
    }




    //public static KieBase loadForRule2(String classPath) {
    //    KieServices kieServices = KieServices.Factory.get();
    //    KieFileSystem kfs = kieServices.newKieFileSystem();
    //    Resource resource = kieServices.getResources().newClassPathResource(classPath);
    //    kfs.write(resource);
    //    KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();
    //    if (kieBuilder.getResults().getMessages(Message.Level.ERROR).size() > 0) {
    //        throw new RuntimeException();
    //    }
    //    KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
    //    KieBase kBase = kieContainer.getKieBase();
    //    return kBase;
    //}
}
