package name.ealen.demo.service;

import name.ealen.log.LogDefine;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author EalenXie Created on 2020/1/16 10:49.
 */
@Service
public class DemoService {


    /**
     * 测试方法, 使用LogDefine.logger记录步骤
     */
    public void sayHello(Map<String, Object> words) {
        LogDefine.logger("1. 请求来了,执行业务动作");
        System.out.println(words);
        LogDefine.logger("2. 业务动作执行完成");
    }

}
