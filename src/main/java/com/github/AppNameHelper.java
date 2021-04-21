package com.github;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * @author EalenXie on 2021/4/20 17:12
 */
public class AppNameHelper {

    private AppNameHelper() {

    }

    /**
     * 设置应用名称
     */
    public static String getAppNameByApplicationContext(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String name = environment.getProperty("spring.application.name");
        if (name != null) {
            return name;
        }
        if (applicationContext.getId() != null) {
            return applicationContext.getId();
        }
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if ("main".equals(stackTraceElement.getMethodName())) {
                return stackTraceElement.getFileName();
            }
        }
        return applicationContext.getApplicationName();
    }

}
