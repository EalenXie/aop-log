package com.github;


import com.github.collector.LogCollector;
import com.github.collector.NothingCollector;
import org.springframework.http.HttpHeaders;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author EalenXie Created on 2020/1/2 17:49.
 * AopLog Annotation
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AopLog {

    /**
     * 仅当发生异常时才记录
     *
     * @return Only record when an exception occurs
     */
    boolean logOnErr() default false;

    /**
     * 操作类型(操作分类)
     *
     * @return Operating the tag
     */
    String type() default "undefined";

    /**
     * 记录的headers ,默认记录 content-type user-agent
     *
     * @return Headers for recording, default record content-type user-Agent
     */
    String[] headers() default {HttpHeaders.USER_AGENT, HttpHeaders.CONTENT_TYPE};

    /**
     * 切面是否记录 请求参数
     *
     * @return Whether the AOP records the request parameters
     */
    boolean args() default true;

    /**
     * 切面是否记录 响应参数
     *
     * @return Whether the AOP records the response parameters
     */
    boolean respBody() default true;

    /**
     * 当发生异常时,AOP是否追加异常堆栈信息到content
     *
     * @return When an exception occurs, does the slice append the exception stack information to content
     */
    boolean stackTraceOnErr() default false;

    /**
     * 异步模式 收集日志
     *
     * @return Asynchronous mode collects logs
     */
    boolean asyncMode() default true;

    /**
     * 指定专门的收集器
     *
     * @return Specify a specialized collector
     */
    Class<? extends LogCollector> collector() default NothingCollector.class;
}
