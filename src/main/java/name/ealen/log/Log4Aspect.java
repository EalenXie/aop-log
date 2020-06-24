package name.ealen.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import name.ealen.log.collector.LogCollectException;
import name.ealen.log.collector.LogCollector;
import name.ealen.log.collector.NothingCollector;
import name.ealen.utils.HttpUtils;
import name.ealen.utils.SerializeConvert;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EalenXie Created on 2020/1/2 17:52.
 * 全局异常日志 切面处理
 */
@Component
@Aspect
@Slf4j
public class Log4Aspect {

    private LogCollector collector;

    private Map<Class<? extends LogCollector>, LogCollector> collectors = new HashMap<>();

    @Resource
    private BeanFactory beanFactory;

    @Resource
    public void setCollector(LogCollector collector) {
        this.collector = collector;
    }

    /**
     * 将会切 被LogNote注解标记的类
     */
    @Pointcut("@within(Log4a)")
    public void noteClass() {
        //ig
    }

    /**
     * 将会切 被LogNote注解标记的方法
     */
    @Pointcut("@annotation(Log4a)")
    public void noteMethod() {
        //ig
    }

    @Around("noteClass()")
    public Object noteClass(ProceedingJoinPoint point) throws Throwable {
        return logger(point);
    }

    @Around("noteMethod()")
    public Object noteMethod(ProceedingJoinPoint point) throws Throwable {
        return logger(point);
    }

    private Object logger(ProceedingJoinPoint point) throws Throwable {
        Object result;
        //1. 获取当前线程日志对象
        Log4 log4 = Log4.getCurrent();
        //2. 获取方法签名对象
        MethodSignature signature = (MethodSignature) point.getSignature();
        //3. 获取注解对象
        Log4a log4a = signature.getMethod().getAnnotation(Log4a.class);
        if (log4a == null) log4a = point.getTarget().getClass().getAnnotation(Log4a.class);
        //4. 是否记录参数
        if (log4a.args()) log4.setArgs(point.getArgs());
        //5. 是否记录方法
        if (log4a.method()) log4.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
        //6. 记录操作分类
        log4.setType(log4a.type());
        //7. 抓取HttpServletRequest中的信息
        getByServletRequest(log4, log4a.headers());
        try {
            //8. 方法逻辑执行
            result = point.proceed();
            //9. 是否记录响应
            if (log4a.respBody()) log4.setRespBody(JSON.toJSON(result));
            //10. 记录方法完成状态
            log4.setSuccess(true);
        } catch (Throwable throwable) {
            //11. 记录方法完成状态
            log4.setSuccess(false);
            //12. 是否记录异常堆栈信息到content
            if (log4a.stackTrace()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    Log4.step("Fail : \n" + sw.toString());
                }
            }
            //13. point.proceed()的异常务必抛出 , 交由后置异常通知处理或者全局异常处理
            throw throwable;
        } finally {
            //14. 计算耗时
            if (log4a.costTime()) log4.toCostTime();
            //15. 记录当前线程日志对象
            Log4.setCurrent(log4);
            //16. 日志收集
            logCollector(log4a, log4);
        }
        //18. 当以上过程执行完成并成功后,释放TreadLocal中的操作日志对象资源
        Log4.removeCurrent();
        return result;
    }


    /**
     * 日志收集
     *
     * @param log4a 日志注解
     * @param log4  日志定义
     * @throws LogCollectException 日志收集异常
     */
    private void logCollector(Log4a log4a, Log4 log4) throws LogCollectException {
        //1. 获取收集器
        Class<? extends LogCollector> clz = log4a.collector();
        //2. 查看是否有指定收集器 有则使用 指定收集器 进行日志收集
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


    /**
     * 为操作日志抓取HttpServletRequest中的信息,如Ip,url,userAgent等等
     */
    private void getByServletRequest(Log4 log4, String[] headers) {
        HttpServletRequest request = HttpUtils.getNonNullHttpServletRequest();
        //1. 记录一下clientIp
        log4.setClientIp(HttpUtils.getIpAddress(request));
        //2. 记录一下请求的url
        log4.setReqUrl(request.getRequestURL().toString());
        //3. 选取记录的header信息 本例只记录一下User-Agent 可按自己业务进行选择记录
        Map<String, String> headersMap = new HashMap<>();
        for (String header : headers) {
            String value = request.getHeader(header);
            if (StringUtils.isNotEmpty(value)) {
                headersMap.put(header, request.getHeader(header));
            }
        }
        log4.setHeaders(headersMap);
    }

    /**
     * 后置异常通知处理完成 之后 交由全局异常通知处理
     */
    @AfterThrowing(pointcut = "noteClass()", throwing = "throwable")
    public void classThrowable(Throwable throwable) {
        //ig 目前直接交由全局异常通知处理
        Log4.removeCurrent();
    }

    /**
     * 后置异常通知处理完成 之后 交由全局异常通知处理
     */
    @AfterThrowing(pointcut = "noteMethod()", throwing = "throwable")
    public void methodThrowable(Throwable throwable) {
        //ig 目前直接交由全局异常通知处理
        Log4.removeCurrent();
    }

}
