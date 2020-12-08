package com.github;

import com.github.collector.LogCollectorExecutor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author EalenXie create on 2020/6/28 15:07
 * AopLog 切面处理器
 * LogData Aspect Processor
 */
@Component
public class AopLogProcessor {

    private final LogCollectorExecutor logCollectorExecutor;

    private final String appName;

    public AopLogProcessor(@Autowired LogCollectorExecutor logCollectorExecutor) {
        this.logCollectorExecutor = logCollectorExecutor;
        Environment environment = logCollectorExecutor.getApplicationContext().getEnvironment();
        String name = environment.getProperty("spring.application.name");
        if (name != null && name.length() > 0) {
            this.appName = name;
        } else {
            this.appName = logCollectorExecutor.getApplicationContext().getId();
        }
    }

    /**
     * 处理 日志数据切面
     *
     * @param data  日志数据
     * @param point 切入point对象
     * @return 返回执行结果
     * @throws Throwable Exceptions in AOP should be thrown out and left to the specific business to handle
     */
    public Object proceed(LogData data, ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        AopLog aopLog = signature.getMethod().getAnnotation(AopLog.class);
        if (aopLog == null) {
            aopLog = point.getTarget().getClass().getAnnotation(AopLog.class);
        }
        if (aopLog != null) {
            return proceed(aopLog, data, point);
        }
        return point.proceed();
    }


    /**
     * 方法执行处理记录
     *
     * @param aopLog 注解对象
     * @param data   日志数据
     * @param point  切入point对象
     * @return 返回执行结果
     * @throws Throwable Exceptions in AOP should be thrown out and left to the specific business to handle
     */
    private Object proceed(AopLog aopLog, LogData data, ProceedingJoinPoint point) throws Throwable {
        Object result = null;
        boolean success = false;
        try {
            result = point.proceed();
            success = true;
            return result;
        } catch (Throwable throwable) {
            if (aopLog.stackTraceOnErr()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    LogData.step("Fail : \n" + sw.toString());
                }
            }
            throw throwable;
        } finally {
            if  (!aopLog.logOnErr() || (aopLog.logOnErr() && !data.isSuccess())) {
                data.toCostTime();
                MethodSignature signature = (MethodSignature) point.getSignature();
                data.setAppName(appName);
                data.setType(aopLog.type());
                data.setMethod(signature.getDeclaringTypeName() + "#" + signature.getName());
                LogDataExtractor.logHttpRequest(data, aopLog.headers());
                if (aopLog.args()) {
                    data.setArgs(LogDataExtractor.getArgs(signature.getParameterNames(), point.getArgs()));
                }
                if (aopLog.respBody()) {
                    data.setRespBody(LogDataExtractor.getResult(result));
                }
                data.setSuccess(success);
                LogData.setCurrent(data);
                if (aopLog.asyncMode()) {
                    logCollectorExecutor.asyncExecute(aopLog.collector(), LogData.getCurrent());
                } else {
                    logCollectorExecutor.execute(aopLog.collector(), LogData.getCurrent());
                }
            }
        }
    }


}
