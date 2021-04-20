package com.github;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

/**
 * @author EalenXie on 2021/4/20 9:23
 * 执行器 使用收集器执行收集过程
 */
@Component
@EnableAsync
public class CollectorExecutor {

    /**
     * 异步 执行收集器
     *
     * @param collector 收集器
     * @param data      数据对象
     */
    @Async("collectorAsyncExecutor")
    public <D> void asyncExecute(Collector<D> collector, D data) {
        execute(collector, data);
    }

    /**
     * 同步 执行收集器
     *
     * @param collector 收集器
     * @param data      数据对象
     */
    public <D> void execute(Collector<D> collector, D data) {
        collector.collect(data);
    }
}
