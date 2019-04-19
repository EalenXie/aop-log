package name.ealen.util;

import org.junit.Test;

/**
 * Created by EalenXie on 2019/4/19 14:07.
 */
public class HttpUtilsTest {


    @Test
    public void ipIsValid() {
        System.out.println(HttpUtils.ipIsValid("101.111."));
    }

    @Test
    public void translateIP2Int() {
        System.out.println(HttpUtils.translateIP2Int("192.168.11.1"));
    }


}
