package name.ealen.web;

import name.ealen.web.vo.Person;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import java.sql.SQLException;


/**
 * Created by EalenXie on 2018/9/7 14:24.
 */
@RestController
public class SayHelloController {


    @RequestMapping("/sayHello")
    public ResponseEntity<?> sayHello(@RequestBody Person o) {
        return ResponseEntity.ok(o);
    }

    /**
     * 异常测试
     */
    @RequestMapping("/sayHelloHttpServerErrorException")
    public String sayHelloHttpServerErrorException() {
        throw new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT);
    }

    @RequestMapping("/sayHelloStackOverflowError")
    public ResponseEntity<?> sayHelloStackOverflowError() {
        throw new StackOverflowError();
    }

    @RequestMapping("/sayHelloSqlException")
    public ResponseEntity<?> sayHelloSqlException() throws SQLException {
        throw new SQLException();
    }

    @RequestMapping("/sayHelloValidate")
    public ResponseEntity<?> sayHelloValidate(@RequestBody @Validated Person o) {
        return new ResponseEntity<>(o, HttpStatus.OK);
    }

}
