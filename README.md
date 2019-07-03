# Concrete

[concrete.coodex.org](https://concrete.coodex.org)

> [![](jetbrains.svg)](https://www.jetbrains.com)感谢[JetBrains](https://www.jetbrains.com/?from=Concrete)提供IDE工具开源授权。

## 什么鬼？

> Concrete是一种基于Java的服务定义规范


```java
@ConcreteService
public interface SomeService {
    
    @AccessAllow
    String someMethod();
    
}
```
## 干哈滴？

> 致力于让研发者将精力尽可能多的投入到需求分析、业务逻辑和用户体验上

## 怎么搞？

看[书](https://concrete.coodex.org)，多练

## 2019-07-03

- 修复MockInterceptor依赖Bean的问题
- AMQPApplication增加构造参数，用以保证请求可达性
    - exchangeName: 之前就有，指使用哪个交换机，为空则表示使用默认交换机`AMQP-PROVIDER`
    - queueName: 新增，若指定队列名，则表示是持久化队列
    - ttl: 新增，当队列为持久化队列时，指定消息过期时间

## 2019-07-02

- coodex-utilities: Profile，增加对YAML支持
    - 加载了`snakeyaml`则自动支持
    - Profile.getInstance(String resourceKey)接口中
        - resourceKey 以 `.properties`结尾时，则使用properties文件，找不到则所有键值为空
        - resourceKey 以 `.yaml`结尾时，则使用yaml文件，找不到则所有键值为空
        - 上述以外，则按照优先级选择，支持了yaml的，则以yaml优先，否则找properties
- concrete
    - 增加TranslateService，解构I18N与resourceBundle的依赖，默认使用ResourceBundle实现
    - 增加I18NFacade门面，对外提供translate服务
    - 消息模板统一到I18NFacade上

## 2019-07-01

- 调整ConcreteService模拟数据的方式
    - 增加服务端拦截器：MockInterceptor
    - 增加MockValue注解，用来指定模拟的值，`json`属性以`.json`结尾时，说明通过json文件加载
    - 原随机模拟的方式依然支持
    

## 2019-06-27

- Warning增加data属性，用来传递提醒的业务数据
- java client端支持Warning（dubbo除外，因为服务端尚未支持，随新的dubbo支持开发提供）
- jaxrs angular code v2支持Warning

## 2019-06-26

- 设计[concrete for product](01.spec/concrete-prod-spec/README.md)，尚未启用
- Subjoin增加Warning相关操作接口，支持Warning随Subjoin返回调用方
- TODO：java client端支持、jaxrs angular code v2支持、jaxrs axios 支持、jaxrs jquery 支持
- TODO：websocket angular支持、websocket jquery 支持

## 2019-06-25

- `org.coodex.concrete.api.Application` 的 `registerPackage` 和 `API.generate` 及 `API.generateFor` 支持通配符。一个`*`表示一级包，两个`*`表示一级或多级

## 2019-06-21

- Client方式本地调用时也调用切片
- concrete-core-spring增加自动检索的包`org.coodex.concrete.**.injectable`
- 增加concrete-s2-api/impl模块

## 2019-06-20

- 【merge from 0.2.4-SNAPSHOT】angular代码生成(v2),默认rxjs版本为6,严格按照6规范使用

## 2019-06-19

- 服务端AOP不再依赖DI容器
- 废弃运行期服务上下文，所有的设置均遵循服务定义
- 切换到spring boot 2后，默认mysql的jdbc版本为8，增加concrete-apm-plugin-mysql8进行适配

## 2019-06-18

- 移除Abstract/MicroService/NotService注解，移除ConcreteService接口，增加ConcreteService注解，在原MicroService能力基础上增加了Abstract和NotService的能力
- 【merge from 0.2.4-SNAPSHOT】angualr代码生成：export runtimeContext对象，可由开发者修改运行时变量

## 2019-06-17

- 建立0.3.x分支，准备开始0.3.0
- 默认实现升级依赖，spring boot 2; spring framework 5; spring data Lovelace-SR8
- 移除声明作废的代码
- 0.3.x目标
    - 接口不再需要继承ConcreteService，而是通过装饰模式指定
    - 动态代理来做aop，不再依赖容器
    - 结构不良的优化


## 2019-06-15

- Destination 增加timeout属性

## 2019-06-12

- 设计支持基于amqp发布服务
    - 重构websocket模块，抽象出自有的数据包处理、服务调用，amqp以及以后其他的支持都将通过类似模式扩展
    - 设计concrete-client的Destination，各模块自行定义各自的Destination，不再局限于Config中获取，为下一步动态客户端提供支持
    - 增加concrete-support-amqp模块，提供基于amqp发布服务的能力
        - AMQPApplication，使用AMQPConnectionConfig和exchangeName构建
    - 增加concrete-client-amqp模块，提供rpc的调用能力
        - location: amqp{s}{://{{username}:{password}@}host{:port}/{virtualHost}}
        - amqp.username
        - amqp.password
        - amqp.host
        - amqp.port
        - amqp.virtualHost
        - amqp.exchange: 默认AMQP_PROVIDER
- coodex-utilities:
    - Profile增加placeHolder机制，${namespace:key},namespace:可为空，为空时对应当前资源文件的key值。

## 2019-03-28

- coodex-utilities: 连击和防反跳机制自带线程池调整，由每个实例一个调整为公用，使用系统属性`coalition.executors.size`进行配置，默认为3

## 2019-03-28

- coodex-utilities: ExecutorsHelper相关接口可以指定线程池名称
- concrete调用端相应更新

## 2019-01-29

- 增加中心时光机服务，达到多虚拟机之间采用相同的基准进行时间加速
    - `org.coodex.util.clock.ClockAgentService`
        - `start()`，启动服务
        - `shutdown()`，终止服务
- 增加基于中心时间服务的时光机
    - 使用时在SPI中为 `org.coodex.util.clock.ClockAgent` 注册`org.coodex.util.clock.RemoteClockAgent`
    - 参数：`org.coodex.util.Clock.remoteHost`和`org.coodex.util.Clock.remotePort`, Config > System.property


## 2019-01-28

- SharedCacheToken增加防反跳，降低io
- 增加时光机，手动滑稽
    - 当前版本可以设置时刻起点（默认为当前虚拟机启动时间），可以设置时间流逝倍率（默认1倍），可扩展基于中心时光机的实现
        - 假设设置为时间流逝倍率为300倍，则，机器时间上每1秒钟等于时光机中的300秒
    - `org.coodex.util.Clock` 提供获取“当前”时刻的两个接口
        - `currentTimeMillis()` 用来替代 `System.currentTimeMillis()`
        - `getCalendar()` 用来替换 `Calendar.getInstance()`
    - `org.coodex.util.Clock` 提供两个接口来替代常用的线程休眠接口
        - `sleep(long)` 用来替代 `Thread.sleep(long)`
        - `objWait(Object,long)` 用来替代 `Object.wait(long)`
    - `org.coodex.concurrent.ExecutorHelper` 对`ScheduledExecutorService`做了包装，日程类方法支持时光机，如上例，计划300秒后执行，实际上会在1秒后执行

```java
package org.coodex.util;


import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.clock.DefaultClockAgent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClockTest {

    private static ScheduledExecutorService scheduledExecutorService = ExecutorsHelper.newScheduledThreadPool(5);

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private static void poll(final CountDownLatch countDownLatch) {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(Common.now("yyyy-MM-dd HH:mm:ss.SSS"));
                countDownLatch.countDown();
            }
        }, 0,1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws InterruptedException {
        System.setProperty(Clock.KEY_MAGNIFICATION, Float.toString(300f));
        System.setProperty(DefaultClockAgent.KEY_BASELINE, "2019-01-28 12:00:00");
        CountDownLatch countDownLatch = new CountDownLatch(20);
        poll(countDownLatch);
        countDownLatch.await();
        ExecutorsHelper.shutdownAll();
    }
}
```

## 2019-01-08

- jaxrs部分，原简单类型默认作为path参数传递，修改为：0.2.4-SNAPSHOT默认行为依然如此，0.2.4发布后默认行为更新为默认使用body传递，
除非明确定义了在path中传递
    - jaxrs.style.before.024
        - true/false
        - System.property 高于 Config.get
        - 0.2.4-SNAPSHOT默认为true，0.2.4发布后默认为false
        - 之后更新不一定保留此参数
    - 更新axios/angular的代码生成器
- 扩展生成API接口，支持扩展属性
    - 生成JaxRS.code.angular.ts.v2时，需要指定rxjs的版本
        - ext={'rxjsVersion':6}
- 修复基于Profile的Configuration命名空间为空时获取不到数据的问题

- 二更，快照版起默认使用新风格

## 2019-01-03
- 修复JaxrsSubjoin可能出现content-type重复的问题
- 定义Configuration接口，用于获取系统的配置；提供默认的基于Profile的Configuration实现；提供Config门面简化获取配置方式
- coodex及concrete配置类内容迁移到Configuration接口，兼容了之前从Profile中获取的行为
- 线程池由ConcreteHelper统一提供，可通过配置灵活设定
- 其他一些优化

### Configuration & Profile

#### coodex-utilities

configuration:

| namespace | key | default | desc |
| --- | ---- | ----- | --- |
| mock | default.chars.range | ASCII | char seq |

#### shared-cache-jedis

| namespace | key | default | desc |
| --- | ---- | ---- | --- |
| sharedcache-jedis | redisServers | none | split by ',' |
| sharedcache-jedis | pool.minIdle | 0 | integer |
| sharedcache-jedis | pool.maxIdle | 8 | integer |
| sharedcache-jedis | pool.maxTotal | 8 | integer |
| sharedcache-jedis | defaultMaxCacheTime | 3600 | long, sec |

#### shared-cache-memcached

| namespace | key | default | desc |
| --- | ---- | ---- | --- |
| sharedcache-xmemcached | defaultMaxCacheTime | 3600 | long, sec |
| sharedcache-xmemcached | memchachedServers | none | split by ' ' |
| sharedcache-xmemcached | poolSize | 1 | integer |
| sharedcache-xmemcached | user.(server) | none | - |
| sharedcache-xmemcached | pwd.(server) | none | - |

#### coodex-utilities-servlet

| namespace | key | default | desc |
| --- | ---- | ---- | --- |
| cors_settings | allowOrigin | none | - |
| cors_settings | exposeHeaders | none | - |
| cors_settings | allowMethod | none | - |
| cors_settings | allowHeaders | none | - |
| cors_settings | maxAge | none | - |
| cors_settings | allowCredentials | false | - |

#### concrete

- configuration

| namespace | key | default | desc |
| --- | ---- | ---- | --- |
| (executorName), (appSet) | executor | none | alias to |
| (executorName), (appSet) | executor.corePoolSize | 0 | integer |
| (executorName), (appSet) | executor.maximumPoolSize | Integer.MAX_VALUE | integer |
| (executorName), (appSet) | executor.keepAliveTime | 60 | integer, min |
| (schedulerName), (appSet) | scheduler | none | alias to |
| (schedulerName), (appSet) | scheduler.executorSize | 1 | integer |
| (appSet) | token.maxIdelTime | 60 | integer, min |
| concrete, (appSet) | api.packages | String[0] | String[] |
| concrete, (appSet) | remoteapi.packages | String[0] | String[] |
| (appSet) | (BeanNameFilter.class.name).prefix | none | className |
| (appSet) | (ServiceProvider.class.name).provider | none | className |
| (appSet) | (ServiceProvider.class.name).default | none | className |
| (appSet) | (ConflictSolution.class.name) | none | className |
| - | cache.thread.pool.size | 1 | integer, deprecated |
| - | cache.object.life | 10 | integer, min, deprecated |
| - | concrete.client(.domainKey).async | true | deprecated |
| - | concrete.client(.domainKey).type | - | deprecated |
| - | concrete.client(.domainKey).domain | - | deprecated |
| (appSet) | tokenBasedTopicMessage.cacheLife | 30 | integer, sec |
| (appSet) | queue.default | none | queueName |
| (appSet) | messagePattern.resourceBundles | messagePattern | - |
| (appSet) | tokenCacheType | none | - |
| (appSet) | aspect.bean.validation | true | - |
| signature, (module), (appSet) | algorithm | none | - |
| signature, (module), (appSet) | keyId | none | - |
| signature, (module), (appSet) | sign | none | - |
| signature, (module), (appSet) | noise | none | - |
| signature, (appSet) | property.algorithm | algorithm | - |
| signature, (appSet) | property.keyId | keyId | - |
| signature, (appSet) | property.sign | sign | - |
| signature, (appSet) | property.noise | noise | - |
| signature, (module), (appSet) | hmacKey(.paperName)(.keyId) | none | - |
| signature, (module), (appSet) | rsa.privateKey(.paperName)(.keyId) | none | - |
| signature, (module), (appSet) | rsa.publicKey(.paperName)(.keyId) | none | - |
| (appSet) | websocket.hostId | none | ??? |
| (appSet) | jsonb.provider | none | - |
| (appSet) | jsr339.charset | utf-8 | - |
| (appSet) | org.coodex.concrete.spring.aspects.ConcreteAOPChain.order | 0 | - |
| moduleMock, (appSet) | client(.destinationId) | false | - |
| (appSet) | zipkin.location | none | - |
| (appSet) | module.name | concrete | - |
| concrete_accounts, (appSet) | application.name | coodex.org | - |
| (appSet) | tenant.RPC.service | none | - |
| (appSet) | defaultPassword | p@55w0rd | - |
| concrete_accounts, (appSet) | validation.defer | false | - |
| concrete_accounts, (appSet) | validation.days | 7 | - |
| concrete_accounts, (appSet) | administrator.id | none | - |
| limiting.maximum.concurrency, (appSet), (strategyName) | max | Integer.MAX_VALUE | - |
| administrator, (appSet) | encoded.password | (default) | - |
| administrator, (appSet) | authKey | none | - |
| administrator, (appSet) | name | none | - |
| administrator, (appSet) | roles | {SystemManager} | - |
| administrator, (appSet) | valid | true | - |
| totp, (appSet) | fault.tolerance | 10 | - |
| totp, (appSet), (app) | issuer.name | coodex.org | - |
| totp, (appSet), (app) | server.name | (issuer.name) | - |
| (appSet) | (serviceClassName).(methodName).(parameterCount) | none | - |
| jaxrs.predicates, (appSet) | service.predicates.PUT | update, set | - |
| jaxrs.predicates, (appSet) | service.predicates.GET | new, get, findBy | - |
| jaxrs.predicates, (appSet) | service.predicates.DELETE | delete | - |
| jaxrs.predicates, (appSet) | service.predicates.POST | save | - |
| attachmentService, (appSet) | fastdfs.tracker | none | String[] |
| attachmentService, (appSet) | key.(clientId) | "" | - |
| attachmentService, (appSet) | attachment.validity | 10 | integer, minutes |
| attachmentService, (appSet) | rule.read | public | - |
| attachmentService, (appSet) | (clientId).location | none | - |
| attachmentService, (appSet) | download.speedLimited | 1024 (k) | - |
| attachmentService, (appSet) | (clientId).readonly | true | - |
| attachmentService, (appSet) | download.priority | 1 | 1 - 10 |
| attachmentService, (appSet) | upload.priority | 5 | 1 - 10 |

- executors

  - service
  - client

- schedulers

  - service
  - count
  - tbm - token based message
  - jms.reconnect
  - localTokenManager
  - fsm.lock.release
  - websocket.retry
  - sct.debouncer

- profile

  - /accounts/(id).properties
    - id: string
    - roles: string[]
    - name: string
    - password: string
    - authKey: string
  - simpleAccounts.properties
    - password: boolean: true
    - authCode: boolean: true

- system properties

| key | default |
| --- | ---- |
| concrete.appSet | null |



## 2018-12-19
- 一些小优化

## 2018-11-12
- concrete-support-jaxrs: bugfix 修复Jaxrs模式下Subjoin未回传问题
- concrete-core, concrete-courier-jms, concrete-courier-rabbitmq: 机制调整，主题无订阅时不产生消费者


## 2018-09-14
- concrete-api-tools: 修复生成Angular脚本，订阅时的错误

## 2018-09-12
- 0.2.4-SNAPSHOT
- concrete-client: 增加`public static <T> T getInstance(Class<T> concreteServiceClass, Destination destination)`接口，不再完全依赖模块配置文件，暂行方案，后续考虑模块配置如何调整。

## 2018-09-09
- concrete-core: 
    - 去掉多余的debug
    - 增加ThrowableMapper，方便根据不同类型的异常转换ErrorInfo
    - IF性能优化
- concrete-core-spring: 缺陷修复;
- concrete-test: 修改TokenID的值相同即token相同，不等同于tokenId
- 0.2.3发布到中央库

## 2018-09-08
- concrete-core, concrete-core-spring: 缺陷修复

## 2018-09-03
- concrete-api-tools: 调整`axios`生成的代码

## 2018-08-29
- concrete-api-tools: 新增对`axios`的支持 `JaxRS.code.axios.js.v1`

## 2018-08-26
- coodex-utilites, concrete-core, concrete-core-spring: Topic可以按照jsr330规范注入了

## 2018-08-25
- concrete-core:
    - 废弃`AbstractTokenBaseTopicSubscribeInterceptor`
    - 新增`AbstractTokenBasedTopicSubscribeInterceptor`进行替代，关注点更少
- concrete-core-spring:
    - 相应修改

## 2018-08-24
- concrete-core: 
    - 一些优化；
    - 签名验签的干扰项调整为String类型;
    - 提供NoiseValidator和NoiseGenerator接口，方便扩展，默认行为与之前的方案相同
- concrete-spring:
    - 一些优化；
- concrete-client:
    - 一些优化；

## 2018-08-23
- concrete-core: 
    - bug fix. 修复JavassistHelper.getSignature时，基础类型支持有误的问题
    - 增加TestContext注解，声明用于支持单元调试环境的注解
- concrete-test
    - 修改上下文

## 2018-08-21
- concrete-support-jsr311, concrete-support-jsr339: 重命名了ConcreteJaxrs\*\*\*Application，调整为ConcreteJSR\*\*\*Application，作废原有名称

## 2018-08-20
- concrete-api: 增加 `MessageSubject` 注解，用于修饰POJO，可达到实现`Subject`接口相同效果，减少对POJO的代码侵入，`concrete-core`修改代码进行支持
- concrete-core: 修改切片对accept方式，定义了`ServerSide` `Local` `ClientSide` `Default` 四个注解，用来声明Interceptor的作用域

## 2018-08-19
- concrete-core-spring: 简化切片的使用方式，废弃`org.coodex.concrete.spring.aspects.*Aspect`，直接定义`ConcreteInterceptor`的Bean即可
- concrete-core: 修改TokenBasedTopicMessage的缓存机制，默认缓存30秒，可通过`tokenBasedTopicMessage.cacheLife`更改，单位为秒

## 2018-08-18
- coodex-utitlies: 增加ByteArrayBuilder，方便构建协议数据
- concrete-core: 增加TokenBasedTopic的订阅管理切片，和自动取消订阅的TokenEventListener，[使用参考](test-case/test-case-server/src/main/java/org/coodex/concrete/test/impl/SubscribeInterceptor.java)

## 2018-08-17
- coodex-utilities: https://github.com/snksoft/java-crc 在其基础上进行修改，参考 https://crccalc.com/ 的算法名定义了CRC8/16/32的枚举，提供通用的crc算法
- concrete-commons-spring-data: 修复isNull和notNull接口参数问题
- concrete-bom: 增加javax.el

## 2018-08-04
- concrete-api-tools: 修复生成的脚本在jquery3下无法正常使用的缺陷。

## 2018-08-01
- concrete-jaxrs-client: 修复接口返回集合类型时，返回null报错问题

## 2018-07-30
- concrete-core: `BeanValidationInterceptor` 无参数方法不再进行校验

## 2018-07-28
- coodex-utilities: 并行处理扩展了一个RunnerWrapper
- concrete-core: APM 增加并行处理支持
- concrete-apm-zipkin: 增加并行处理支持
- concrete-client: 增加调用接口和方法的tag，方便查看
- concrete-jaxrs-client: 修复`X-CLIENT-PROVIDER`为空的问题
- concrete-apm-plugin-mysql: 参考 `brave-instrumentation-mysql` 增加apm的mysql的插件，在`url`后面增加`statementInterceptors=org.coodex.concrete.apm.mysql.TracingStatementInterceptor`即可 

## 2018-07-27
- 增加APM能力，基于zipkin做了一个轻量封装
    - 增加`concrete-apm-zipkin`依赖；
    - concrete.properties里配置`module.name`, `zipkin.location`即可

## 2018-07-21
- concrete-client: `moduleMock.properties`中可配置`client.MODULE_NAME=true`来模拟客户端调用返回数据，优先级高于`0720`修改

## 2018-07-20
- coodex-utilities
    - 提供一个SimpleDateFormat不是线程安全的坑的解决方案，concrete使用到SimpleDateFormat的地方相应更改。`Common.getSafetyDateFormat()`
    - Common提供了一组日期时间与字符串转换的接口
- concrete-client: 根据模块化的概念，调整模拟客户端调用返回数据的vm参数优先级，`org.coodex.concrete.client.MODULE_NAME` 高于原优先级

## 2018-07-19
- concrete-jaxrs: 废弃蹩脚的BigString，增加Body注解，用来声明指定的`基础类型参数`通过body传递。Body也可以用于修饰自定义注解类型，如下例也能声明通过body传递
```java
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Body
public @interface PostParam {
}
```
- concrete-support-jaxrs: 修复多个通过body传递参数时，参数传递有误的问题

## 2018-07-14
- concrete-jaxrs-client: 修正URLEncoder问题，` ` 转为 `%20`
- concrete-support-jaxrs: 修复Subjoin无法获取客户端传递的header的问题。根据[RFC2616 4.2](https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2)，使其对大小写不敏感
    - 坑: spring-boot-start默认使用tomcat,tomcat把所有请求header转小写了,使用spring-boot-start-jetty,则可以保留大小写,不确定哪个更好
- concrete-core: 适配之前版本的signature规则恩
- coodex-utilities: `SingtonMap` 增加最大存在时长参数

## 2018-07-13
- concrete-core: 增加发布订阅模型的消息Serializer
- concrete-courier-jsm, concrete-courier-rabbitmq: 增加 `serializer` 配置项, 可选有 `json`, 默认为java serialize
- concrete-commons-spring-data: `SpecCommon`增加 `distinct`

## 2018-07-02
- concrete-core: 增加queue的默认值配置
- 发布订阅模型支持Observer Bean,凡注册的Observer Bean均可被自动订阅
    - 增加MessageConsumer注解，用于声明Observer Bean应该订阅的主题

## 2018-07-01
- coodex-utilities: 增加多任务并行单元，所有并行任务完成后返回

```java

    //使用10个线程的线程池作为并行处理容器
    Parallel parallel = new Parallel(ExecutorsHelper.newFixedThreadPool(10));
    Runnable [] runnables = new Runnable[20];
    for(int i = 0; i < runnables.length; i ++){
        // 每个任务随机执行0-5000毫秒, 20%几率抛异常
        runnables[i] = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Common.random(5000));
                    if(Common.random(5)>3)
                        throw new RuntimeException("test");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    //所有任务执行完成后
    Parallel.Batch batch = parallel.run(runnables);

    System.out.println(JSON.toJSONString(batch,SerializerFeature.PrettyFormat));

```

## 2018-06-28
- 发布订阅模型增加基于rabbitmq的支持 `rabbitmq` or `rabbitmq:_url_`
    - 不带url配置时，需要配置 `username` `password` `ssl`(默认false) `virtualHost` `host` `port`(默认5672) 
    - [url参考](https://www.rabbitmq.com/api-guide.html#connecting)
        - concrete为 `amqp(s)://username:password@host:port/` 设置 `virtualHost` 为 `/`



## 2018-06-27
- 发布订阅模型增加jms支持 `jms::_provider_`
    - 增加activemq的支持模块 `jms::activemq:_url_`


## 2018-05-19
- 0.2.3-SNAPSHOT
- 重构Subjoin，移除语言环境；提供回写能力；
- TokenManager增加newToken接口；getToken(String, boolean)声明作废
- 重新设计ServiceContext
    - ServiceContext: 包含当前上下文的附加信息、语言环境、令牌id
        - ContainerContext: 服务运行容器的上下文，泛化自ServiceContext，增加了令牌、调用者
            - TestServiceContext: 测试环境的上下文，泛化自ContainerContext
            - ServerSideContext: service support的运行上下文，泛化自ContainerContext；须各服务提供者具体实现
                - JaxRS/WebSocket/Dubbo支持进行相应调整
            - LocalServiceContext: 本地调用时的上下文，泛化自ContainerContext；使用当前服务上下文的信息构建
        - ClientSideContext: 泛化自ServiceContext，增加调用的服务定义，封装了tokenId的获取规则；须各客户端提供者具体实现
    - 其他属性调整
        - Courier从上下文中移除，Server端消息推送不应受运行环境限制，调整方案待定
- 重新设计Server端推送模
    - 废弃原仅支持单机的订阅模型
    - 增加发布订阅模型 `org.coodex.concrete.message`
        - 通用消息主题 `Topic<MessageType>`
        - 基于Token的消息主题 `TokenBasedTopic<MessageType>`,相同消息同一个令牌只消费一次，消息推送基于此类型主题实现
            - 目前已支持jaxrs，其他服务模型持续增加
        - concrete-core-spring增加对此发布订阅模型的DI支持
            - 已知问题,目前尚不能使用@Inject注解注入,方案研究中 :(
            - 示例
            ```java
                private Topic<String> x;// 示意而已，建议消息体为明确的消息类型
            
                private Topic<Set<String>> y;
                
                @Queue("test")
                private Topic<String> z;
            ```
- 重写了一个对泛型检索的类，在发布订阅模型中试用，后续会替代原TypeHelper
- concrete-core: 增加IP和MAC的Mocker
- concrete-test
    - 增加TestSubjoin注解，方便添加测试环境的附加信息
- bug fix
    - concrete-api-tools: 生成angular based sdk的模板文件中的错误

## 2018-04-14

- org.coodex.concrete.coomon.Assert声明作废，使用org.coodex.concrete.common.IF替代
- 服务端增加dubbo支持
- 客户端增加dubbo支持
```properties
location=dubbo
# 客户端额外增加registry配置项
registry=protocal://user:pwd@ip:port, .....
```
- 发布0.2.2

## 2018-04-04

- coodex-utilities提供`Singleton<T>`和`SingletonMap<K,V>`，通过双重校验锁的模式，减少同步锁的开销，Concrete相关单例模式依此更改
- 重构java client，作废原java client代码，新旧版本java client不保证行为一致
    - 作废org.coodex.concrete.jaxrs.Client
    - 提供org.coodex.concrete.Client作为获取Client的统一入口
    - IoC容器中，支持JSR330规范；支持ConcreteService和RX_ConcreteService
    ```java
        @Inject @ConcreteClient("TARGET")
        private SomeService someService;
    ```
    - 配置调整为
    ```properties
      # 优先级为
      # client.模块名.properties
      # location: 服务注册端点，不得为空, local表示本地调用；alias:其它模块名,表示该模块同目的模块
      # tokenTransfer: 布尔值，默认为否，表示是否需要从当前系统传递token到远端。当前系统与远端模块共享token时使用
      # tokenManagerKey：作为客户端，使用哪个key来保存跟远端之间的token，相同key的则说明使用同一份token。tokenTransfor为真时，此属性无效
      # 高于
      # client.properties
      # 模块名.location
      # 模块名.tokenTransfer
      # 模块名.tokenManagerKey
      # 高于
      # location
      # tokenTransfer
      # tokenManagerKey
      # 高于
      # concrete.properties
      # client.模块名.location
      # client.模块名.tokenTransfer
      # client.模块名.tokenManagerKey
      # 高于
      # client.location
      # client.tokenTransfer
      # client.tokenManagerKey
    ```
    - 适配java client，签名切片调整。当一个系统需要对接多个远端模块时，可以使用signature.模块名.properties来区分设置
    - 重构SSLContextFactory，根据ssl描述生成SSLContext
        - AllTrusted，AllTrustedSSLContextFactory，信任所有证书
        - 为空或者Default，JSSEDefaultSSLContextFactory，使用JSSE环境的默认sslContext
        - certPath:证书路径，使用`;`分隔，X509CertsSSLContextFactory，信任提供的证书路径中的证书，证书路径须指向具体的证书文件。单个path中使用classpath:多个path时，使用`,`分割
    - 重构jaxrs客户端调用
    - 重构Websocket客户端调用，增加Websocket客户端的同步调用模式
- next feature:
    - 服务、客户端增加dubbo支持
    - 服务、客户端增加socket支持
    - 增加JWT支持
    

## 2018-3-18

- bugfix: 业务线程池模型缺陷修复
- 重构代码结构

## 2018-02-28

- 0.2.2-SNAPSHOT
- bugfix: 
    - 根据https://stackoverflow.com/questions/27513994/chrome-stalls-when-making-multiple-requests-to-same-resource 修改浏览器端api的headers
    - jaxrs2.0 Polling线程池有可能为空的问题
- java-client移除了自动polling的机制

## 2018-02-08

- coodex-utilities: 增加Debounce跟Throttle
- bug fixed:
    - java-client: 修复不带domain请求时concrete.client.domain设为local不可用的缺陷

## 2018-01-03

- api-tools: 生成angular SDK时，增加了模块化定义，简化项目代码; 生成的service依赖改为相对路径，不再需要在tsconfig.json里添加baseUrl。
- api-tools: 新增AngularCodeRenderV2，使用Angular 4.3的新模块HttpClient模块替代Http模块，推荐Angular版本高于4.3的使用

## 2017-12-26

- jaxrs: 调整jaxrs的使用方式，实现上不在需要基于明确的jaxrs实现，也不再需要使用蹩脚的JaxRSServiceHelper.generate。
    - 继承了javax.ws.rs.core.Application
    - 只需要在相应的ConcreteJaxrsApplication中注册class或package即可

## 2017-12-14

- jaxrs: 规范tokenKey的header，使用`-`替换`_`
- 修复devMode下，String类型containType不正确的问题
- api-tools: angular api的Broadcast服务增加doPolling()接口，用于断线重连
- 增加javaclient对消息推送的支持:
    - 使用`org.coodex.concrete.client.MessageSubscriber.subscribe(String subject, MessageListener<T> listener)`进行订阅
    - 原WebSocket的收发代码模式废弃

## 2017-12-06

- bugfix: coodex-utilities, Profile的getStrList接口默认值为null但是返回零长度数组的问题

## 2017-11-27

- concrete-commons-spring-data: SpecCommon增加表达式相关的接口，原SpecCommon.spec接口声明作废，

## 2017-11-24

- 调整模拟数据的参数，优先级为org.coodex.concrete.XXXX.devMode > org.coodex.concrete.devMode, XXXX可选范围如下：
    - jaxrs: jsr311/339 support
    - websocket: jsr356 support
    - jaxrs.client: jaxrs java客户端
    - websocket.client: websocket java 客户端
- jaxrs-support: Polling模块自动加载，无需额外注册

## 2017-11-22

- concrete-api: 
    - 增加消息推送模型
- concrete-core: 
    - 增加消息推送模型的实现，提供基于Token的消息过滤器，提供LocalPostOffice，集群应用时，可自行基于JMS或者其他消息通信方式实现跨主机的PostOffice，只需在消息到达时调用AbstractPostOffice的分发功能即可；
    - 【BUG FIXED】 
        - bug: 因为PriorityBlockingQueue是无界的，最大值无效，所以，concrete默认是单线程再跑服务 - -#
        - 修改业务线程池模型，只要线程池还有空闲线程就执行任务，线程池满时才放入优先级队列等待资源。因为concrete大量使用了本地线程变量，此模型较java本身的无界队列线程池模型保障了可用性的前提下更有利于资源回收
- concrete-support-websocket: 
    - 支持消息推送模型，deprecated原推送接口
- concrete-support-jaxrs: 
    - 支持消息推送模型，基于token过滤消息，服务端消息缓存5分钟。提供Polling接口，客户端可指定轮询超时时间，单位为秒，jsr339支持后端非阻塞获取消息，不占用业务线程池资源
- concrete-api-tools: 
    - 增加jQuery jaxrs对消息推送模型的支持，订阅消息通过configuration的onBroadcast(msgId, host, subject, content)达到，轮询需要至少调用一次concrete.polling()，建议在订阅好消息以后在开始轮询，否则可能出现token覆盖问题；jQuery websocket同时增加concrete.polling接口，无任何效果，为的是一份代码可以无缝支持多个传输协议
    - 增加Angular jaxrs对消息模型推送的支持，订阅消息通过注入Broadcast服务实例来subscribe；同步修改Angular webSocket的订阅方式
- 增加Java/RX client对消息推送的支持【todo】

## 2017-11-19

- 客户端共享token
    - jaxrs支持不再通过cookie传递TokenId，使用Header
    - websocket支持增加对tokenId的支持
    - java client和RX client获取实例时，可以定制token的作用域，tokenManagerKey非空且相同的，则在client端共享token
    - jQuery jaxrs/websocket 选用localStorage支持共享token，指定configuration.globalTokenKey非空且相同的共享token
    - Angular jaxrs/websocket 选用localStorage支持共享token，指定RuntimeContext 的 globalTokenKey非空且相同的共享token
- 修改Angular jaxrs/websocket指定服务位置的方式：指定RuntimeContext的root
- 服务端跨域设置如下
```properties
    # cors_settings.properties
    allowOrigin=*
    exposeHeaders=concrete-token-id,concrete-error-occurred
    allowMethod=POST,OPTIONS,GET,DELETE,PUT,PATCH
    allowHeaders=CONCRETE-TOKEN-ID,CONTENT-TYPE,X-CLIENT-PROVIDER
    allowCredentials=true
```
        

## 2017-11-18

- 增加状态容器，容器中的状态基于id最多仅允许存在一份，防止出现多个线程操作相同id但实例不同的状态
- 增加带信号的状态以及带信号的状态检测器，只需指定允许哪些信号状态进入即可，无需为每个状态转移编写StateCondition，简化开发

```java
package test.org.coodex.concrete.fsm.signaledfsm;

import org.coodex.concrete.fsm.SignaledState;

public class DemoSignaledState implements SignaledState {
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public long getSignal() {
        return value;
    }
}
```

```java
package test.org.coodex.concrete.fsm.signaledfsm;

import org.coodex.concrete.fsm.FiniteStateMachine;
import org.coodex.concrete.fsm.SignaledGuard;

public interface FSMDemo2 extends FiniteStateMachine<DemoSignaledState> {

    @SignaledGuard(allowed = 3)
    void toZero();
    @SignaledGuard(allowed = 0)
    void toOne();
    @SignaledGuard(allowed = 1)
    void toTwo();
    @SignaledGuard(allowed = 2)
    void toThree();
}
```

## 2017-11-17

- 增加一个轻量级有限状态机框架
    - State在FSM中的操作是线程安全的
    - 自行定义FSM的状态转移
    - 以目标状态为视角，通过StateCondition确定是否可以从当前状态转移到目标状态
    - 提供一个基于java动态代理和ConcreteServiceLoader的Provider
    - 示例：状态为仅包含一个数的模型，转移条件为从0-3循环，及0->1->2->3->0，Runner的main方法模拟300个线程并发进行状态迁移
    - 示例代码参见concrete-fms-impl的test代码 https://github.com/coodex2016/concrete.coodex.org/tree/0.2.1/11.concrete-fsm-impl/src/test
    - 目前的实现基于java动态代理，因此，有几个要求如下：
        - State必须是个贫血模型，并且每个属性都可以set，用以达到事务控制；为了支持deepCopy，State还必须要有public无参数的构造方法
        - 如果需要自身调用状态转移时，状态机需要继承AbstractFSM，并且使用getSelf()来执行状态转换
        

## 2017-10-16

- bug fixed: 当服务产生异常时，sendError报The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method

## 2017-09-27

- jaxrs: 
    - 修正字符串返回值不规范的用法，调整为：若返回值为字符串时，content-type为text/plain
    - 同步调整jaxrs-client/jquery jaxrs api tool/ angular jaxrs api tool
    - angular用户需要注意，请删除 `AbstractConcreteService.ts` 重新生成api


## 2017-09-25

- 实现webSocket caller的获取方式 - -#
    - org.coodex.concrete.support.websocket.CallerHackFilter 负责在客户端第一次请求时将调用者信息保存在httpSession中
    - org.coodex.concrete.support.websocket.CallerHackConfigurator 负责从HttpSession中的Caller放入到WebSocket的Session中
    - 使用时，1、配置Filter map WebSocket的EndPoint url；2、为ServerEndpoint指定CallerHackConfigurator

## 2017-09-24

- concrete-api: 增加Caller接口，方便业务获取调用者信息
```java
    @Inject
    private Caller caller;

    // .....
    public void someMethod(){
        caller.getAddress();// 调用者地址
        caller.getAgent();
    }
```
- concrete-core: 修改业务服务上下文机制，大量减少了闭包对象的创建，方便后续扩展
- concrete-core-spring: 增加ConcreteSpringConfiguration，封装Token、Caller、Subjoin的单例bean
```xml 
<!-- 主配置文件中使用 -->
<bean class="org.coodex.concrete.spring.ConcreteSpringConfiguration"></bean>
```
或
```java
//主配置class中增加注解
@Import(ConcreteSpringConfiguration.class)
```

## 2017-09-20

- concrete-core: 移除对fastjson的依赖，默认JSONSerializer还是FastJsonSerializer，通过反射引用。可能引起的问题，之前使用默认JSONSerializer的项目需要自行引入fastjson依赖
- 增加:concrete-serializer-jackson2，提供基于jackson2的JsonSerializer
- 增加:concrete-serializer-jsonb-jdk8，提供基于JSR 367 json-bind规范的JsonSerializer,JDK8以上支持
- 修复原先不规范、基于FastJson特性的缺陷代码

## 2017-09-18

- coodex-utilities: 增加org.coodex.util.Parameter注解，用于声明参数名称，降低客户端与服务端对jdk特性的依赖
```java
    @Description(name = "帐号登录")
    String login(
            @Parameter("account")
            @BigString
                    String account,
            @Parameter("password")
            @Description(name = "密码")
            @BigString
                    String password,
            @Parameter("authCode")
            @BigString
                    String authCode);
```
- 附件模块修改api，增加Parameter注解
- accounts修改api，增加Parameter注解

## 2017-09-14

- ConcreteWebSocketEndPoint 增加 getToken(Session session)接口，方便基于token的过滤
- concrete-api-tools增加websocket angular支持，使用方式同jaxrs的angular支持。不同点：
    - 增加Broadcast工具类，用于处理服务端广播
```typescript
    Broadcast.subscribe('abcd'/*broadcast subject*/, Subscriber.create(
      (data) => console.log(data),
      () => {},
      () => {}));
```


## 2017-09-11

- concrete-api-tools增加websocket jquery支持，使用方式同jaxrs的jquery支持。不同点：
    - configuration增加onBroadcast(msgId, hostId, subject, content)属性，用来处理服务端的广播
- websocket java client增加BroadcastListener，用于处理服务端广播，支持ServiceLoader机制加载lisenter，也支持`org.coodex.concrete.websocket.WebSocket.addBroadcastListener`
- 服务端支持确定广播调用模式，如下：
```java
WebSocket.getEndPoint("/WebSocket"/* websocket的ServerEndpoint */).broadcast(subject, message);
```

## 2017-09-10

- 调整jsr339的线程池配置，考虑到其他服务支持方式都可以使用同一个业务线程池，将其移至concrete-core，配置项 `service.executor.corePoolSize` \ `service.executor.maximumPoolSize`
- 增加jsr356 websocket的支持
- 增加websocket rxjava客户端支持
- 结合rxjava，调整AbstractSignatureInterceptor的模式，废弃JaxRSSignatureInterceptor，使用org.coodex.concrete.core.intercept.SignatureInterceptor替代
- 结合rxjava，增加异步拦截器链
- 废弃0901的想法，JMS不适合支持concrete异步服务
- some bugs fixed
- todo: concrete-api-tools 增加websocket jquery支持/websocket angular支持

## 2017-09-01

- 增加对rxjava2的支持
    - [X] 增加concrete-rx包（呃。。。虽然就一个注解。。。)
    - [X] concrete-api-tools增加ConcreteService转RXService工具
    - [X] 增加ConcreteSyncInterceptor，修改原ConcreteInterceptor接口，将invoke和around移至ConcreteSyncInterceptor
    - [X] 增加AbstractSyncInterceptor，修改原AbstractSyncInterceptor，将invoke和around移至AbstractSyncInterceptor
    - [X] 改造SignatureInterceptor支持异步
    - [X] 增加concrete-rx-client，客户端支持响应式编程
    - [X] 增加concrete-jaxrs-client-rx，将concrete-jaxrs-client转成rx，参见[JavaClient](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/JavaClient.html)
    - [ ] 废弃: 增加concrete-support-jms，支持通过jms提供异步服务
    - [ ] 废弃: 增加concrete-jms-rx-client

## 2017-08-29

增加演示构建过程示例

## 2017-07-21

- concrete-core: Assert支持直接使用String抛出异常
- concrete-commons-spring-data
    - 增加SpecificationsMaker接口，方便从业务代码中剥离动态查询参数构建
    - 增加AbstractSpecificationMaker，支持同一个查询条件的多种构建方式

例如：
```java
// 原代码

ConditionType c = pageRequest.getCondition();

Specifications<EntityType> spec = null;
if(c.someProperties() != null){
    spec = SpecCommon.someOperator(spec, ....);
}

....

return PageHelper.copy(repo.findAll(spec, pageParam), copier);

// 现代码
@Inject
private SpecicationsMaker<EntityType, ConditionType> maker;


return PageHelper.copy(repo.findAll(maker.make(pageRequest.getCondition()), pageParam), copier);

```

AbstractSpecificationsMaker用法

```java
public class SomeMaker extends AbstractSpecificationsMaker<EntityType, ConditionType>{
    
    // 名称为buildFor1
    public Specifications<EntityType> buildFor1(ConditionType conditionType){
        ....
    }
    
    // 名称为buildFor2
    @MakerFunction("buildFor2")
    public Specifications<EntityType> xxxx(ConditionType conditionType){
        ....
    }
}

// 业务代码

@Inject
private AbstractSpecificationsMaker<EntityType, ConditionType> maker;


//业务场景1
return PageHelper.copy(repo.findAll(maker.make(pageRequest.getCondition(), "buildFor1"), pageParam), copier);

//业务场景2
return PageHelper.copy(repo.findAll(maker.make(pageRequest.getCondition(), "buildFor2"), pageParam), copier);

```


## 2017-07-17

- coodex-utilities: 修复random低级错误
- 增加明确的AccountID接口，需要提供序列化方法。定义AccountIDDeserializer，用于反序列化AccountID，方便其他数据到Account的关联，例如：操作日志，AccountMappingProfile(近期增加，用于外部系统关联到系统账户，如微信等)
    - concrete-account实现:AccountIDImpl，AccountIDImplDeserializer


## 2017-07-14

- 修改jaxrs谓词规则，匹配谓词时，依长度优先，同长度则PUT > DELETE > POST > GET


## 2017-07-13

- 修改java client的接口名 getBean --> getInstance，getBean在0.2.2移除

## 2017-07-05

- accounts模块增加一个简单实现，支撑业务功能快速开发，基于/accounts/***.properties提供账户

## 2017-06-30
   
- 修复java client在Android环境中的问题：1、classloader机制不同导致的缺陷；2、因android正则表达式支持不完整，调整相关表达式
- 根据v5哥的发现修改CorsFilter

## 2017-05-27

- concrete.properties: 
    - 增加 `concrete.api.packages`，用于声明当前应用所使用的api包
    - 增加 `concrete.remoteapi.packages`，用于声明当前应用所使用的远端api包
    - 增加 `concrete.appSet`，用于声明当前应用所属的应用集合
- concrete-accounts: 增加对SaaS多租户模式的账户支持

## 2017-05-20

- 修复部分缺陷
- api: 增加TokenEventListener, 通过ServiceLoader加载
- api: [log](https://concrete.coodex.org/%E5%AE%9A%E4%B9%89/log.html)注解进行调整
- 添加[账户模块](https://concrete.coodex.org/%E5%B8%90%E5%8F%B7/)，提供组织结构类型的账户管理框架及参考实现

## 2017-05-17

- coodex-utilities: 调整关联策略模型，提供更方便的扩展方式

```java
public class IdCardRelation extends AbstractRelationPolicy {
    @Override
    public String[] getPolicyNames() {
        return new String[]{ID_CARD_TO_SEX, ID_CARD_TO_BIRTHAY};
    }

    @RelationMethod(ID_CARD_TO_SEX)
    public Integer toSex(String idCardNo){
        if (idCardNo != null) {
            switch (idCardNo.length()) {
                case 15:
                    return (idCardNo.charAt(14) - '0') % 2 == 0 ? 2 : 1;
                case 18:
                    return (idCardNo.charAt(16) - '0') % 2 == 0 ? 2 : 1;
            }
        }
        return null;
    }

    @RelationMethod(ID_CARD_TO_BIRTHAY)
    public String toBirthday(String idCardNo){
        if (idCardNo != null) {
            switch (idCardNo.length()) {
                case 15:
                    return "19" + idCardNo.substring(6, 12);
                case 18:
                    return idCardNo.substring(6, 14);
            }
        }
        return null;
    }

}
```

## 2017-05-16

- coodex-utilities: POJOMocker推倒从来，原接口保留到0.2.1，0.2.2版将移除。新[Mocker看这](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/mocker.html)。

## 2017-05-10

- api: 调整Overlay注解
    - 增加overlay属性，false：declaringClass --> module; true: module --> declaringClass。默认true。不设置Overlay同false
    - 增加definition属性，true: 仅作用于定义上, false:可通过具体服务原子实现进行重载。默认true


## 2017-05-09

- core: 调整ConcreteCache，支持不同的缓存策略，支持动态调整，重载getRule
```properties
# concrete.properties
# 线程池大小，根据具体应用的负载考虑，默认为1
cache.thread.pool.size=1
# 缓存对象生命周期，默认10分钟。也可以通过构造函数自行传入单位和时限
cache.object.life=10
# 可自定义的rule，例如org.coodex.concrete.accounts.organization
# 则，优先级为
# org.coodex.concrete.accounts.organization.cache.object.life
# org.coodex.concrete.accounts.cache.object.life
# org.coodex.concrete.cache.object.life
# org.coodex.cache.object.life
# org.cache.object.life
# cache.object.life
```

## 2017-05-08

- core: 再次调整ConcreteServiceLoader机制，默认Provider可以通过interfaceClass.name.default进行指定
- api: 增加OperationLog注解，用于定义模块如何渲染日志; 增加LogAtomic注解，用于定义服务原子如何记录日志
- 工具链: 增加对操作日志的支持


## 2017-05-03

- api-tools: 完善文档化输出方式；
- api-tools: 修复pojo到自身类型引用时造成死循环的缺陷
- api: 增加Safely注解，可被重载，用于定义服务是否需要可信账户才可访问，类似于taobao web使用cookie登录时，可以浏览部分内容，但是下单、支付等需要验证当前账户
- core: rbac进行相应调整
- coodex-utilities: Profile.getStrList会trim
- 少量缺陷修改

## 2017-04-28

- 调整jaxrs的谓词规则，基于java方法名
- 修改RBAC
    - 行期调整acl基于**moduleName**.properties的methodName.methodParamCount
    - AccessAllow可以定于与类型上
- 修复ServiceLoader泛型参数为变量类型问题
- 增加Overlay注解，用于声明注解是否可被重载
    - 可被重载(Domain, Limiting, Priority, ServiceTiming, Signable)时，上下文搜索优先级为：
        - method
        - moduleClass
        - method.declaringClass
    - 不可被重载(AccessAllow)时，上下文搜索优先级为
        - method
        - method.declaringClass    
        - moduleClass
- 调整继承链的支持方式

## 2017-04-27

- 重构SPIFacade，更名为ServiceLoader/ConcreteServiceLoader/AcceptableServiceLoader
- 统一部分混乱的代码
- 增加多AccountFactory支持
    - 规划id，分别实现AcceptableAccountFactory
    - 注册AccountFactoryAggregation(多AccountFactory聚合)和AccountFactoryAggregationFilter(AccountFactory冲突解决方案)
    

## 2017-04-25

- JaxRS模块增加PathParam和RouteBy的检查
- 增加请求附加信息[Subjoin](https://concrete.coodex.org/%E5%AE%9A%E4%B9%89/Subjoin.html)，在jaxrs模块中利用Header实现
- api/core部分少量接口、工具调整package
- 增加可签名[Signable](https://concrete.coodex.org/%E5%AE%9A%E4%B9%89/Signable.html)注解，用于定义模块或者单元是否需要签名
- 调整ConcreteSPIFacade机制，除了ServiceLoader加载的providers外，还会把所有该类型的bean加载进来
- 修改Spring切片支持方式，AbstractConcreteInterceptor增加泛型参数，只需要指定其泛型，并增加Aspect注解即可；移除原切片原子的设计


## 2017-04-19

- 修复JavassistHelper的一个缺陷，重构至concrete-core中
- coodex-utilities中定义了一个[计数模型](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/counter.html)，目的：数据随时产生随时统计或统计预处理，不需要所有的统计都到数据库里去算，分段统计快照，提高统计效率
- concrete-core中提供一套实现
    - concrete.properties配置`counter.thread.pool.size`

## 2017-04-15

- jaxrs 339增加字符集配置，concrete.properties ```jsr339.charset```, 默认utf8       
- 重构javaClient，调整机制，简化client端构建
    - 提供基于JaxRS Client的默认InvokerFactory
    - 移除serializer-fastjson包，将其作为默认的serializer实现
    - 支持字符集设定，参见[Java客户端调用](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/JavaClient.html)


## 2017-04-14

- JaxRS支持：取消只有1个POJO的限制。文档、客户端工具链同步更新


## 2017-04-13

- 修复生成文档时，泛型服务的POJO类型继承问题

## 2017-04-12
- 增加Limiting注解，定义业务限流接口，保障系统的可用性
- 提供最大业务并发量模型的默认实现，后续扩展令牌桶、漏桶模型的默认实现
    - 策略配置文件 limiting.maximum.concurrency.properties
    ```properties
      # 默认策略的最大业务并发量
      max
      # 指定策略的最大业务并发量
      strategyName.max
    ```
- [api工具](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/API.html)增加angular支持，支持angular2+

## 2017-03-27

- 调整客户端访问机制，定义SSLContextFactory接口，提供部分实现
    - JSSEDefaultSSLContextFactory: 生产JSSE的默认SSLContext
    - AllTrustedSSLContextFactory：信任所有server端证书，不建议使用，中间人攻击数据泄漏的哗哗的
    - X509CertsSSLContextFactory：cocnrete默认实现，只信任指定资源目录下的证书
        ```properties
        # concrete.properties
        
        # 默认路径
        trusted.certs.path
        
        # 各domain的资源路径, domain全小写
        # trusted.certs.path.domain(.port), 例如：
        trusted.certs.path.example.coodex.org
        ```
        参考InsertCerts.java 提供工具方法
        ```java
        X509CertsSSLContextFactory.saveCertificateFromServer(String host, int port, String storePath)
        ```
        获取指定host port的证书
         


## 2017-03-25
- 作废CommonRepository接口。这是一个草率的设计，不推荐。对于大部分系统都会采用读写分离，应该从设计上就明确区分出A仓库和T仓库，A仓库不应该有CUD，T仓库不应该支持动态查询，防止程序误用。

## 2017-03-24

- concrete-core 增加ConcreteCache对象，用来解决需要定期失效的缓存数据模型，例如：当实时性要求不高时，用户的角色可以缓存下来，10分钟后再重新加载新的角色，减小数据获取的开销
```properties
# concrete.properties
# 线程池大小，根据具体应用的负载考虑，默认为1
cache.thread.pool.size=1
# 缓存对象生命周期，默认10分钟。也可以通过构造函数自行传入单位和时限
cache.object.life=10
```



## 2017-03-22

- 增加[SaaS](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/SaaS.html)支持


## 2017-03-21

- 增加分页相关的POJO抽象定义
- 增加PageCopier通用方法，从spring data的page对象复制成PageResult
- 修改ConcreteException，当最后一个参数为Throwable时，将其设置为引发ConcreteException的Cause

## 2017-03-18
- 315搞到了coodex.org域名，撒花
- 重构包名 -> org.coodex
- 重构groupId -> org.coodex
- Apache Licence 2.0
- git 迁移到 https://github.com/coodex2016/concrete.coodex.org
- 基于 [V5哥](mailto:sujiwu@126.com) 的设计思路，添加jpa和spring data jpa的通用模块，定义了PO/VO转换的规约
- 318 sonatype Staging repositories have been prepared for org.coodex，发布0.2.0
- 文档域名也迁移到coodex.org
- 开启0.2.1-SNAPSHOT，继续功能补充

## 2017-03-09
- 修复BigString注解的全部缺陷
- JavaClient模块强化，增加AOP功能
    - 继承org.coodex.concrete.core.intercept.AbstractInterceptor，重载有关方法
    - META-INF/services/org.coodex.concrete.core.intercept.ConcreteInterceptor 中增加需要使用到的拦截器
    - org.coodex.concrete.jaxrs.Client.getUnitFromContext：根据拦截器上下文获取Unit信息

## 2017-02-21

[@Description](https://concrete.coodex.org/%E5%AE%9A%E4%B9%89/Description.html) 增强装饰功能。
可以装饰pojo属性和parameter。pojo属性指public field和getXXX isXXX(boolean)


## 2017-02-09

增加token Cookie path设定。原逻辑根据各模块的baseUri设置cookie path，主要考虑了同功能负载均衡可共享cookie，当多模块分离部署需共享cookie时，会无法获取。
    
    concrete.properties 增加配置项: jaxrs.token.cookie.path，默认为使用baseUri，否则使用设定值




## 2016-12-20

将账户是否有效的属性从Account接口中移至Token中。Account是客观账户实体，Token是账户认证后的状态。
Token中增加了当前账户相关的接口。


## 2016-12-15

增加[附件管理模块](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/fileServer.html)及基于Jax-RS 2.0 和 [FastDFS](https://github.com/happyfish100)的参考实现。

## 2016-12-10
调整Jaxrs的令牌模式，不再依赖servlet container环境。


## 2016-12-09

* 修改RoleOwner为Domain，为服务模块延伸了领域概念。

* 增加特权的定义
    * `*` 表示绝对特权，所有服务原子均有权访问
    * `*.role` 表示任意领域内的role角色
    * `domain.*` 表示领域内的特权，既该领域内所有服务均可使用

## 2016-12-07

* 工具链增加了[java客户端](https://concrete.coodex.org/%E5%B7%A5%E5%85%B7%E9%93%BE/JavaClient.html)
