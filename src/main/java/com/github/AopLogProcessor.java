package com.github;

import com.github.collector.LogCollector;
import com.github.collector.NothingCollector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author EalenXie create on 2020/6/28 15:07
 * AopLog 切面处理器
 * LogData Aspect Processor
 */
@Component
public class AopLogProcessor {

    private final ApplicationContext applicationContext;
    private final CollectorExecutor collectorExecutor;
    private final LogCollector logCollector;
    private final Map<Class<? extends LogCollector>, LogCollector> collectors = new HashMap<>();
    private final String appName;

    public AopLogProcessor(@Autowired ApplicationContext applicationContext,
                           @Autowired CollectorExecutor collectorExecutor,
                           @Autowired LogCollector logCollector) {
        this.applicationContext = applicationContext;
        this.collectorExecutor = collectorExecutor;
        this.logCollector = logCollector;
        this.appName = AppNameHelper.getAppNameByApplicationContext(applicationContext);
    }


    public String getAppName() {
        return appName;
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
     * 选择一个收集器进行执行
     */
    private LogCollector selectLogCollector(Class<? extends LogCollector> clz) {
        if (clz == NothingCollector.class) {
            return logCollector;
        } else {
            LogCollector collector;
            try {
                collector = applicationContext.getBean(clz);
            } catch (Exception e) {
                collector = collectors.get(clz);
                if (collector == null) {
                    collector = BeanUtils.instantiateClass(clz);
                    collectors.put(clz, collector);
                }
            }
            return collector;
        }
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
                    LogData.step("Fail : \n" + sw);
                }
            }
            throw throwable;
        } finally {
            if (!aopLog.logOnErr() || !data.isSuccess()) {
                MethodSignature signature = (MethodSignature) point.getSignature();
                data.setAppName(appName);
                data.setCostTime(System.currentTimeMillis() - data.getLogDate().getTime());
                data.setType(aopLog.type());
                data.setMethod(signature.getDeclaringTypeName() + "#" + signature.getName());
                DataExtractor.logHttpRequest(data, aopLog.headers());
                if (aopLog.args()) {
                    data.setArgs(DataExtractor.getArgs(signature.getParameterNames(), point.getArgs()));
                }
                if (aopLog.respBody()) {
                    data.setRespBody(DataExtractor.getResult(result));
                }
                data.setSuccess(success);
                LogData.setCurrent(data);
                if (aopLog.asyncMode()) {
                    collectorExecutor.asyncExecute(selectLogCollector(aopLog.collector()), LogData.getCurrent());
                } else {
                    collectorExecutor.execute(selectLogCollector(aopLog.collector()), LogData.getCurrent());
                }
            }
        }
    }

}
