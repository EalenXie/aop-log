AopLog
==================

#### AopLog是基于SpringAop和ThreadLocal实现的一个对请求方法埋点信息收集与处理的日志工具包。

![](https://img.shields.io/static/v1?label=release&message=2.5&color=green)
![](https://img.shields.io/static/v1?label=jar&message=24k&color=green)
![](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![](https://img.shields.io/badge/JDK-1.8+-red.svg)

设计目的和场景 :

- 使用Spring AOP拦截方法参数大部分做法基本上大同小异,不用每个项目工程都写AOP拦截处理日志的代码,引入此包即可。
- 可获取埋点方法的请求参数,响应参数,请求头,以及内部耗时,方法是成功还是失败，自定义步骤记录等等信息。
- 整个方法完整过程只产生一个埋点信息记录(一个LogData对象)，比如`@Controller`中一次完整的http请求。
- 收集情况可选，可只在异常时执行收集过程(有些只是为了排查问题打印的日志,程序正常运行时其实毫无意义)。
- 埋点信息收集，自行实现收集过程，比如埋点日志打印，常见埋点日志写入数据库，写入到文件，写入队列等等。
- 埋点信息收集不干扰埋点方法正常流程,收集过程异步化处理(默认,可通过注解的`asyncMode`进行设置),不影响正常请求方法的性能与响应。
- 只需通过`@AopLog`注解(或者自定义切面)决定是否埋点收集。

### 快速开始

#### 项目通过[Maven仓库地址](https://mvnrepository.com/artifact/com.github.ealenxie/aop-log/2.5) 的pom.xml引入。

```xml

<dependency>
    <groupId>com.github.ealenxie</groupId>
    <artifactId>aop-log</artifactId>
    <version>2.5</version>
</dependency>

```

#### 或者通过gradle引入

```gradle
compile group: 'com.github.ealenxie', name: 'aop-log', version: '2.5'
```

#### @AopLog注解使用，进行埋点收集

直接在类(作用类的所有方法)或类方法(作用于方法)上加上注解`@AopLog`,进行埋点收集

例如 :

```java

@AopLog(type = "测试接口", stackTraceOnErr = true)
@RestController
public class AppController {

    @GetMapping("/app/sayHello")
    public RespBody<String> sayHello() {
        return RespBody.ok("hello EalenXie");
    }

}

```

#### 自定义全局的日志收集器实现收集 LogCollector

例如只是简单打印,或写入到库等等。(例子只是提供参考说明,收集过程请不要完全照搬,没有考虑到LogData不能被序列化的情况)

```java

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.LogData;
import com.github.collector.LogCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author EalenXie create on 2020/9/15 13:46
 * 此为样例参考
 * 配置一个简单的日志收集器 这里只是做了一个log.info打印一下，可以在这里写入到数据库中或者写入
 */
@Slf4j
@Component
public class AopLogCollector implements LogCollector {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void collect(LogData logData) {
        try {
            log.info(objectMapper.writeValueAsString(logData));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
```

配置`@Component`的全局日志收集器只能配置一个。

接口调用 `/say/hello` 测试即可看看到控制台打印出结果 :

```
2020-09-16 16:01:04.782  INFO 2012 --- [AsyncExecutor-2] name.ealen.infra.advice.AopLogCollector  : {"appName":"app-template","host":"127.0.0.1","port":8080,"clientIp":"192.168.110.1","reqUrl":"http://localhost:8080/app/sayHello","httpMethod":"GET","headers":{"User-Agent":"Apache-HttpClient/4.5.10 (Java/11.0.5)"},"type":"测试","content":"","method":"name.ealen.api.facade.AppController#sayHello","args":null,"respBody":{"code":"200","desc":"OK","message":"请求成功","dateTime":"2020-09-16 16:01:04","body":"hello EalenXie"},"logDate":1600243264780,"costTime":1,"threadName":"http-nio-8080-exec-3","threadId":33,"success":true}
```

#### 埋点日志对象LogData属性说明

**LogData 埋点日志对象获取的内容**

| 字段         | 类型      | 注释                                        |
|:-----------|:--------|:------------------------------------------|
| appName    | String  | 应用名称                                      |
| host       | String  | 主机                                        |
| port       | int     | 端口号                                       |
| clientIp   | String  | 请求客户端的Ip                                  | 
| reqUrl     | String  | 请求地址                                      |
| headers    | Object  | 请求头部信息(可选择获取) 默认获取user-agent,content-type |
| tag        | String  | 操作标签,默认值undefined                         | 
| content    | String  | 方法步骤内容,默认是空,可使用LogData.step进行内容步骤记录       |
| method     | String  | 请求的本地java方法                               | 
| args       | Object  | 方法请求参数                                    |
| respBody   | Object  | 方法响应参数                                    |
| costTime   | long    | 整个方法内部耗时                                  |
| logDate    | Date    | Log产生时间,LogData对象初始化的时间                   |
| threadName | String  | 线程名称                                      |
| threadId   | long    | 线程Id                                      |
| success    | boolean | 执行状态,成功(true)/异常(false)                   | 

#### AopLog 注解选项说明

| 选项              | 类型                            | 说明                                     | 默认                            |
|:----------------|:------------------------------|:---------------------------------------|-------------------------------|
| logOnErr        | boolean                       | 仅当发生异常时才收集                             | false                         |
| tag             | String                        | 操作标签                                   | 默认值"undefined"                |
| headers         | String[]                      | 获取的header信息 ,选择要获取哪些header信息           | 默认"User-Agent","content-type" |
| args            | boolean                       | 是否获取请求参数                               | true                          |
| respBody        | boolean                       | 是否获取响应参数                               | true                          |
| stackTraceOnErr | boolean                       | 当目标方法发生异常时,是否追加异常堆栈信息到LogData的content中 | false                         |
| asyncMode       | boolean                       | 异步方式收集                                 | true                          |
| collector       | Class<? extends LogCollector> | 指定日志收集器                                | 默认不调整收集器,使用全局的日志收集器           |

#### LogData的step方法。

记录步骤。(如果某些重要步骤希望被记录下来)
例如 :

```java

import com.github.AopLog;
import com.github.LogData;
import name.ealen.infra.base.resp.RespBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author EalenXie create on 2020/6/22 14:28
 */
@AopLog(tag = "测试", stackTraceOnErr = true)
@RestController
public class AppController {


    @GetMapping("/app/sayHello")
    public RespBody<String> sayHello() {
        LogData.step("1. 第一步执行完成");
        //......
        LogData.step("2. 第二步执行完成");
        //.....
        LogData.step("3. service的方法执行完成");
        //.....
        return RespBody.ok("hello EalenXie");
    }

}

```

此时再次接口调用 `/say/hello` 测试即可看看到控制台打印出结果，重点观察content字段 :

```
2020-09-16 17:26:20.285  INFO 3284 --- [AsyncExecutor-2] name.ealen.infra.advice.AopLogCollector  : {"appName":"app-template","host":"127.0.0.1","port":8080,"clientIp":"192.168.110.1","reqUrl":"http://localhost:8080/app/sayHello","httpMethod":"GET","headers":{"User-Agent":"Apache-HttpClient/4.5.10 (Java/11.0.5)"},"tag":"测试","content":"1. 第一步执行完成\n2. 第二步执行完成\n3. service的方法执行完成\n","method":"name.ealen.api.facade.AppController#sayHello","args":null,"respBody":{"code":"200","desc":"OK","message":"请求成功","dateTime":"2020-09-16 17:26:20","body":"hello EalenXie"},"logDate":1600248380283,"costTime":1,"threadName":"http-nio-8080-exec-2","threadId":32,"success":true}
```

```
"content":"1. 第一步执行完成\n2. 第二步执行完成\n3. service的方法执行完成\n"
```

#### 不通过@AopLog注解,通过自定义切面进行收集

自定义切面注入`AopLogProcessor`,调用`proceed(config, point)`即可

```java


import com.github.AopLogConfig;
import com.github.AopLogProcessor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by EalenXie on 2021/7/14 10:29
 * 自定义切面
 */
@Aspect
@Component
public class CustomLogDataAspect {

    @Resource
    private AopLogProcessor aopLogProcessor;
    private static final AopLogConfig CONFIG;

    static {
        CONFIG = new AopLogConfig();
        CONFIG.setTag("操作标签");
        CONFIG.setStackTraceOnErr(false);
        CONFIG.setHeaders(new String[]{"content-type", "user-agent"});
    }

    // 自定义切点 execution(public * com.test.web.TestController.*(..))
    @Pointcut("execution(public * com.test.web.TestController.*(..))")
    public void test() {
        //ig
    }

    // 请使用环绕通知 @Around()
    @Around("test()")
    public Object note(ProceedingJoinPoint point) throws Throwable {
        return aopLogProcessor.proceed(CONFIG, point);
    }
}

```

#### Change Notes:

有关更改的详细信息，请参阅[发布说明](https://github.com/EalenXie/aop-log/releases)。
