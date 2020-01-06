package name.ealen.global.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author EalenXie Created on 2020/1/2 17:36.
 * 全局异常 处理器
 */
@Slf4j
@RestControllerAdvice
public class GloExceptionAdvice {


    /**
     * 异常级别
     */
    @ExceptionHandler(Exception.class)
    public Object exception(Exception e) {
        log.error("printStackTrace", e);
        return new Object();
    }

    /**
     * 错误级别
     */
    @ExceptionHandler(Error.class)
    public Object error(Error e) {
        log.error("printStackTrace", e);
        return new Object();
    }
}
