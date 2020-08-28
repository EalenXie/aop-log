package name.ealen.log;

import lombok.extern.slf4j.Slf4j;
import name.ealen.log.collector.LogCollectException;
import name.ealen.log.collector.LogCollector;
import name.ealen.log.collector.NothingCollector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author EalenXie create on 2020/6/28 15:07
 */
@Slf4j
@Component
public class Log4aHandler {
    private LogCollector collector;
    private Map<Class<? extends LogCollector>, LogCollector> collectors = new HashMap<>();
    @Resource
    private BeanFactory beanFactory;

    @Resource
    public void setCollector(LogCollector collector) {
        this.collector = collector;
    }

    public Object proceed(Log4 log4, ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Log4a log4a = signature.getMethod().getAnnotation(Log4a.class);
        if (log4a == null) log4a = point.getTarget().getClass().getAnnotation(Log4a.class);
        if (log4a != null) {
            if (!log4a.logOnErr()) {
                logProcessBefore(log4a, log4, point);
            }
            return proceed(log4a, log4, point);
        }
        return point.proceed();
    }

    public void logProcessBefore(Log4a log4a, Log4 log4, ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        log4.setAppName(SpringEnvHelper.getAppName());
        log4.setType(log4a.type());
        log4.setMethod(signature.getDeclaringTypeName() + "#" + signature.getName());
        Log4Extractor.logHttpRequest(log4, log4a.headers());
        if (log4a.args()) {
            log4.setArgs(Log4Extractor.getArgs(signature.getParameterNames(), point.getArgs()));
        }
    }

    /**
     * 方法执行
     */
    private Object proceed(Log4a log4a, Log4 log4, ProceedingJoinPoint point) throws Throwable {
        try {
            Object result = point.proceed();
            if (log4a.respBody()) {
                log4.setRespBody(Log4Extractor.getResult(result));
            }
            log4.setSuccess(true);
            return result;
        } catch (Throwable throwable) {
            if (log4a.logOnErr()) {
                logProcessBefore(log4a, log4, point);
            }
            log4.setSuccess(false);
            if (log4a.stackTraceOnErr()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    Log4.step("Fail : \n" + sw.toString());
                }
            }
            throw throwable;
        } finally {
            log4.toCostTime();
            Log4.setCurrent(log4);
            if (!log4a.logOnErr()) {
                logCollector(log4a, log4);
            } else {
                if (!log4.isSuccess()) {
                    logCollector(log4a, log4);
                }
            }
        }
    }

    /**
     * 日志收集
     *
     * @param log4a 日志注解
     * @param log4  日志定义
     * @throws LogCollectException 日志收集异常
     */
    private void logCollector(Log4a log4a, Log4 log4) throws LogCollectException {
        Class<? extends LogCollector> clz = log4a.collector();
        if (clz != NothingCollector.class) {
            LogCollector c;
            try {
                c = beanFactory.getBean(clz);
            } catch (Exception e) {
                c = collectors.get(clz);
                if (c == null) {
                    c = BeanUtils.instantiateClass(clz);
                    collectors.put(clz, c);
                }
            }
            c.collect(log4);
        } else {
            collector.collect(log4);
        }
    }

}
