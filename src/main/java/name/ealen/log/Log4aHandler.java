package name.ealen.log;

import lombok.extern.slf4j.Slf4j;
import name.ealen.log.collector.LogCollectException;
import name.ealen.log.collector.LogCollector;
import name.ealen.log.collector.NothingCollector;
import name.ealen.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author EalenXie create on 2020/6/28 15:07
 */
@Slf4j
@Component
public class Log4aHandler {
    private LogCollector collector;
    private Map<Class<? extends LogCollector>, LogCollector> collectors = new HashMap<>();
    @Resource
    private BeanFactory beanFactory;

    @Resource
    public void setCollector(LogCollector collector) {
        this.collector = collector;
    }

    public Object proceed(Log4 log4, ProceedingJoinPoint point) throws Throwable {
        // 获取注解内容
        MethodSignature signature = (MethodSignature) point.getSignature();
        Log4a log4a = signature.getMethod().getAnnotation(Log4a.class);
        if (log4a == null) log4a = point.getTarget().getClass().getAnnotation(Log4a.class);
        if (log4a != null) {
            // 是否记录方法
            if (log4a.method()) log4.setMethod(signature.getDeclaringTypeName() + "#" + signature.getName());
            // 记录操作分类
            log4.setType(log4a.type());
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = null;
            HttpServletResponse response = null;
            if (attributes != null) {
                request = attributes.getRequest();
                response = attributes.getResponse();
                //获取Ip和URL
                log4.setClientIp(HttpUtils.getIpAddress(request));
                //获取URL
                log4.setReqUrl(request.getRequestURL().toString());
                //获取Header信息
                log4.setHeaders(getHeadersMap(request, log4a.headers()));
            }
            //记录参数
            if (log4a.args()) {
                log4.setArgs(getArgs(request, signature.getParameterNames(), point.getArgs()));
            }
            //记录执行(响应,状态,耗时,并进行日志收集)
            return proceed(log4a, log4, point, response);
        }
        return point.proceed();
    }

    /**
     * 方法执行
     */
    private Object proceed(Log4a log4a, Log4 log4, ProceedingJoinPoint point, HttpServletResponse response) throws Throwable {
        try {
            // 方法逻辑执行
            Object result = point.proceed();
            //是否记录响应
            if (log4a.respBody()) {
                if (response != null && MediaType.APPLICATION_XML_VALUE.equals(response.getContentType())) {
                    log4.setRespBody(xmlParam(result));
                } else {
                    log4.setRespBody(result);
                }
            }
            //记录方法完成状态 成功
            log4.setSuccess(true);
            return result;
        } catch (Throwable throwable) {
            //记录方法完成状态 失败
            log4.setSuccess(false);
            //是否记录异常堆栈信息到content
            if (log4a.stackTrace()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    Log4.step("Fail : \n" + sw.toString());
                }
            }
            //point.proceed()的异常务必抛出 , 交由后置异常通知处理或者全局异常处理
            throw throwable;
        } finally {
            //计算耗时
            if (log4a.costTime()) log4.toCostTime();
            //记录当前线程日志对象
            Log4.setCurrent(log4);
            //日志收集
            logCollector(log4a, log4);
        }
    }

    /**
     * 日志收集
     *
     * @param log4a 日志注解
     * @param log4  日志定义
     * @throws LogCollectException 日志收集异常
     */
    private void logCollector(Log4a log4a, Log4 log4) throws LogCollectException {
        //1. 获取收集器
        Class<? extends LogCollector> clz = log4a.collector();
        //2. 查看是否有指定收集器 有则使用 指定收集器 进行日志收集
        if (clz != NothingCollector.class) {
            LogCollector c;
            try {
                c = beanFactory.getBean(clz);
            } catch (Exception e) {
                c = collectors.get(clz);
                if (c == null) {
                    c = BeanUtils.instantiateClass(clz);
                    collectors.put(clz, c);
                }
            }
            c.collect(log4);
        } else {
            collector.collect(log4);
        }
    }

    /**
     * 获取HeadersMap对象
     *
     * @param request HttpServletRequest
     * @param headers 需要记录的headers列表
     */
    private Map<String, String> getHeadersMap(HttpServletRequest request, String[] headers) {
        // 选取记录的header信息 本例只记录一下User-Agent 可按自己业务进行选择记录
        Map<String, String> headersMap = new HashMap<>();
        for (String header : headers) {
            String value = request.getHeader(header);
            if (StringUtils.isNotEmpty(value)) {
                headersMap.put(header, request.getHeader(header));
            }
        }
        return headersMap;
    }

    /**
     * 获取参数
     */
    private Object getArgs(HttpServletRequest request, String[] parameterNames, Object[] args) {
        if (request != null) {
            // application/x-www-form-urlencoded 参数记录
            if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(request.getContentType())) {
                return formParam(request);
            }
            Object target;
            if (args.length == 1) target = args[0];
            else target = args;
            if (target == null) return null;
            // application/xml 参数记录
            if (MediaType.APPLICATION_XML_VALUE.equals(request.getContentType())) {
                return xmlParam(target);
            }
            // application/json  application/json;charset=UTF-8 n参数记录
            if (MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType()) || MediaType.APPLICATION_JSON_UTF8_VALUE.equals(request.getContentType())) {
                return target;
            }
        }
        return getByApplet(parameterNames, args);
    }

    /**
     * 获取程序参数
     *
     * @param parameterNames 参数名
     * @param args           参数值
     */
    private Object getByApplet(String[] parameterNames, Object[] args) {
        if (parameterNames == null || parameterNames.length == 0 || args == null || args.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterNames.length; i++) {
            sb.append(parameterNames[i]).append("=").append(args[i].toString()).append("&");
        }
        if (sb.lastIndexOf("&") != -1) {
            sb.deleteCharAt(sb.lastIndexOf("&"));
        }
        return sb.toString();
    }

    /**
     * 解析form 参数
     */
    private Object formParam(HttpServletRequest request) {
        StringBuilder args = new StringBuilder();
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) return null;
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length > 0) {
                for (String s : entry.getValue()) {
                    args.append(entry.getKey()).append("=").append(s).append("&");
                }
            }
        }
        if (args.lastIndexOf("&") != -1) {
            args.deleteCharAt(args.lastIndexOf("&"));
        }
        return args.toString();
    }

    /**
     * 解析XML 数据
     */
    private Object xmlParam(Object pointArgs) {
        try {
            StringWriter writer = new StringWriter();
            Marshaller marshaller = JAXBContext.newInstance(pointArgs.getClass()).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(pointArgs, writer);
            return writer.toString().replace("standalone=\"yes\"", "");
        } catch (JAXBException e) {
            log.warn("parse xml data exception : {}", e.getLinkedException().getMessage());
        }
        return pointArgs;
    }

}
