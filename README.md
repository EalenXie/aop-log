AopLog
==================

#### AopLog是基于Spring Aop 和ThreadLocal实现的一个专门对请求方法内容日志的拦截与处理的日志工具包。

![](https://img.shields.io/static/v1?label=release&message=2.1&color=green)
![](https://img.shields.io/static/v1?label=jar&message=16k&color=green)
![](https://img.shields.io/badge/License-Apache%202.0-blue.svg)
![](https://img.shields.io/badge/JDK-1.8+-red.svg)

场景 : 
1. 我想知道一些重要的请求方法的请求参数,响应参数,请求头,以及耗时,方法是成功还是失败等等信息。
2. 普通的log.info或warn信息没有所属请求的上下关系,我不知道执行到哪一步发生了异常，并不方便查看和分析。
3. 正式环境中,我并不想打印太多无意义的info日志(有些只是为了排查问题打印的日志)，只希望在发生异常时记录日志。
4. 日志的收集,我希望将这些请求的日志记录下来，记录方式我自己决定，比如正常的日志打印，常见的日志写入数据库，日志写入到文件，日志入队列等等。
5. 整个日志的记录完全不干扰正常请求方法的流程,日志的收集处理异步化,不影响正常请求方法的性能与响应。
6. 不想日后每个项目工程都写一份这样的Aop拦截处理日志的代码。


### 快速开始  
#### 项目通过maven的pom.xml引入
```xml

<dependency>
    <groupId>com.github.ealenxie</groupId>
    <artifactId>aop-log</artifactId>
    <version>2.1</version>
</dependency>

```
#### 或者通过gradle引入
```gradle
compile group: 'com.github.ealenxie', name: 'aop-log', version: '2.1'
```



#### @AopLog注解使用，进行日志记录

直接在类(作用类的所有方法)或类方法(作用于方法)上加上注解@AopLog,进行日志记录

例如 : 

```java
import com.github.AopLog;
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
配置@Component的全局日志收集器只能配置一个。


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
| costTime | long | 整个方法耗时|
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

```
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

注意: 此方法如果不在被@AopLog注解的方法的整体调用链路中使用，则当前线程中的ThreadLocal中的LogData不会释放，需要手动调用LogData.removeCurrent();

此时再次接口调用 `/say/hello` 测试即可看看到控制台打印出结果，重点观察content字段 : 

```
2020-09-16 17:26:20.285  INFO 3284 --- [AsyncExecutor-2] name.ealen.infra.advice.AopLogCollector  : {"appName":"app-template","host":"127.0.0.1","port":8080,"clientIp":"192.168.110.1","reqUrl":"http://localhost:8080/app/sayHello","httpMethod":"GET","headers":{"User-Agent":"Apache-HttpClient/4.5.10 (Java/11.0.5)"},"type":"测试","content":"1. 第一步执行完成\n2. 第二步执行完成\n3. service的方法执行完成\n","method":"name.ealen.api.facade.AppController#sayHello","args":null,"respBody":{"code":"200","desc":"OK","message":"请求成功","dateTime":"2020-09-16 17:26:20","body":"hello EalenXie"},"logDate":1600248380283,"costTime":1,"threadName":"http-nio-8080-exec-2","threadId":32,"success":true}
```