package io.github.log.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author EalenXie Created on 2020/1/14 10:24.
 */
@Slf4j
@EnableAsync
@Configuration
public class LogCollectorExecutorConfiguration implements AsyncConfigurer {


    /**
     * 默认配置一个空的收集器
     */
    @Bean
    @ConditionalOnMissingBean(value = LogCollector.class)
    public LogCollector nothingCollector() {
        return new NothingCollector();
    }


    /**
     * 配置 日志收集器异步线程池
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix("LogCollectorAsyncExecutor-");
        executor.setRejectedExecutionHandler((r, exec) -> log.error("LogCollectorAsyncExecutor thread queue is full,activeCount:{},Subsequent collection tasks will be rejected,please check your LogCollector or config your Executor",
                exec.getActiveCount()));
        executor.initialize();
        return executor;
    }


    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("LogCollectorExecutor execution Exception [method: {} ,params: {} ]", method, params, ex);
    }

}
