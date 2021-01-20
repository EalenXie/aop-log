AopLog
==================

#### AopLog是基于SpringAop和ThreadLocal实现的一个对请求方法埋点记录与处理的日志工具包。

![](https://img.shields.io/static/v1?label=release&message=2.4&color=green)
![](https://img.shields.io/static/v1?label=jar&message=24k&color=green)
![](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![](https://img.shields.io/badge/JDK-1.8+-red.svg)

设计目的和场景 : 

1. 使用Spring Aop拦截参数日志目前大部分做法都基本上大同小异,不想日后每个项目工程都写一份这样的Aop拦截处理日志的代码,甚至代码侵入。
2. 重要的业务接口埋点,我想知道一些相对重要的请求方法的请求参数,响应参数,请求头,以及内部耗时,方法是成功还是失败等等信息。发生错误时我也不知道执行到哪一步发生了异常，是不是某个参数导致出的逻辑问题。
3. 普通的log.info或warn信息没有所属请求的上下关系,并不方便查看和分析。
4. 正式环境中,我并不想打印太多无意义的info日志(有些只是为了排查问题打印的日志,程序正常运行时其实毫无意义)，只希望在发生异常时记录日志或者只希望每次请求只记录一条次关键的请求信息。
5. 日志的收集,我希望将这些请求的埋点信息记录下来，记录的实现方式我自己决定，比如埋点日志打印，常见埋点日志写入数据库，写入到文件，写入队列等等。
6. 整个埋点日志的记录完全不干扰正常请求方法的流程,收集处理过程异步化,完全不影响正常请求方法的性能与响应。
7. 只需要通过`@AopLog`注解决定是否埋点记录。


### 快速开始  
#### 项目通过maven的pom.xml引入
```xml

<dependency>
    <groupId>com.github.ealenxie</groupId>
    <artifactId>aop-log</artifactId>
    <version>2.4</version>
</dependency>

```
#### 或者通过gradle引入
```gradle
compile group: 'com.github.ealenxie', name: 'aop-log', version: '2.4'
```



#### @AopLog注解使用，进行日志记录

直接在类(作用类的所有方法)或类方法(作用于方法)上加上注解`@AopLog`,进行日志记录

例如 : 

```java

@AopLog(type = "测试接口",stackTraceOnErr = true)
@RestController
public class AppController {

    @GetMapping("/app/sayHello")
    public RespBody<String> sayHello() {
        return RespBody.ok("hello EalenXie");
    }

}

```

#### 自定义全局的日志收集器实现收集 LogCollector

例如只是简单打印,或写入到库等等。

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

#### 记录的日志对象LogData属性说明
**LogData 记录的内容**

| 字段 | 类型  | 注释 |
| :------- | :------------ | :------------------------------ | 
| appName | String | 应用名称|
| host | String | 主机  |
| port | int | 端口号  |
| clientIp | String  | 请求客户端的Ip       | 
| reqUrl   | String  | 请求地址 |
| headers  | Object | 请求头部信息(可选择记录) 默认记录user-agent,content-type |
| type | String  | 操作类型,默认值undefined | 
| content | String | 方法步骤内容,默认是空,可使用LogData.step进行内容步骤记录
| method  | String | 请求的本地java方法  | 
| args     | Object | 方法请求参数  |
| respBody | Object | 方法响应参数  |
| costTime | long | 整个方法内部耗时|
| logDate  | Date | Log产生时间,LogData对象初始化的时间 |
| threadName  | String | 线程名称|
| threadId  | long | 线程Id |
| success  | boolean | 执行状态,成功(true)/异常(false) | 


#### AopLog 注解选项说明

| 选项       | 类型                          | 说明                                               | 默认                 |
| :--------- | :---------------------------- | :------------------------------------------------- | -------------------- |
| logOnErr    | boolean               | 仅当发生异常时才记录收集                                    | false   |
| type       | String                        | 操作类型                                           | 默认值"undefined"    |
| headers    | String[]                      | 记录的header信息 ,选择要记录哪些header信息| 默认"User-Agent","content-type"     |
| args       | boolean                       | 是否记录请求参数                                   | true                |
| respBody   | boolean                       | 是否记录响应参数                                   | true                |
| stackTraceOnErr | boolean                       | 当目标方法发生异常时,是否追加异常堆栈信息到LogData的content中 | false                |
| asyncMode | boolean                       |  异步方式收集 | true                |
| collector  | Class<? extends LogCollector> | 指定日志收集器                                     | 默认不调整收集器,使用全局的日志收集器 |


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
@AopLog(type = "测试",stackTraceOnErr = true)
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
2020-09-16 17:26:20.285  INFO 3284 --- [AsyncExecutor-2] name.ealen.infra.advice.AopLogCollector  : {"appName":"app-template","host":"127.0.0.1","port":8080,"clientIp":"192.168.110.1","reqUrl":"http://localhost:8080/app/sayHello","httpMethod":"GET","headers":{"User-Agent":"Apache-HttpClient/4.5.10 (Java/11.0.5)"},"type":"测试","content":"1. 第一步执行完成\n2. 第二步执行完成\n3. service的方法执行完成\n","method":"name.ealen.api.facade.AppController#sayHello","args":null,"respBody":{"code":"200","desc":"OK","message":"请求成功","dateTime":"2020-09-16 17:26:20","body":"hello EalenXie"},"logDate":1600248380283,"costTime":1,"threadName":"http-nio-8080-exec-2","threadId":32,"success":true}
```
```
"content":"1. 第一步执行完成\n2. 第二步执行完成\n3. service的方法执行完成\n"
```

#### Change Notes:

有关更改的详细信息，请参阅[发布说明](https://github.com/EalenXie/aop-log/releases)。