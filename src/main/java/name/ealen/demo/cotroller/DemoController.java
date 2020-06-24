package name.ealen.demo.cotroller;

import name.ealen.demo.service.DemoService;
import name.ealen.log.Log4a;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author EalenXie Created on 2020/1/16 10:44.
 * 测试接口 记录异常到堆栈,记录请求参数,和响应参数
 */
@Log4a(type = "测试API", stackTrace = true)
@RestController
public class DemoController {
    @Resource
    private DemoService demoService;
    @PostMapping("/sayHello")
    public ResponseEntity<?> sayHello(@RequestBody Map<String, Object> request) {
        demoService.sayHello(request);
        return ResponseEntity.ok(request);
    }
}
