package com.github;

import com.github.collector.LogCollector;
import com.github.collector.NothingCollector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author EalenXie create on 2021/1/4 11:19
 */
@ComponentScan
@Configuration
public class AopLogAutoConfiguration {

    private static final Log log = LogFactory.getLog(AopLogAutoConfiguration.class);

    /**
     * 默认配置一个空的收集器
     *
     * @return By default, an empty collector is configured
     */
    @Bean
    @ConditionalOnMissingBean(value = LogCollector.class)
    public LogCollector nothingCollector() {
        return new NothingCollector();
    }


    /**
     * 默认配置异步收集器线程池
     *
     * @return The asynchronous collector thread pool is configured by default
     */
    @Bean
    @ConditionalOnMissingBean(name = "logCollectorAsyncExecutor")
    public Executor logCollectorAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix("logCollectorAsyncExecutor-");
        executor.setRejectedExecutionHandler((r, exec) -> log.error("LogCollectorAsyncExecutor thread queue is full,activeCount:" + exec.getActiveCount() +
                ",Subsequent collection tasks will be rejected,please check your LogCollector or config your Executor"));
        executor.initialize();
        return executor;
    }
}
