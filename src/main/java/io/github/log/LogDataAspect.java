package io.github.log;

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
public class LogDataAspect {


    @Resource
    private AopLogHandler aopLogHandler;


    /**
     * 将会切 被AopLog注解标记的方法
     */
    @Pointcut("@annotation(AopLog) || @within(AopLog)")
    public void aopLogPointCut() {
        //ig
    }

    @Around("aopLogPointCut()")
    public Object note(ProceedingJoinPoint point) throws Throwable {
        return aopLog(point);
    }

    private Object aopLog(ProceedingJoinPoint point) throws Throwable {
        try {
            LogData.removeCurrent();
            LogData data = LogData.getCurrent();
            return aopLogHandler.proceed(data, point);
        } finally {
            LogData.removeCurrent();
        }
    }

}
