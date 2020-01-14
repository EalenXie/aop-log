package name.ealen.global.config;

import name.ealen.log.LogDefine;
import name.ealen.log.collector.LogCollectException;
import name.ealen.log.collector.LogCollector;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author EalenXie Created on 2020/1/10 17:53.
 */
//@Component
public class LogWriteToFileCollector implements LogCollector {
    @Override
    public void collect(LogDefine define) throws LogCollectException {
        try (FileWriter fw = new FileWriter("D:\\home\\temp\\日志.txt", true)) {
            fw.append(define.toString());
        } catch (IOException e) {
            throw new LogCollectException(e);
        }
    }
}
