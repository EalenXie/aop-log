package name.ealen.demo;

import name.ealen.log.Log4;
import name.ealen.log.collector.LogCollectException;
import name.ealen.log.collector.LogCollector;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author EalenXie Created on 2020/1/10 17:53.
 * 配置一个全局的日志收集器
 * 这里只是做一个简单示例进行说明,请勿使用该收集方法到生产中
 * 本例直接将日志append到文件中
 */
@Component
public class DemoLogCollector implements LogCollector {
    @Override
    public void collect(Log4 log4) throws LogCollectException {
        try (FileWriter fw = new FileWriter("D:\\home\\temp\\日志.txt", true)) {
            fw.append(log4.toString());
        } catch (IOException e) {
            throw new LogCollectException(e);
        }
    }
}
