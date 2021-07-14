package com.github;

import com.github.collector.LogCollector;
import com.github.collector.NothingCollector;
import org.springframework.http.HttpHeaders;

/**
 * Created by EalenXie on 2021/7/14 11:48
 */
public class AopLogConfig {
    /**
     * 仅当发生异常时才记录
     */
    private boolean logOnErr;
    /**
     * 操作标签(操作分类)
     */
    private String tag;
    /**
     * 记录的headers ,默认记录 content-type user-agent
     */
    private String[] headers;
    /**
     * 切面是否记录 请求参数
     */
    private boolean args;
    /**
     * 切面是否记录 响应参数
     */
    private boolean respBody;
    /**
     * 当发生异常时,AOP是否追加异常堆栈信息到content
     */
    private boolean stackTraceOnErr;
    /**
     * 异步模式 收集日志
     */
    private boolean asyncMode;
    /**
     * 指定专门的收集器
     */
    private Class<? extends LogCollector> collector;

    public AopLogConfig() {
        this.logOnErr = false;
        this.tag = "undefined";
        this.headers = new String[]{HttpHeaders.USER_AGENT, HttpHeaders.CONTENT_TYPE};
        this.args = true;
        this.respBody = true;
        this.stackTraceOnErr = true;
        this.asyncMode = true;
        this.collector = NothingCollector.class;
    }

    public boolean isLogOnErr() {
        return logOnErr;
    }

    public void setLogOnErr(boolean logOnErr) {
        this.logOnErr = logOnErr;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String[] getHeaders() {
        return headers;
    }

    public void setHeaders(String[] headers) {
        this.headers = headers;
    }

    public boolean isArgs() {
        return args;
    }

    public void setArgs(boolean args) {
        this.args = args;
    }

    public boolean isRespBody() {
        return respBody;
    }

    public void setRespBody(boolean respBody) {
        this.respBody = respBody;
    }

    public boolean isStackTraceOnErr() {
        return stackTraceOnErr;
    }

    public void setStackTraceOnErr(boolean stackTraceOnErr) {
        this.stackTraceOnErr = stackTraceOnErr;
    }

    public boolean isAsyncMode() {
        return asyncMode;
    }

    public void setAsyncMode(boolean asyncMode) {
        this.asyncMode = asyncMode;
    }

    public Class<? extends LogCollector> getCollector() {
        return collector;
    }

    public void setCollector(Class<? extends LogCollector> collector) {
        this.collector = collector;
    }
}
