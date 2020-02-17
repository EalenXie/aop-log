package name.ealen.log.collector;

import name.ealen.log.Log4;

/**
 * @author EalenXie Created on 2020/1/13 18:00.
 */
public class NothingCollector implements LogCollector {
    @Override
    public void collect(Log4 define) {
        //ig
        //this is a empty collector will do nothing
    }
}
