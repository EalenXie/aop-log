package name.ealen.demo.controller;

import name.ealen.demo.service.DemoService;
import name.ealen.log.Log4a;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author EalenXie Created on 2020/1/16 10:44.
 * 测试接口 记录异常到堆栈,记录请求参数,和响应参数
 * 进行各种测试 支持FORM, XML, JSON ,特殊对象,普通参数等记录
 */
@Log4a(type = "测试API", stackTrace = true)
@RestController
public class DemoController {
    @Resource
    private DemoService demoService;

    /**
     * JSON数据测试
     */
    @PostMapping("/sayHello")
    public ResponseEntity<?> sayHello(@RequestBody Map<String, Object> request) {
        demoService.sayHello(request);
        return ResponseEntity.ok(request);
    }

    /**
     * RequestParam 参数测试
     */
    @PostMapping("/params")
    public ResponseEntity<?> params(@RequestParam Integer a) {
        return ResponseEntity.ok(a);
    }

    /**
     * 无参测试
     */
    @GetMapping("/noArgs")
    public ResponseEntity<?> noArgs() {
        return ResponseEntity.ok().build();
    }

    /**
     * XML 格式数据测试
     * 也返回XML
     */
    @PostMapping(value = "/callXml", consumes = {MediaType.APPLICATION_XML_VALUE})
    public XmlDataDTO callXml(@RequestBody XmlDataDTO dataDTO) {
        return dataDTO;
    }

    /**
     * 特殊对象测试
     */
    @GetMapping("/callHttpServletRequest")
    public ResponseEntity<?> callHttpServletRequest(HttpServletRequest request) {
        return ResponseEntity.ok().build();
    }

}
