package io.github.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author EalenXie create on 2020/8/28 13:36
 * LogData 参数抽取器
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogDataExtractor {

    private static final String AND_REG = "&";
    private static final String EQUALS_REG = "=";


    /**
     * 获取HttpServletRequest对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest();
        }
        return null;
    }

    /**
     * 获取HttpServletResponse对象
     */
    public static HttpServletResponse getResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getResponse();
        }
        return null;
    }


    /**
     * 获取请求参数内容
     *
     * @param parameterNames 参数名称列表
     * @param args           参数列表
     */
    public static Object getArgs(String[] parameterNames, Object[] args) {
        Object target;
        if (args.length == 1) target = args[0];
        else target = args;
        if (target == null) return null;
        HttpServletRequest request = getRequest();
        if (request != null && StringUtils.isNotEmpty(request.getContentType())) {
            String contentType = request.getContentType();
            if (MediaType.APPLICATION_XML_VALUE.equals(contentType)) {
                return xmlArgs(target);
            }
            if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(contentType) ||
                    MediaType.APPLICATION_JSON_VALUE.equals(contentType) ||
                    MediaType.APPLICATION_JSON_UTF8_VALUE.equals(contentType)) {
                return target;
            }
        }
        return appletArgs(parameterNames, args);
    }

    /**
     * 获取程序执行结果内容
     */
    public static Object getResult(Object resp) {
        HttpServletResponse response = getResponse();
        if (response != null && MediaType.APPLICATION_XML_VALUE.equals(response.getContentType()))
            return xmlArgs(resp);
        else return resp;
    }

    /**
     * 获取程序参数
     *
     * @param parameterNames 参数名
     * @param args           参数值
     */
    public static Object appletArgs(String[] parameterNames, Object[] args) {
        if (parameterNames == null || parameterNames.length == 0 || args == null || args.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterNames.length; i++) {
            sb.append(parameterNames[i]).append(EQUALS_REG).append(args[i].toString()).append(AND_REG);
        }
        if (sb.lastIndexOf(AND_REG) != -1) {
            sb.deleteCharAt(sb.lastIndexOf(AND_REG));
        }
        return sb.toString();
    }

    /**
     * 解析XML 数据
     */
    public static Object xmlArgs(Object pointArgs) {
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


    /**
     * 获取 HttpServletRequest 对象信息
     */
    public static void logHttpRequest(LogData data, String[] headers) {
        HttpServletRequest request = LogDataExtractor.getRequest();
        if (request != null) {
            data.setHost(request.getLocalAddr());
            data.setPort(request.getLocalPort());
            data.setClientIp(getIpAddress(request));
            data.setReqUrl(request.getRequestURL().toString());
            data.setHttpMethod(request.getMethod());
            Map<String, String> headersMap = new HashMap<>();
            for (String header : headers) {
                String value = request.getHeader(header);
                if (StringUtils.isNotEmpty(value)) {
                    headersMap.put(header, request.getHeader(header));
                }
            }
            data.setHeaders(headersMap);
        }
    }

    /**
     * 获取用户IP地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        String[] localhostIp = {"127.0.0.1", "0:0:0:0:0:0:0:1"};
        String ip = request.getRemoteAddr();
        for (String header : ipHeaders) {
            if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                break;
            }
            ip = request.getHeader(header);
        }
        for (String local : localhostIp) {
            if (StringUtils.isNotEmpty(ip) && ip.equals(local)) {
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    log.warn("Get host ip exception , UnknownHostException : {}", e.getMessage());
                }
                break;
            }
        }
        if (StringUtils.isNotEmpty(ip) && ip.length() > 15 && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(','));
        }
        return ip;
    }

}
