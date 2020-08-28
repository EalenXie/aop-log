package name.ealen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Created by EalenXie on 2018/8/28 15:26.
 */
@EnableAsync
@SpringBootApplication
public class Log4aApplication {

    public static void main(String[] args) {
        SpringApplication.run(Log4aApplication.class, args);
    }
}
