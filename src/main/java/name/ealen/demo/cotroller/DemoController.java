package name.ealen.demo.cotroller;

import name.ealen.demo.service.DemoService;
import name.ealen.log.Log4a;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author EalenXie Created on 2020/1/16 10:44.
 * 测试接口 记录异常到堆栈,记录请求参数,和响应参数
 */
@Log4a(type = "测试API", stackTrace = true,headers ={ HttpHeaders.ACCEPT_CHARSET,HttpHeaders.CONTENT_TYPE})
@RestController
public class DemoController {
    @Resource
    private DemoService demoService;
    @PostMapping("/sayHello")
    public ResponseEntity<?> sayHello(@RequestBody Map<String, Object> request) {
        demoService.sayHello(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/params")
    public ResponseEntity<?> params(@RequestParam Integer a){
        return ResponseEntity.ok(a);
    }
    @GetMapping("/noArgs")
    public ResponseEntity<?> noArgs(){
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/callXml", consumes = { MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<?> callXml(@RequestBody XmlDataDTO dataDTO){
        return ResponseEntity.ok(dataDTO);
    }



}
