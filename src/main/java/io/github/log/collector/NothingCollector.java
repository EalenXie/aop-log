package io.github.log.collector;


import io.github.log.LogData;

/**
 * @author EalenXie Created on 2020/1/13 18:00.
 */
public class NothingCollector implements LogCollector {
    @Override
    public void collect(LogData data) {
        //ig
        //this is a empty collector will do nothing
    }
}
