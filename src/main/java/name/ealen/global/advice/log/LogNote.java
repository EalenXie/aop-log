package name.ealen.global.advice.log;

import name.ealen.global.advice.log.collector.LogCollector;
import org.springframework.http.HttpHeaders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author EalenXie Created on 2020/1/2 17:49.
 * 自定义全局异常注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogNote {

    /**
     * 操作类型(操作分类)
     */
    String type() default "undefined";

    /**
     * 切面是否记录 请求方法
     */
    boolean method() default true;

    /**
     * 切面是否记录 请求耗时
     */
    boolean costTime() default true;

    /**
     * 记录的headers ,默认只记录一下 user-agent
     */
    String[] headers() default HttpHeaders.USER_AGENT;

    /**
     * 切面是否记录 请求参数
     */
    boolean args() default false;

    /**
     * 切面是否记录 响应参数
     */
    boolean respBody() default false;

    /**
     * 当发生异常时 , 切面是否记录异常堆栈信息到content
     */
    boolean stackTrace() default false;

    /**
     * 收集器
     */
    Class<? extends LogCollector> collector();


}
