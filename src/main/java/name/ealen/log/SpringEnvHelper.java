package name.ealen.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author EalenXie create on 2020/7/24 10:24
 */
@Slf4j
@Component
public class SpringEnvHelper {


    @Resource
    private Environment environment;


    private String activeProfiles = "default";

    private String appName = "undefined";

    @PostConstruct
    private void initialize() {
        appName = getEnvPropertySafely("spring.application.name");
        activeProfiles = getEnvPropertySafely("spring.profiles.active");
        if (StringUtils.isEmpty(activeProfiles)) activeProfiles = getEnvPropertySafely("spring.profiles.default");
    }

    public String getAppName() {
        return appName;
    }

    public String getProfilesActive() {
        return activeProfiles;
    }

    /**
     * 判断是否是开发环境
     */
    public boolean isDev() {
        return isDev("dev", "default");
    }

    /**
     * 判断是否是开发环境
     */
    public boolean isDev(String... envKeys) {
        return StringUtils.isEmpty(activeProfiles) || isEnv(envKeys);
    }

    /**
     * 判断是否是测试环境
     */
    public boolean isSit() {
        return isEnv("test", "sit", "uat");
    }

    /**
     * 判断是否是生产环境
     */
    public boolean isProd() {
        return isEnv("prod", "master");
    }


    public boolean isEnv(String... envKeys) {
        if (envKeys == null || envKeys.length == 0) return false;
        for (String env : envKeys) {
            if (env.equalsIgnoreCase(activeProfiles)) return true;
        }
        return false;
    }


    public String getEnvPropertySafely(String propertyName) {
        try {
            return environment.getProperty(propertyName);
        } catch (Exception ignore) {
            //ig
            return "";
        }
    }


}
