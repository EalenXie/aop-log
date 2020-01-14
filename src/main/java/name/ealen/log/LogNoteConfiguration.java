package name.ealen.log;

import name.ealen.log.collector.LogCollector;
import name.ealen.log.collector.NothingCollector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author EalenXie Created on 2020/1/14 10:24.
 */
@Configuration
public class LogNoteConfiguration {


    /**
     * 默认配置一个空的收集器
     */
    @Bean
    @ConditionalOnMissingBean(value = LogCollector.class)
    public LogCollector nothingCollector() {
        return new NothingCollector();
    }

}
