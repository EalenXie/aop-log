package com.github.proxy;

import java.util.Date;

/**
 * @author EalenXie create on 2021/1/4 11:56
 */
public class ReqInfo {
    /**
     * 应用名称
     */
    private String appName;
    /**
     * 请求host
     */
    private String host;
    /**
     * 请求port
     */
    private int port;
    /**
     * 请求url
     */
    private String url;
    /**
     * 请求header信息
     */
    private Object httpHeaders;
    /**
     * 请求method
     */
    private String method;
    /**
     * 请求参数
     */
    private Object req;
    /**
     * 请求时间
     */
    private Date logDate;
    /**
     * 响应参数
     */
    private Object resp;
    /**
     * http 返回码
     */
    private int statusCode;
    /**
     * 耗时时间
     */
    private long costTime;
    /**
     * 备注
     */
    private String desc;
    /**
     * 请求状态 异常 false 正常 true
     */
    private boolean success;
    /**
     * url路径参数
     */
    private String urlParam;

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Object httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getReq() {
        return req;
    }

    public void setReq(Object req) {
        this.req = req;
    }

    public Date getLogDate() {
        return logDate == null ? null : (Date) logDate.clone();
    }

    public void setLogDate(Date logDate) {
        if (logDate != null) {
            this.logDate = (Date) logDate.clone();
        }
    }

    public Object getResp() {
        return resp;
    }

    public void setResp(Object resp) {
        this.resp = resp;
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
