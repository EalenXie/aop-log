package name.ealen.util;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by EalenXie on 2018/9/7 18:40.
 */
public enum CommonUtil {

    ;

    /**
     * 获取请求 IP
     */
    public static String getIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        String ip = request.getRemoteAddr();
        for (String header : ipHeaders) {
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                break;
            }
            ip = request.getHeader(header);
        }
        return ip;
    }

    /**
     * 获取 yyyy-MM-dd HH:mm:ss 格式的当前时间
     */
    public static String getCurrentDateTime() {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        s.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return s.format(new Date());
    }
}
