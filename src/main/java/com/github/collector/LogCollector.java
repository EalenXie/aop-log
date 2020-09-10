package com.github.collector;


import com.github.LogData;

/**
 * @author EalenXie Created on 2020/1/7 9:12.
 * 日志收集器
 */
@FunctionalInterface
public interface LogCollector {
    /**
     * 日志收集
     *
     * @param data 日志数据对象
     */
    void collect(LogData data);
}
