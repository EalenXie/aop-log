package name.ealen.log;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author EalenXie Created on 2019/12/23 16:46.
 * 自定义日志对象 线程单例(不提供对外的构造方法,每个线程中仅有一个此对象)
 * 如果此对象需要记录到数据库 长字段需要注意长度问题 Mysql推荐用longtext
 */
@Data
public class LogDefine implements Serializable {

    private static final long serialVersionUID = -6795454806540874727L;

    /**
     * 请务必注意该对象 使用->释放 原则
     */
    private static final ThreadLocal<LogDefine> LOG_DEFINE_THREAD_LOCAL = new ThreadLocal<>();

    private LogDefine() {
        logDate = new Date();
    }

    /**
     * 请求Ip
     */
    private String clientIp;
    /**
     * 请求地址
     */
    private String reqUrl;
    /**
     * 请求头部信息(可选择记录)
     */
    private String headers;
    /**
     * 操作类型
     */
    private String type;
    /**
     * 方法内容 (如果此对象需要记录到 数据库 字段应该长度尽可能大 Mysql推荐用longtext)
     */
    private StringBuilder content;
    /**
     * 操作方法
     */
    private String method;
    /**
     * 参数 (如果此对象需要记录到 数据库 字段应该长度尽可能大 Mysql推荐用longtext)
     */
    private String args;
    /**
     * 响应体 (如果此对象需要记录到 数据库 字段应该长度尽可能大 Mysql推荐用longtext)
     */
    private String respBody;
    /**
     * 操作日期(调用日期)
     */
    private Date logDate;
    /**
     * 业务处理耗时
     */
    private Long costTime;
    /**
     * 执行状态 成功(true)/异常(false)  默认失败false
     */
    private Boolean success = false;

    /**
     * 耗时计算
     */
    public void toCostTime() {
        LogDefine define = LogDefine.getCurrent();
        define.setCostTime((System.currentTimeMillis() - getLogDate().getTime()));
        LogDefine.setCurrent(define);
    }


    /**
     * 获取当前线程中的操作日志对象
     */
    public static LogDefine getCurrent() {
        LogDefine define = LOG_DEFINE_THREAD_LOCAL.get();
        if (define == null) {
            define = new LogDefine();
            LOG_DEFINE_THREAD_LOCAL.set(define);
        }
        return define;
    }

    public static void setCurrent(LogDefine define) {
        LOG_DEFINE_THREAD_LOCAL.set(define);
    }

    /**
     * 移除当前线程操作日志对象
     */
    public static void removeCurrent() {
        LogDefine define = LOG_DEFINE_THREAD_LOCAL.get();
        if (define != null && define.getContent() != null) define.content.setLength(0);
        LOG_DEFINE_THREAD_LOCAL.remove();
    }

    /**
     * 内容记录记录 正常会在aop中结束释放
     *
     * @param step 这里可以使用 该方法记录每一个步骤 : 注意 调用该方法时 请注意释放 ; 不用此对象时，请 调用 移除当前线程操作日志对象
     */
    public static void logger(String step) {
        LogDefine define = getCurrent();
        if (define.getContent() == null) define.setContent(new StringBuilder());
        define.getContent().append(step).append("\n");
        setCurrent(define);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
