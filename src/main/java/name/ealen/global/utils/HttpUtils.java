package name.ealen.global.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author EalenXie Created on 2020/1/2 18:08.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static HttpServletRequest getNonNullHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) throw new IllegalStateException("获取不到当前ServletRequest");
        return attributes.getRequest();
    }

    /**
     * 从HttpServletRequest 获取 jsonString 格式的header信息
     */
    public static String getJsonHeaders(HttpServletRequest request, String... headers) {
        Map<String, String> headersMap = new ConcurrentHashMap<>();
        for (String header : headers) {
            String value = request.getHeader(header);
            if (StringUtils.isNotEmpty(value)) {
                headersMap.put(header, request.getHeader(header));
            }
        }
        return SerializeConvert.toJsonStringNoException(headersMap);
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