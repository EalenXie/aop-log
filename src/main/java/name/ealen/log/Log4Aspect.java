package name.ealen.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
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
        //1. 获取当前线程日志对象
        Log4 log4 = Log4.getCurrent();
        //2. 程序执行
        Object result = log4aHandler.proceed(log4, point);
        //3. 当以上过程执行完成并成功后,释放TreadLocal中的操作日志对象资源
        Log4.removeCurrent();
        return result;
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
