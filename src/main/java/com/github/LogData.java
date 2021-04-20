package com.github;


import java.util.Date;

/**
 * @author EalenXie Created on 2019/12/23 16:46.
 * Log data object (no external constructor is provided; there is only one such object per thread)
 * 自定义日志数据对象 (不提供对外的构造方法,每个线程中仅有一个此对象)
 */
public class LogData {

    private LogData() {
    }

    /**
     * 线程LogData对象
     */
    private static final ThreadLocal<LogData> LOG_DATA = new ThreadLocal<>();
    /**
     * 线程StringBuilder对象 主要用于追加字段到最终的content
     */
    private static final ThreadLocal<StringBuilder> CONTENT_BUILDER = new ThreadLocal<>();
    /**
     * 应用名
     */
    private String appName;
    /**
     * 主机
     */
    private String host;
    /**
     * 端口号
     */
    private Integer port;
    /**
     * 请求Ip
     */
    private String clientIp;
    /**
     * 请求地址
     */
    private String reqUrl;
    /**
     * http请求method
     */
    private String httpMethod;
    /**
     * 请求头部信息(可选择记录)
     */
    private Object headers;
    /**
     * 操作类型
     */
    private String type;
    /**
     * 方法内容
     */
    private String content;
    /**
     * 操作方法
     */
    private String method;
    /**
     * 参数
     */
    private Object args;
    /**
     * 响应体
     */
    private Object respBody;
    /**
     * 操作日期(调用日期)
     */
    private Date logDate;
    /**
     * 业务处理耗时
     */
    private long costTime;
    /**
     * 线程名
     */
    private String threadName = Thread.currentThread().getName();
    /**
     * 线程Id
     */
    private long threadId = Thread.currentThread().getId();
    /**
     * 执行状态 成功(true)/异常(false)  默认失败false
     */
    private boolean success = false;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Object getHeaders() {
        return headers;
    }

    public void setHeaders(Object headers) {
        this.headers = headers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getArgs() {
        return args;
    }

    public void setArgs(Object args) {
        this.args = args;
    }

    public Object getRespBody() {
        return respBody;
    }

    public void setRespBody(Object respBody) {
        this.respBody = respBody;
    }

    public Date getLogDate() {
        return logDate == null ? null : (Date) logDate.clone();
    }

    public void setLogDate(Date logDate) {
        if (logDate != null) {
            this.logDate = (Date) logDate.clone();
        }
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取当前线程中的操作日志对象
     *
     * @return Gets the LogData in the current thread
     */
    protected static LogData getCurrent() {
        if (LOG_DATA.get() == null) {
            LogData logData = new LogData();
            logData.setLogDate(new Date());
            StringBuilder sb = CONTENT_BUILDER.get();
            if (sb == null) {
                CONTENT_BUILDER.set(new StringBuilder());
            }
            LOG_DATA.set(logData);
        }
        return LOG_DATA.get();
    }

    /**
     * 设置当前线程中的操作日志对象
     *
     * @param logData AopLog日志对象
     */
    protected static void setCurrent(LogData logData) {
        if (CONTENT_BUILDER.get() != null) {
            logData.setContent(CONTENT_BUILDER.get().toString());
        }
        LOG_DATA.set(logData);
    }

    /**
     * 移除当前线程AopLog日志对象
     */
    public static void removeCurrent() {
        CONTENT_BUILDER.remove();
        LOG_DATA.remove();
    }

    /**
     * 内容记录记录
     *
     * @param step 这里可以使用 该方法记录每一个步骤
     */
    public static void step(String step) {
        StringBuilder sb = CONTENT_BUILDER.get();
        if (sb != null) {
            sb.append(step).append("\n");
            CONTENT_BUILDER.set(sb);
        }
    }

    /**
     * 内容记录步骤记录  例如 step("ABC--{}--EFG ", "D")   ABC--D--EFG
     *
     * @param stepTemplate step模板 该方法记录每一个步骤
     * @param args         模板参数
     */
    public static void step(String stepTemplate, Object... args) {
        step(MessageFormatter.format(stepTemplate, args));
    }

}
