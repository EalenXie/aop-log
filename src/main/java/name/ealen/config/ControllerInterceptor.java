package name.ealen.config;

import com.alibaba.fastjson.JSON;
import name.ealen.model.ExceptionResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by EalenXie on 2018/9/7 14:19.
 * AOP打印日志 : 请求的对象,请求参数,返回数据,请求状态,内部方法耗时
 */
@Aspect
@Component
public class ControllerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ControllerInterceptor.class);

    @Resource
    private Environment environment;

    private String getAppName() {
        try {
            return environment.getProperty("spring.application.name");
        } catch (Exception ignore) {
            return "unnamed";
        }
    }

    /**
     * 获取Application Profiles
     */
    private String getApplicationProfile() {
        StringBuilder profile = new StringBuilder(environment.getDefaultProfiles()[0]);
        if (environment.getActiveProfiles().length != 0) {
            profile = new StringBuilder(environment.getActiveProfiles()[0]);
            if (environment.getActiveProfiles().length > 1) {
                for (int i = 1; i < environment.getActiveProfiles().length; i++) {
                    profile.append(",").append(environment.getActiveProfiles()[i]);
                }
            }
        }
        return profile.toString();
    }

    /**
     * 注意 : pjp.proceed()执行的异常请务必抛出，交由ControllerAdvice捕捉到并处理
     */
    @Around(value = "execution (*  name.ealen.web.*.*(..))")
    public Object processApiFacade(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        String name = pjp.getTarget().getClass().getSimpleName();
        String method = pjp.getSignature().getName();
        Object result;
        try {
            Object[] requestParams = pjp.getArgs();
            log.info(String.format("RequestTarget : %s start with %s %s.%s", getAppName(), getApplicationProfile(), name, method));
            if (requestParams.length > 0) {     //日志打印请求参数
                try {
                    log.info("RequestParam : {}", JSON.toJSON(requestParams));
                    ExceptionResponse.getCurrentException().setRequestBody(JSON.toJSON(requestParams).toString());
                } catch (Exception e) {
                    for (Object param : requestParams) {
                        try {
                            log.info("RequestParam : {}", JSON.toJSON(param));
                        } catch (Exception ig) {
                            log.info("RequestParam : {}", param);
                        }
                    }
                }
            }
            result = pjp.proceed();
            try {
                ExceptionResponse.getCurrentException().setResponseBody(JSON.toJSONString(result));
                log.info("ResponseBody: {}", JSON.toJSONString(result));
            } catch (Exception ignore) {
                log.info("ResponseBody Exception ignore");
            } finally {
                ExceptionResponse.removeExceptionResponse();
            }
        } finally {
            log.info("Internal Method Cost Time: {}ms", System.currentTimeMillis() - startTime);
        }
        return result;
    }

}
