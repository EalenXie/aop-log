package name.ealen.global.advice.log;

import lombok.extern.slf4j.Slf4j;
import name.ealen.global.utils.HttpUtils;
import name.ealen.global.utils.SerializeConvert;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * @author EalenXie Created on 2020/1/2 17:52.
 * 全局异常日志 切面处理
 */
@Component
@Aspect
@Slf4j
public class GloLogAspect {

    /**
     * 将会切 被SysActLogNote注解标记的方法
     */
    @Pointcut("@within(GloLogNote)")
    public void gloLogNotePoint() {
        //ig
    }


    @Around("gloLogNotePoint()")
    public Object gloLog(ProceedingJoinPoint point) throws Throwable {
        Object result;
        //1. 获取当前线程日志对象
        GloLog gloLog = GloLog.getCurrent();
        //2. 获取方法签名对象
        MethodSignature signature = (MethodSignature) point.getSignature();
        //3. 获取注解对象
        GloLogNote note = signature.getMethod().getAnnotation(GloLogNote.class);
        if (note == null) note = point.getTarget().getClass().getAnnotation(GloLogNote.class);
        //4. 是否记录参数
        if (note.args()) gloLog.setArgs(SerializeConvert.toJsonStringNoException(point.getArgs()));
        //5. 是否记录方法
        if (note.method()) gloLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
        //6. 记录操作分类
        gloLog.setType(note.type());
        //7. 抓取HttpServletRequest中的信息
        getByServletRequest(gloLog);
        try {
            //8. 方法逻辑执行
            result = point.proceed();
            //9. 是否记录响应
            if (note.respBody()) gloLog.setRespBody(SerializeConvert.toJsonStringNoException(result));
            //10. 记录方法完成状态
            gloLog.setSuccess(true);
            //11. 记录当前线程日志对象
            GloLog.setCurrent(gloLog);
        } catch (Throwable throwable) {
            //12. 记录方法完成状态
            gloLog.setSuccess(false);
            //13. 是否记录异常堆栈信息到content
            if (note.stackTrace()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    GloLog.contentRecord("Fail : \n" + sw.toString());
                }
            }
            //14. point.proceed()的异常务必抛出 , 交由后置异常通知处理或者全局异常处理
            throw throwable;
        } finally {
            //15. 计算耗时
            if (note.costTime()) gloLog.costTimeCompute();
            //16. 记录当前线程日志对象
            GloLog.setCurrent(gloLog);
            //17. 此时可以对此对象 进行记录 或者 收集 .....
            //do it yourself
            String str= gloLog + "\n";
//            FileConvert.writeStringToFile(str, new File("D:\\home","glo-log.txt"));
        }
        //18. 当以上过程执行完成并成功后,释放TreadLocal中的操作日志对象资源
        GloLog.removeCurrent();
        return result;
    }


    /**
     * 为操作日志抓取HttpServletRequest中的信息,如Ip,url,userAgent等等
     */
    private void getByServletRequest(GloLog gloLog) {
        HttpServletRequest request = HttpUtils.getHttpServletRequest();
        if (Objects.nonNull(request)) {
            //1. 记录一下clientIp
            gloLog.setClientIp(HttpUtils.getIpAddress(request));
            //2. 记录一下请求的url
            gloLog.setReqUrl(request.getRequestURL().toString());
            //3. 选取记录的header信息 本例只记录一下User-Agent 可按自己业务进行选择记录
            gloLog.setHeaders(HttpUtils.getJsonHeaders(request, HttpHeaders.USER_AGENT));
        }
    }

    /**
     * 后置异常通知处理完成 之后 交由全局异常通知处理
     */
    @AfterThrowing(pointcut = "gloLogNotePoint()", throwing = "throwable")
    public void throwable(Throwable throwable) {
        //ig 目前直接交由全局异常通知处理
        GloLog.removeCurrent();
    }


}
