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
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
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
    private Environment environment;
    @Resource
    private BeanFactory beanFactory;
    private String appName = "undefined";

    @PostConstruct
    private void getApplicationName() {
        try {
            String name = environment.getProperty("spring.application.name");
            appName = StringUtils.isNotEmpty(name) ? name : "undefined";
        } catch (Exception ignore) {
            //ig
        }
    }

    @Resource
    public void setCollector(LogCollector collector) {
        this.collector = collector;
    }

    public Object proceed(Log4 log4, ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Log4a log4a = signature.getMethod().getAnnotation(Log4a.class);
        if (log4a == null) log4a = point.getTarget().getClass().getAnnotation(Log4a.class);
        if (log4a != null) {
            if (log4a.method()) log4.setMethod(signature.getDeclaringTypeName() + "#" + signature.getName());
            log4.setType(log4a.type());
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = null;
            HttpServletResponse response = null;
            if (attributes != null) {
                request = attributes.getRequest();
                response = attributes.getResponse();
                log4.setHost(request.getLocalAddr());
                log4.setPort(request.getLocalPort());
                log4.setAppName(appName);
                log4.setClientIp(HttpUtils.getIpAddress(request));
                log4.setReqUrl(request.getRequestURL().toString());
                log4.setHeaders(getHeadersMap(request, log4a.headers()));
            }
            if (log4a.args()) {
                log4.setArgs(getArgs(request, signature.getParameterNames(), point.getArgs()));
            }
            return proceed(log4a, log4, point, response);
        }
        return point.proceed();
    }

    /**
     * 方法执行
     */
    private Object proceed(Log4a log4a, Log4 log4, ProceedingJoinPoint point, HttpServletResponse response) throws Throwable {
        try {
            Object result = point.proceed();
            if (log4a.respBody()) {
                if (response != null && MediaType.APPLICATION_XML_VALUE.equals(response.getContentType())) {
                    log4.setRespBody(xmlParam(result));
                } else {
                    log4.setRespBody(result);
                }
            }
            log4.setSuccess(true);
            return result;
        } catch (Throwable throwable) {
            log4.setSuccess(false);
            if (log4a.stackTraceOnErr()) {
                try (StringWriter sw = new StringWriter(); PrintWriter writer = new PrintWriter(sw, true)) {
                    throwable.printStackTrace(writer);
                    Log4.step("Fail : \n" + sw.toString());
                }
            }
            throw throwable;
        } finally {
            if (log4a.costTime()) log4.toCostTime();
            Log4.setCurrent(log4);
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
        Class<? extends LogCollector> clz = log4a.collector();
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
            if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(request.getContentType())) {
                return formParam(request);
            }
            Object target;
            if (args.length == 1) target = args[0];
            else target = args;
            if (target == null) return null;
            if (MediaType.APPLICATION_XML_VALUE.equals(request.getContentType())) {
                return xmlParam(target);
            }
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
