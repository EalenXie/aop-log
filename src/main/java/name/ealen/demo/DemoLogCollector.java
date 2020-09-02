package name.ealen.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import name.ealen.log.LogData;
import name.ealen.log.collector.LogCollectException;
import name.ealen.log.collector.LogCollector;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
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
    private static final ObjectMapper mapper = new ObjectMapper();

    @Async
    @Override
    public void collect(LogData data) throws LogCollectException {
        try {
            File file = new File("D:\\home\\temp\\日志.txt");
            if (!file.getParentFile().exists()) {
                FileUtils.forceMkdir(file.getParentFile());
            }
            try (FileWriter fw = new FileWriter(file, true)) {
                fw.append(mapper.writeValueAsString(data));
            }
        } catch (IOException e) {
            throw new LogCollectException(e);
        }
    }
}
