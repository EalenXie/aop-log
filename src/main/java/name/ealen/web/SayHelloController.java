package name.ealen.web;

import name.ealen.web.vo.Person;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;


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
        throw new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT);
//        throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
//        return "hello world";
    }

    @RequestMapping("/say")
    public ResponseEntity<?> say(@RequestBody Object o) {
//        throw new StackOverflowError();
        return new ResponseEntity<>(o, HttpStatus.OK);
    }

    @RequestMapping("/sayPerson")
    public ResponseEntity<?> sayHelloWithParams(@RequestBody @Validated Person o) {
        System.out.println("111");
//        throw new StackOverflowError();
        return new ResponseEntity<>(o, HttpStatus.OK);
    }

}
