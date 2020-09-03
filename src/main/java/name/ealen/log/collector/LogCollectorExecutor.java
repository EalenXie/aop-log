package name.ealen.log.collector;

import name.ealen.log.LogData;
import name.ealen.log.collector.LogCollector;
import name.ealen.log.collector.NothingCollector;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author EalenXie create on 2020/9/3 13:29
 */
@Component
public class LogCollectorExecutor {

    @Resource
    private LogCollector collector;

    private Map<Class<? extends LogCollector>, LogCollector> collectors = new HashMap<>();

    private ApplicationContext applicationContext;

    public LogCollectorExecutor(@Autowired ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 日志收集
     *
     * @param clz     日志收集Class对象
     * @param logData 日志数据
     */
    @Async
    public void execute(Class<? extends LogCollector> clz, LogData logData) {
        if (clz != NothingCollector.class) {
            LogCollector c;
            try {
                c = applicationContext.getBean(clz);
            } catch (Exception e) {
                c = collectors.get(clz);
                if (c == null) {
                    c = BeanUtils.instantiateClass(clz);
                    collectors.put(clz, c);
                }
            }
            c.collect(logData);
        } else {
            collector.collect(logData);
        }
    }


}
