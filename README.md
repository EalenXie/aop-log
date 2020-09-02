AopLog
==================

#### 基于AOP和ThreadLocal实现的一个AOP 日志模块

在API每次被请求时,可以在整个方法调用链路中记录一条唯一的API请求日志,可以记录请求中的任意内容,比如传参,响应,请求url,method,clientIp,请求成功或异常,等等,以及HttpServletRequest中的任意内容。

实现的核心为AOP以及ThreadLocal。 AOP 会切所有被AopLog注解的方法,会记录一个线程中唯一一个LogData(日志数据)对象,抓取请求的内容和HttpServletRequest中的内容,并且可以完成自定义的日志收集(例如写入到数据库或者写入到文件),记录无论目标方法成功或失败,在执行完成后都将对ThreadLocal中的资源进行释放。

**LogData 记录的内容**

| 字段     | 类型          | 注释                            | 是否默认记录                                       |
| :------- | :------------ | :------------------------------ | -------------------------------------------------- |
| appName | String        | 应用名称                  | 是                                                 |
| host | String        | 主机                  | 是                                                 |
| port | int        | 端口号                  | 是                                                 |
| clientIp | String        | 请求客户端的Ip                  | 是                                                 |
| reqUrl   | String        | 请求地址                        | 是                                                 |
| headers  | Object        | 请求头部信息(可选择记录)        | 是,默认记录user-agent,content-type            	   |
| type     | String        | 操作类型                        | 是,默认值undefined                                 |
| content  | StringBuilder | 操作类型                        | 否,方法内容,可使用LogData.step进行内容步骤记录 |
| method   | String        | 请求的本地java方法              | 是                                                 |
| args     | Object        | 方法请求参数                    | 是                                                 |
| respBody | Object        | 方法响应参数                    | 是                                                 |
| costTime | Long          | 整个方法耗时                    | 是                                                 |
| logDate  | Long          | Log产生时间                     | 是,默认值是LogData对象初始化的时间               |
| threadName  | String      | 线程名称                     | 是               |
| threadId  | Long          | 线程Id                     | 是               |
| success  | Boolean       | 执行状态,成功(true)/异常(false) | 是,默认false                                       |

**AopLog 注解选项说明**

| 选项       | 类型                          | 说明                                               | 默认                 |
| :--------- | :---------------------------- | :------------------------------------------------- | -------------------- |
| logOnErr    | boolean               | 仅当发生异常时才记录                                    | false   |
| type       | String                        | 操作类型                                           | 默认值"undefined"    |
| headers    | String[]                      | 记录的header信息                                   | 默认"User-Agent","content-type"     |
| args       | boolean                       | 是否记录请求参数                                   | true                |
| respBody   | boolean                       | 是否记录响应参数                                   | true                |
| stackTraceOnErr | boolean                       | 当目标方法发生异常时,是否追加异常堆栈信息到content | false                |
| collector  | Class<? extends LogCollector> | 指定日志收集器                                     | 默认空的收集器不指定 |


##### 例子使用说明

######  @AopLog注解使用

直接在Controller 方法或类上加上注解`@AopLog`,可以对该Controller中所有方法进行日志记录与收集

```java
@AopLog(type = "测试API", stackTraceOnErr = true)
@RestController
public class DemoController {
    @Resource
    private DemoService demoService;
    /**
     * JSON数据测试
     */
    @PostMapping("/sayHello")
    public ResponseEntity<?> sayHello(@RequestBody Map<String, Object> request) {
        demoService.sayHello(request);
        return ResponseEntity.ok(request);
    }
    /**
     * RequestParam 参数测试
     */
    @PostMapping("/params")
    public ResponseEntity<?> params(@RequestParam Integer a) {
        return ResponseEntity.ok(a);
    }
    /**
     * 无参测试
     */
    @GetMapping("/noArgs")
    public ResponseEntity<?> noArgs() {
        return ResponseEntity.ok().build();
    }
    /**
     * XML 格式数据测试
     */
    @PostMapping(value = "/callXml", consumes = {MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> callXml(@RequestBody XmlDataDTO dataDTO) {
        return ResponseEntity.ok(dataDTO);
    }
    /**
     * 特殊对象测试
     */
    @GetMapping("/callHttpServletRequest")
    public ResponseEntity<?> callHttpServletRequest(HttpServletRequest request) {
        return ResponseEntity.ok().build();
    }

}
```

###### LogData.step 记录详细

这里调用了service方法,LogData.step 方法记录每一个步骤详细内容

```java
/**
 * @author EalenXie Created on 2020/1/16 10:49.
 */
@Service
@Slf4j
public class DemoService {
    /**
     * 测试方法, 使用LogData.step记录步骤
     */
    public void sayHello(Map<String, Object> words) {
        LogData.step("1. 请求来了,执行业务动作");
        log.info("do somethings");
        LogData.step("2. 业务动作执行完成");
    }
}
```

###### 自定义的全局日志收集器

本例中写了一个最简单的直接append写入到文件中，你可以选择自定义的方式进行日志收集

```java

/**
 * @author EalenXie Created on 2020/1/10 17:53.
 * 配置一个全局的日志收集器
 * 这里只是做一个简单示例进行说明,请勿使用该收集方法到生产中
 * 本例直接将日志append到文件中
 */
@Component
public class DemoLogCollector implements LogCollector {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void collect(LogData data) throws LogCollectException {
        try {
            File file = new File("D:\\home\\temp\\日志.txt");
            if (!file.getParentFile().exists()) {
                FileUtils.forceMkdir(file.getParentFile());
            }
            try (FileWriter fw = new FileWriter(file, true)) {
                fw.append(mapper.writeValueAsString(data));
            }
        } catch (IOException e) {
            throw new LogCollectException(e);
        }
    }
}
```

测试后 , 可以从 D:\\home\\temp\\日志.txt中获取到记录的日志内容。

json格式的数据记录(参数JSON): 
```json
{
	"appName": "aop-log",
	"host": "127.0.0.1",
	"port": 9527,
	"clientIp": "192.168.1.54",
	"reqUrl": "http://localhost:9527/sayHello",
	"headers": {
		"User-Agent": "Apache-HttpClient/4.5.10 (Java/11.0.5)",
		"Content-Type": "application/json"
	},
	"type": "测试API",
	"content": "1. 请求来了,执行业务动作\n2. 业务动作执行完成\n",
	"method": "name.ealen.demo.controller.DemoController#sayHello",
	"args": {
		"id": 999,
		"value": "content"
	},
	"respBody": {
		"headers": {},
		"body": {
			"id": 999,
			"value": "content"
		},
		"statusCodeValue": 200,
		"statusCode": "OK"
	},
	"logDate": "2020-07-21 08:00:23",
	"costTime": 8,
	"threadName": "http-nio-9527-exec-2",
	"threadId": 38,
	"success": true
}
```

xml格式的数据(参数XML): 
```json
{
	"appName": "aop-log",
	"host": "127.0.0.1",
	"port": 9527,
	"clientIp": "192.168.1.54",
	"reqUrl": "http://localhost:9527/callXml",
	"headers": {
		"User-Agent": "Apache-HttpClient/4.5.10 (Java/11.0.5)",
		"Content-Type": "application/xml"
	},
	"type": "测试API",
	"content": "",
	"method": "name.ealen.demo.controller.DemoController#callXml",
	"args": "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><xml><message>1111 </message><username>zhangsan</username></xml>",
	"respBody": {
		"message": "1111 ",
		"username": "zhangsan"
	},
	"logDate": "2020-07-21 08:01:54",
	"costTime": 3,
	"threadName": "http-nio-9527-exec-5",
	"threadId": 41,
	"success": true
}
```

特殊参数格式(键值对形式,参数默认取对象的toString()方法):
```json
{
	"appName": "aop-log",
	"host": "127.0.0.1",
	"port": 9527,
	"clientIp": "192.168.1.54",
	"reqUrl": "http://localhost:9527/callHttpServletRequest",
	"headers": {
		"User-Agent": "Apache-HttpClient/4.5.10 (Java/11.0.5)"
	},
	"type": "测试API",
	"content": "",
	"method": "name.ealen.demo.controller.DemoController#callHttpServletRequest",
	"args": "request=org.apache.catalina.connector.RequestFacade@19d433f",
	"respBody": {
		"headers": {},
		"body": null,
		"statusCodeValue": 200,
		"statusCode": "OK"
	},
	"logDate": "2020-07-21 08:02:53",
	"costTime": 1,
	"threadName": "http-nio-9527-exec-7",
	"threadId": 43,
	"success": true
}
```




