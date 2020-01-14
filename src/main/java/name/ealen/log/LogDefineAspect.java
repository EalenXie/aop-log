package name.ealen.log;

import lombok.extern.slf4j.Slf4j;
import name.ealen.log.collector.LogCollectException;
import name.ealen.log.collector.LogCollector;
import name.ealen.log.collector.NothingCollector;
import name.ealen.utils.HttpUtils;
import name.ealen.utils.SerializeConvert;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author EalenXie Created on 2020/1/2 17:52.
 * 全局异常日志 切面处理
 */
@Component
@Aspect
@Slf4j
public class LogDefineAspect {

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
    @Pointcut("@within(LogNote)")
    public void noteClass() {
        //ig
    }

    /**
     * 将会切 被LogNote注解标记的方法
     */
    @Pointcut("@annotation(LogNote)")
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
        LogDefine define = LogDefine.getCurrent();
        //2. 获取方法签名对象
        MethodSignature signature = (MethodSignature) point.getSignature();
        //3. 获取注解对象
        LogNote note = signature.getMethod().getAnnotation(LogNote.class);
        if (note == null) note = point.getTarget().getClass().getAnnotation(LogNote.class);
        //4. 是否记录参数
        if (note.args()) define.setArgs(SerializeConvert.toJsonStringNoException(point.getArgs()));
        //5. 是否记录方法
        if (note.method()) define.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
        //6. 记录操作分类
        define.setType(note.type());
        //7. 抓取HttpServletRequest中的信息
        getByServletRequest(define, note.headers());
        try {
            //8. 方法逻辑执行
            result = point.proceed();
            //9. 是否记录响应
            if (note.respBody()) define.setRespBody(SerializeConvert.toJsonStringNoException(result));
            //10. 记录方法完成状态
            define.setSuccess(true);
            //11. 记录当前线程日志对象
            LogDefine.setCurrent(define);
        } catch (Throwable throwable) {
            //12. 记录方法完成状态
            define.setSuccess(false);
            //13. 是否记录异常堆栈信息到content
            if (note.stackTrace()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    LogDefine.logger("Fail : \n" + sw.toString());
                }
            }
            //14. point.proceed()的异常务必抛出 , 交由后置异常通知处理或者全局异常处理
            throw throwable;
        } finally {
            //15. 计算耗时
            if (note.costTime()) define.toCostTime();
            //16. 记录当前线程日志对象
            LogDefine.setCurrent(define);
            //17. 日志收集
            logCollector(note, define);
        }
        //18. 当以上过程执行完成并成功后,释放TreadLocal中的操作日志对象资源
        LogDefine.removeCurrent();
        return result;
    }


    /**
     * 日志收集
     *
     * @param note   日志注解
     * @param define 日志定义
     * @throws LogCollectException 日志收集异常
     */
    private void logCollector(LogNote note, LogDefine define) throws LogCollectException {
        //1. 获取收集器
        Class<? extends LogCollector> clz = note.collector();
        //2. 查看是否有指定收集器 有则使用 指定收集器 进行日志收集
        if (clz != NothingCollector.class) {
            LogCollector c;
            try {
                c = beanFactory.getBean(clz);
            } catch (Exception e) {
                c = collectors.get(clz);
                if (c == null) {
                    try {
                        c = clz.newInstance();
                    } catch (InstantiationException | IllegalAccessException ex) {
                        throw new LogCollectException("LogCollector cannot be acquire", ex);
                    }
                    collectors.put(clz, c);
                }
            }
            c.collect(define);
        } else {
            collector.collect(define);
        }
    }


    /**
     * 为操作日志抓取HttpServletRequest中的信息,如Ip,url,userAgent等等
     */
    private void getByServletRequest(LogDefine define, String[] headers) {
        HttpServletRequest request = HttpUtils.getNonNullHttpServletRequest();
        if (Objects.nonNull(request)) {
            //1. 记录一下clientIp
            define.setClientIp(HttpUtils.getIpAddress(request));
            //2. 记录一下请求的url
            define.setReqUrl(request.getRequestURL().toString());
            //3. 选取记录的header信息 本例只记录一下User-Agent 可按自己业务进行选择记录
            define.setHeaders(HttpUtils.getJsonHeaders(request, headers));
        }
    }

    /**
     * 后置异常通知处理完成 之后 交由全局异常通知处理
     */
    @AfterThrowing(pointcut = "noteClass()", throwing = "throwable")
    public void classThrowable(Throwable throwable) {
        //ig 目前直接交由全局异常通知处理
        LogDefine.removeCurrent();
    }

    /**
     * 后置异常通知处理完成 之后 交由全局异常通知处理
     */
    @AfterThrowing(pointcut = "noteMethod()", throwing = "throwable")
    public void methodThrowable(Throwable throwable) {
        //ig 目前直接交由全局异常通知处理
        LogDefine.removeCurrent();
    }

}
