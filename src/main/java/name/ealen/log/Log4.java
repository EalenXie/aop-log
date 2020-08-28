package name.ealen.log;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author EalenXie Created on 2019/12/23 16:46.
 * 自定义日志对象 线程单例(不提供对外的构造方法,每个线程中仅有一个此对象)
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Log4 {

    /**
     * 请务必注意该对象 使用->释放 原则
     */
    private static final ThreadLocal<Log4> LOG_4_THREAD_LOCAL = new ThreadLocal<>();
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
     * 请求头部信息(可选择记录)
     */
    private Object headers;
    /**
     * 操作类型
     */
    private String type;
    /**
     * 方法内容 (如果此对象需要记录到 数据库 字段应该长度尽可能大 Mysql推荐用longtext)
     */
    private StringBuilder content = new StringBuilder();
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    /**
     * 耗时计算
     */
    public void toCostTime() {
        Log4 log4 = Log4.getCurrent();
        log4.setCostTime((System.currentTimeMillis() - logDate.getTime()));
        Log4.setCurrent(log4);
    }

    /**
     * 获取当前线程中的操作日志对象
     */
    public static Log4 getCurrent() {
        Log4 log4 = LOG_4_THREAD_LOCAL.get();
        if (log4 == null) {
            log4 = new Log4();
            log4.setLogDate(new Date());
            LOG_4_THREAD_LOCAL.set(log4);
        }
        return LOG_4_THREAD_LOCAL.get();
    }

    public static void setCurrent(Log4 log4) {
        LOG_4_THREAD_LOCAL.set(log4);
    }

    /**
     * 移除当前线程操作日志对象
     */
    public static void removeCurrent() {
        Log4 log4 = LOG_4_THREAD_LOCAL.get();
        if (log4 != null && log4.getContent() != null) log4.content.setLength(0);
        LOG_4_THREAD_LOCAL.remove();
    }

    /**
     * 内容记录记录 正常会在aop中结束释放
     *
     * @param step 这里可以使用 该方法记录每一个步骤 : 注意 调用该方法时 请注意释放 ; 不用此对象时，请 调用 移除当前线程操作日志对象
     */
    public static void step(String step) {
        Log4 log4 = getCurrent();
        log4.getContent().append(step).append("\n");
        setCurrent(log4);
    }

}
