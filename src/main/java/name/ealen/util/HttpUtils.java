package name.ealen.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * Created by EalenXie on 2019/4/19 14:04.
 */
public class HttpUtils {

    private HttpUtils() {
    }

    /**
     * 获取请求网络IP
     */
    public static String getIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {"x-forwarded-for", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        String[] localhostIp = {"127.0.0.1", "0:0:0:0:0:0:0:1"};
        String ip = request.getRemoteAddr();
        for (String header : ipHeaders) {
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) break;
            ip = request.getHeader(header);
        }
        for (String local : localhostIp) {
            if (ip != null && ip.equals(local)) {
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException ignored) {

                }
                break;
            }
        }
        if (ip != null && ip.length() > 15 && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(','));
        }
        return ip;
    }


    /**
     * 检查IP格式是否合法
     */
    public static boolean ipIsValid(String ip) {
        if (ip == null) return false;
        String regex = "(" + "(2[0-4]\\d)" + "|(25[0-5])" + ")|(" + "1\\d{2}" + ")|(" + "[1-9]\\d" + ")|(" + "\\d" + ")";
        regex = "(" + regex + ").(" + regex + ").(" + regex + ").(" + regex + ")";
        return Pattern.compile(regex).matcher(ip).matches();
    }

    /**
     * 把ip转化为整数
     */
    public static long translateIP2Int(String ip) {
        String[] intArr = ip.split("\\.");
        int[] ipInt = new int[intArr.length];
        for (int i = 0; i < intArr.length; i++) {
            ipInt[i] = new Integer(intArr[i]);
        }
        return ipInt[0] * 256 * 256 * 256 + +ipInt[1] * 256 * 256 + ipInt[2] * 256 + ipInt[3];
    }


    /**
     * 根据Ip获取详细的地址信息，可能会出现null
     */
    public static String getAddressByIp(String ip) {
        if (ip == null) return null;
        HttpURLConnection connection = null;


        try{
            URL url = new URL("http://ip.taobao.com/service/getIpInfo.php?ip=" + ip);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            StringBuilder buffer = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            reader.close();
            return buffer.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) connection.disconnect();        // 关闭连接
        }
    }


    /**
     * 向指定URL发送GET方法的请求
     */
    public static String httpGet(String url, String param) throws IOException {
        if (url == null) return null;
        StringBuilder result = new StringBuilder();
        HttpURLConnection connection;
        URL realUrl = new URL(url + "?" + param);
        connection = (HttpURLConnection) realUrl.openConnection();
        connection.connect();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } finally {
            connection.disconnect();
        }
        return result.toString();
    }

}
