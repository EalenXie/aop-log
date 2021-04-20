package com.github;

import com.github.collector.LogCollector;
import com.github.collector.NothingCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AopLogAutoConfiguration.class);

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
    @ConditionalOnMissingBean(name = "collectorAsyncExecutor")
    public Executor logCollectorAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix("collectorAsyncExecutor-");
        executor.setRejectedExecutionHandler((r, exec) -> log.error("collectorAsyncExecutor thread queue is full,activeCount:{},Subsequent collection tasks will be rejected,please check your LogCollector or config your Executor", exec.getActiveCount()));
        executor.initialize();
        return executor;
    }
}
