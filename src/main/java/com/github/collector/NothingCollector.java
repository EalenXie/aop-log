package com.github.collector;


import com.github.LogData;

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
