Log4a
==================

#### 基于AOP和ThreadLocal实现的一个Http API 日志模块

在API每次被请求时,可以在整个方法调用链路中记录一条唯一的API请求日志,可以记录请求中的任意内容,比如传参,响应,请求url,method,clientIp,请求成功或异常,等等,以及HttpServletRequest中的任意内容。

实现的核心为AOP以及ThreadLocal。 AOP 会切所有被Log4a注解的方法,会记录一个线程中唯一一个Log4对象,抓取请求的内容和HttpServletRequest中的内容,并且可以完成自定义的日志收集(例如写入到数据库或者写入到文件),记录无论目标方法成功或失败,在执行完成后都将对ThreadLocal中的资源进行释放。

**Log4 记录的内容**

| 字段     | 类型          | 注释                            | 是否默认记录                                       |
| :------- | :------------ | :------------------------------ | -------------------------------------------------- |
| clientIp | String        | 请求客户端的Ip                  | 是                                                 |
| reqUrl   | String        | 请求地址                        | 是                                                 |
| headers  | Object        | 请求头部信息(可选择记录)        | 是,默认记录user-agent,content-type            	   |
| type     | String        | 操作类型                        | 是,默认值undefined                                 |
| content  | StringBuilder | 操作类型                        | 否,方法内容,可使用Log4.step进行内容步骤记录 |
| method   | String        | 请求的本地java方法              | 是                                                 |
| args     | Object        | 方法请求参数                    | 是                                                 |
| respBody | Object        | 方法响应参数                    | 是                                                 |
| costTime | Long          | 整个方法耗时                    | 是                                                 |
| logDate  | Long          | Log产生时间                     | 是,默认值是Log4对象初始化的时间               |
| success  | Boolean       | 执行状态,成功(true)/异常(false) | 是,默认false                                       |

**Log4a 注解选项说明**

| 选项       | 类型                          | 说明                                               | 默认                 |
| :--------- | :---------------------------- | :------------------------------------------------- | -------------------- |
| type       | String                        | 操作类型                                           | 默认值"undefined"    |
| method     | boolean                       | 是否记录请求的本地java方法                         | true                 |
| costTime   | boolean                       | 是否记录整个方法耗时                               | true                 |
| headers    | String[]                      | 记录的header信息                                   | 默认"User-Agent","content-type"     |
| args       | boolean                       | 是否记录请求参数                                   | true                |
| respBody   | boolean                       | 是否记录响应参数                                   | true                |
| stackTrace | boolean                       | 当目标方法发生异常时,是否追加异常堆栈信息到content | false                |
| costTime   | boolean                       | 是否记录整个方法耗时                               | true                 |
| collector  | Class<? extends LogCollector> | 指定日志收集器                                     | 默认空的收集器不指定 |


##### 例子使用说明

######  @Log4a注解使用

直接在Controller 方法或类上加上注解`@Log4a`,可以对该Controller中所有方法进行日志记录与收集

```java
@Log4a(type = "测试API", stackTrace = true, args = true, respBody = true)
@RestController
public class DemoController {
    @Resource
    private DemoService demoService;
    @PostMapping("/sayHello")
    public ResponseEntity sayHello(@RequestBody Map<String, Object> request) {
        demoService.sayHello(request);
        return ResponseEntity.ok(request);
    }
}
```

###### Log4.step 记录详细

这里调用了service方法,Log4.step 方法记录每一个步骤详细内容

```java
/**
 * @author EalenXie Created on 2020/1/16 10:49.
 */
@Service
@Slf4j
public class DemoService {
    /**
     * 测试方法, 使用Log4.step记录步骤
     */
    public void sayHello(Map<String, Object> words) {
        Log4.step("1. 请求来了,执行业务动作");
        log.info("do somethings");
        Log4.step("2. 业务动作执行完成");
    }
}
```

###### 自定义的全局日志收集器

本例中写了一个最简单的直接append写入到文件中，你可以选择自定义的方式进行日志收集

```java
@Component
public class DemoLogCollector implements LogCollector {
    @Override
    public void collect(Log4 log4) throws LogCollectException {
        try (FileWriter fw = new FileWriter("D:\\home\\temp\\日志.txt", true)) {
            fw.append(log4.toString());
        } catch (IOException e) {
            throw new LogCollectException(e);
        }
    }
}
```

测试后 , 可以从 日志.txt中获取到记录的日志内容。

```json
{
	"args": [{
		"id": 999,
		"value": "content"
	}],
	"clientIp": "192.168.1.54",
	"content": "1. 请求来了,执行业务动作\n2. 业务动作执行完成\n",
	"costTime": 2,
	"headers": {
		"User-Agent": "Apache-HttpClient/4.5.10 (Java/11.0.5)",
		"Content-Type": "application/json"
	},
	"logDate": 1592963836735,
	"method": "name.ealen.demo.cotroller.DemoController.sayHello",
	"reqUrl": "http://localhost:9527/sayHello",
	"respBody": {
		"headers": {},
		"statusCodeValue": 200,
		"body": {
			"id": 999,
			"value": "content"
		},
		"statusCode": "OK"
	},
	"success": true,
	"type": "测试API"
}
```













