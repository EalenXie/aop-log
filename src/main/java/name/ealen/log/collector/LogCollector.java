package name.ealen.log.collector;

import name.ealen.log.LogDefine;

/**
 * @author EalenXie Created on 2020/1/7 9:12.
 * 日志收集器
 */
public interface LogCollector {
    /**
     * 日志收集
     *
     * @param define 日志定义
     * @throws LogCollectException 收集异常时将会抛出日志收集异常
     */
    void collect(LogDefine define) throws LogCollectException;
}
