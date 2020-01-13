package name.ealen.web;

import name.ealen.global.advice.log.LogNote;
import name.ealen.web.vo.Person;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import java.sql.SQLException;


/**
 * Created by EalenXie on 2018/9/7 14:24.
 */
@RestController
public class SayHelloController {


    @PostMapping("/sayHello")
    public ResponseEntity sayHello(@RequestBody Person o) {
        return ResponseEntity.ok(o);
    }

    /**
     * 异常测试
     */
    @GetMapping("/sayHelloHttpServerErrorException")
    public String sayHelloHttpServerErrorException() {
        throw new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT);
    }

    @GetMapping("/sayHelloStackOverflowError")
    public ResponseEntity sayHelloStackOverflowError() {
        throw new StackOverflowError();
    }

    @GetMapping("/sayHelloSqlException")
    public ResponseEntity sayHelloSqlException() throws SQLException {
        throw new SQLException();
    }

    @PostMapping("/sayHelloValidate")
    public ResponseEntity sayHelloValidate(@RequestBody @Validated Person o) {
        return new ResponseEntity<>(o, HttpStatus.OK);
    }

}
