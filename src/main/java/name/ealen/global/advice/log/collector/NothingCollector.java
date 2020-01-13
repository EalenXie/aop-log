package name.ealen.global.advice.log.collector;

import name.ealen.global.advice.log.LogDefine;

/**
 * @author EalenXie Created on 2020/1/13 18:00.
 */
public class NothingCollector implements LogCollector {
    @Override
    public void collect(LogDefine define) {
        //ig
        //this is a nothing collector will do nothing
    }
}
