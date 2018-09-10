package name.ealen.config;

import com.alibaba.fastjson.JSON;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Around(value = "execution (*  name.ealen.web.*.*(..))")
    public Object processApiFacade(ProceedingJoinPoint pjp) {
        String appName;
        try {
            appName = environment.getProperty("spring.application.name").toUpperCase();
        } catch (Exception e) {
            appName = "UNNAMED";
        }
        long startTime = System.currentTimeMillis();
        String name = pjp.getTarget().getClass().getSimpleName();
        String method = pjp.getSignature().getName();
        Object result = null;
        HttpStatus status = null;
        try {
            result = pjp.proceed();
            log.info("RequestTarget : " + appName + "." + name + "." + method);
            log.info("RequestParam : " + JSON.toJSON(pjp.getArgs()));
            if (result instanceof ResponseEntity) {
                status = ((ResponseEntity) result).getStatusCode();
            } else {
                status = HttpStatus.OK;
            }
        } catch (Throwable throwable) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            result = new ResponseEntity<>("{\"Internal Server Error\" : \"" + throwable.getMessage() + "\"}", status);
            throwable.printStackTrace();
        } finally {
            log.info("ResponseEntity : {" + "\"HttpStatus\":\"" + status.toString() + "\"" + ",\"ResponseBody\": " + JSON.toJSON(result) + "}");
            log.info("Internal Method Cost Time: {}ms", System.currentTimeMillis() - startTime);
        }
        return result;
    }
}
