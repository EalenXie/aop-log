package com.github;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author EalenXie Created on 2020/1/2 17:52.
 * LogData Aspect
 */
@ComponentScan
@Component
@Aspect
@EnableAspectJAutoProxy(exposeProxy = true)
public final class LogDataAspect {

    @Resource
    private AopLogProcessor aopLogProcessor;

    /**
     * 将会切 被AopLog注解标记的方法
     */
    @Pointcut("@annotation(AopLog) || @within(AopLog)")
    public void aopLogPointCut() {
        //ig
    }

    @Around("aopLogPointCut()")
    public Object note(ProceedingJoinPoint point) throws Throwable {
        AopLogConfig config = new AopLogConfig();
        MethodSignature signature = (MethodSignature) point.getSignature();
        AopLog aopLog = signature.getMethod().getAnnotation(AopLog.class);
        if (aopLog == null) {
            aopLog = point.getTarget().getClass().getAnnotation(AopLog.class);
        }
        if (aopLog != null) {
            config.setLogOnErr(aopLog.logOnErr());
            config.setTag(aopLog.tag());
            config.setHeaders(aopLog.headers());
            config.setArgs(aopLog.args());
            config.setRespBody(aopLog.respBody());
            config.setStackTraceOnErr(aopLog.stackTraceOnErr());
            config.setAsyncMode(aopLog.asyncMode());
            config.setCollector(aopLog.collector());
        }
        return aopLogProcessor.proceed(config, point);
    }

}
