package name.ealen.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author EalenXie Created on 2020/1/2 17:52.
 * 全局异常日志 切面处理
 */
@Component
@Aspect
@Slf4j
public class Log4Aspect {


    @Resource
    private Log4aHandler log4aHandler;


    /**
     * 将会切 被Log4a注解标记的方法
     */
    @Pointcut("@annotation(Log4a) || @within(Log4a)")
    public void logNote() {
        //ig
    }

    @Around("logNote()")
    public Object noteMethod(ProceedingJoinPoint point) throws Throwable {
        return logger(point);
    }

    private Object logger(ProceedingJoinPoint point) throws Throwable {
        try {
            Log4.removeCurrent();
            Log4 log4 = Log4.getCurrent();
            return log4aHandler.proceed(log4, point);
        } finally {
            Log4.removeCurrent();
        }
    }

}
