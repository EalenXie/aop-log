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
public class AopLogHandler {
    private LogCollector collector;
    private Map<Class<? extends LogCollector>, LogCollector> collectors = new HashMap<>();
    @Resource
    private BeanFactory beanFactory;

    @Resource
    public void setCollector(LogCollector collector) {
        this.collector = collector;
    }

    public Object proceed(LogData data, ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        AopLog aopLog = signature.getMethod().getAnnotation(AopLog.class);
        if (aopLog == null) aopLog = point.getTarget().getClass().getAnnotation(AopLog.class);
        if (aopLog != null) {
            if (!aopLog.logOnErr()) {
                logProcessBefore(aopLog, data, point);
            }
            return proceed(aopLog, data, point);
        }
        return point.proceed();
    }

    public void logProcessBefore(AopLog aopLog, LogData data, ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        data.setAppName(SpringEnvHelper.getAppName());
        data.setType(aopLog.type());
        data.setMethod(signature.getDeclaringTypeName() + "#" + signature.getName());
        LogDataExtractor.logHttpRequest(data, aopLog.headers());
        if (aopLog.args()) {
            data.setArgs(LogDataExtractor.getArgs(signature.getParameterNames(), point.getArgs()));
        }
    }

    /**
     * 方法执行
     */
    private Object proceed(AopLog aopLog, LogData data, ProceedingJoinPoint point) throws Throwable {
        try {
            Object result = point.proceed();
            if (aopLog.respBody()) {
                data.setRespBody(LogDataExtractor.getResult(result));
            }
            data.setSuccess(true);
            return result;
        } catch (Throwable throwable) {
            if (aopLog.logOnErr()) {
                logProcessBefore(aopLog, data, point);
            }
            data.setSuccess(false);
            if (aopLog.stackTraceOnErr()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    LogData.step("Fail : \n" + sw.toString());
                }
            }
            throw throwable;
        } finally {
            data.toCostTime();
            LogData.setCurrent(data);
            if (!aopLog.logOnErr()) {
                logCollector(aopLog, data);
            } else {
                if (!data.isSuccess()) {
                    logCollector(aopLog, data);
                }
            }
        }
    }

    /**
     * 日志收集
     *
     * @param aopLog 日志注解
     * @param data   日志数据
     * @throws LogCollectException 日志收集异常
     */
    private void logCollector(AopLog aopLog, LogData data) throws LogCollectException {
        Class<? extends LogCollector> clz = aopLog.collector();
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
            c.collect(data);
        } else {
            collector.collect(data);
        }
    }

}
