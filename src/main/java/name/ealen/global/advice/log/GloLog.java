package name.ealen.global.advice.log;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * @author EalenXie Created on 2019/12/23 16:46.
 * 全局 自定义 线程单例(不提供对外的构造方法,每个线程中有一个此对象) 日志对象
 * 如果此对象需要记录到数据库 长字段需要注意长度问题 Mysql推荐用longtext
 */
@Data
public class GloLog implements Serializable {

    private static final long serialVersionUID = -6795454806540874727L;

    /**
     * 请务必注意该对象 使用->释放 原则
     */
    private static final ThreadLocal<GloLog> GLO_LOG_THREAD_LOCAL = new ThreadLocal<>();



    private GloLog() {
        actDate = new Date();
    }

    /**
     * 请求Ip
     */
    @Size(max = 40)
    private String clientIp;
    /**
     * 请求地址
     */
    @Size(max = 200)
    private String reqUrl;
    /**
     * 请求头部信息(可选择记录 如果此对象需要记录到 数据库 字段应该长度尽可能大 Mysql推荐用longtext )
     */
    private String headers;
    /**
     * 操作类型
     */
    @Size(max = 30)
    private String type;
    /**
     * 方法内容 (如果此对象需要记录到 数据库 字段应该长度尽可能大 Mysql推荐用longtext)
     */
    private StringBuilder content;
    /**
     * 操作方法
     */
    @Size(max = 100)
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date actDate;
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
    public void costTimeCompute() {
        GloLog gloLog = GloLog.getCurrent();
        gloLog.setCostTime((System.currentTimeMillis() - getActDate().getTime()));
        GloLog.setCurrent(gloLog);
    }


    /**
     * 获取当前线程中的操作日志对象
     */
    public static GloLog getCurrent() {
        GloLog actLog = GLO_LOG_THREAD_LOCAL.get();
        if (actLog == null) {
            actLog = new GloLog();
            GLO_LOG_THREAD_LOCAL.set(actLog);
        }
        return actLog;
    }

    public static void setCurrent(GloLog actLog) {
        GLO_LOG_THREAD_LOCAL.set(actLog);
    }

    /**
     * 移除当前线程操作日志对象
     */
    public static void removeCurrent() {
        GloLog actLog = GLO_LOG_THREAD_LOCAL.get();
        if (actLog != null && actLog.getContent() != null) actLog.content.setLength(0);
        GLO_LOG_THREAD_LOCAL.remove();
    }

    /**
     * 内容记录记录
     *
     * @param step 这里可以使用 该方法记录每一个步骤 : 注意 调用该方法时 请注意释放 ; 不用此对象时，请 调用 移除当前线程操作日志对象
     */
    public static void contentRecord(String step) {
        GloLog gloLog = getCurrent();
        if (gloLog.getContent() == null) gloLog.setContent(new StringBuilder());
        gloLog.getContent().append(step).append("\n");
        setCurrent(gloLog);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

}
