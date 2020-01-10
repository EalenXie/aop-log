package name.ealen.global.advice.log.collector;

import name.ealen.global.advice.log.LogDefine;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author EalenXie Created on 2020/1/10 17:53.
 */
public class LogWriteToFileCollector implements LogCollector {
    @Override
    public void collect(LogDefine define) throws LogCollectException {
        try (FileWriter fw = new FileWriter("text.txt")) {
            fw.write(define.toString());
        } catch (IOException e) {
            throw new LogCollectException(e);
        }
    }
}
