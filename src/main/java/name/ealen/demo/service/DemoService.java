package name.ealen.demo.service;

import lombok.extern.slf4j.Slf4j;
import name.ealen.log.LogData;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author EalenXie Created on 2020/1/16 10:49.
 */
@Service
@Slf4j
public class DemoService {
    /**
     * 测试方法, 使用LogData.step记录步骤
     */
    public void sayHello(Map<String, Object> words) {
        LogData.step("1. 请求来了,执行业务动作");
        log.info(String.valueOf(words));
        LogData.step("2. 业务动作执行完成");
    }
}
