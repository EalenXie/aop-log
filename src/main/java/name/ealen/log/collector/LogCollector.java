package name.ealen.log.collector;

import name.ealen.log.LogDefine;

/**
 * @author EalenXie Created on 2020/1/7 9:12.
 */
public interface LogCollector {
    void collect(LogDefine define) throws LogCollectException;
}
