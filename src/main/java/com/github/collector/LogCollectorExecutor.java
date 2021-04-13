package com.github.collector;

import com.github.LogData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author EalenXie create on 2020/9/3 13:29
 * Log collection executor
 */
@Component
@EnableAsync
@ComponentScan
public class LogCollectorExecutor {

    private final LogCollector logCollector;

    private final Map<Class<? extends LogCollector>, LogCollector> collectors = new HashMap<>();

    private final ApplicationContext applicationContext;

    public LogCollectorExecutor(@Autowired ApplicationContext applicationContext, @Autowired LogCollector logCollector) {
        this.applicationContext = applicationContext;
        this.logCollector = logCollector;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * AsyncMode log collection 异步模式日志收集
     *
     * @param clz     日志收集器Class对象
     * @param logData 日志数据
     */
    @Async("logCollectorAsyncExecutor")
    public void asyncExecute(Class<? extends LogCollector> clz, LogData logData) {
        execute(clz, logData);
    }

    /**
     * 同步模式收集日志
     *
     * @param clz     日志收集器Class对象
     * @param logData 日志数据
     */
    public void execute(Class<? extends LogCollector> clz, LogData logData) {
        getExecuteLogCollector(clz).collect(logData);
    }

    /**
     * Get the specified log collector 获取指定的日志收集器
     *
     * @param clz 日志收集器Class对象
     * @return 获取指定的日志收集器
     */
    private LogCollector getExecuteLogCollector(Class<? extends LogCollector> clz) {
        if (clz != null && clz != NothingCollector.class) {
            LogCollector c;
            try {
                c = applicationContext.getBean(clz);
            } catch (Exception e) {
                c = collectors.get(clz);
                if (c == null) {
                    c = BeanUtils.instantiateClass(clz);
                    collectors.put(clz, c);
                }
            }
            return c;
        } else {
            return logCollector;
        }
    }

}
