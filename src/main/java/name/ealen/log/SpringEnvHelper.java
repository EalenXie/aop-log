package name.ealen.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author EalenXie create on 2020/7/24 10:24
 */
@Component
public class SpringEnvHelper implements ApplicationContextAware {
    private static final String[] DEV = {"dev", "default"};
    private static final String[] SIT = {"sit", "test", "uat"};
    private static final String[] PROD = {"prod", "prd", "master"};
    private static String[] profiles;
    private static String appName;

    @Override
    public void setApplicationContext(@Autowired ApplicationContext applicationContext) {
        appName = applicationContext.getId();
        profiles = applicationContext.getEnvironment().getActiveProfiles();
        if (profiles.length == 0) profiles = applicationContext.getEnvironment().getDefaultProfiles();
        if (profiles.length == 0) profiles = DEV;
    }

    public static String getAppName() {
        return appName;
    }

    /**
     * 判断是否是开发环境
     */
    public static boolean isDev() {
        return isEnv(DEV);
    }

    /**
     * 判断是否是测试环境
     */
    public static boolean isSit() {
        return isEnv(SIT);
    }

    /**
     * 判断是否是生产环境
     */
    public static boolean isProd() {
        return isEnv(PROD);
    }

    /**
     * 判断环境
     */
    public static boolean isEnv(String... envKeys) {
        if (envKeys == null || envKeys.length == 0) return false;
        if (profiles == envKeys) return true;
        for (String profile : profiles) {
            for (String env : envKeys) {
                if (env.equalsIgnoreCase(profile)) return true;
            }
        }
        return false;
    }
}
