package name.ealen.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by EalenXie on 2018/9/7 14:24.
 */
@RestController
public class SayHelloController {


    /**
     * 异常测试
     */
    @RequestMapping("/sayHello")
    public String sayHello() {
//        throw new NullPointerException();
//        throw new StackOverflowError();
//        throw new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT);
//        throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        return "hello world";
    }

    @RequestMapping("/say")
    public ResponseEntity<?> say(@RequestBody Object o) {
        return new ResponseEntity<>(o, HttpStatus.OK);
    }


}
