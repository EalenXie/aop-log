package com.github;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
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
public class LogDataAspect {

    @Resource
    private AopLogProcessor aopLogProcessor;

    /**
     * 将会切 被AopLog注解标记的方法
     */
    @Pointcut("@annotation(AopLog) || @within(AopLog)|| execution(* com.github.collector.*(..))")
    public void aopLogPointCut() {
        //ig
    }

    @Around("aopLogPointCut()")
    public Object note(ProceedingJoinPoint point) throws Throwable {
        return aopLog(point);
    }

    /**
     * @param point aop 切点对象
     * @return 返回执行结果
     * @throws Throwable Exceptions in AOP should be thrown out and left to the specific business to handle
     */
    private Object aopLog(ProceedingJoinPoint point) throws Throwable {
        try {
            LogData.removeCurrent();
            LogData data = LogData.getCurrent();
            return aopLogProcessor.proceed(data, point);
        } finally {
            LogData.removeCurrent();
        }
    }

}
