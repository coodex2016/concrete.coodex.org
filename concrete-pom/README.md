# Concrete

> 主分支已切换到0.5.0快照版，项目结构优化、功能完善中，稳定版请转到[0.4.x](https://github.com/coodex2016/concrete.coodex.org/tree/0.4.x)

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

## 其他项目

[coodex-libraries](https://github.com/coodex2016/coodex-libraries) [点我看文档](https://docs.coodex.org/lib/)
<!--
## 2020-06-??

- 【feature】concrete-api-tools: 重命名为concrete-api-renderer，调整为仅定义API文档化渲染的规范，封装通用操作，具体渲染分拆到具体模块完成
-->

## 2022-05-13

- concrete-api-tools: axios调用端增加cancel能力

## 中间一段时间

- 忘了写文档 ......

## 2021-04-05

- concrete-core-spring 环境下，Config.get优先从spring上下文中获取配置

## 2020-10-12

- 废弃ConcreteServiceLoaderProvider，改为使用org.coodex.spring.SpringServiceLoaderProvider

## 2020-10-11

- 根据coodex-libraries的ActiveProfiles机制，增加SpringActiveProfilesProvider，使用spring-boot时，coodex Profile与Spring active
  profiles机制一致

## 2020-05-15

- 废弃OperationLog机制，对于开发者并没有足够的优势

## 2020-05-13

- 根据`coodex-libraries`的更新，重构ErrorCodes部分，调整如下：
    - 使用`org.coodex.util.Renderer`渲染错误信息
    - 增加`@ErrorCode`注解，用来声明一个class是用来进行错误码定义的，其value可以指定message template在I18N下的命名空间，关于在此类中定义的错误码：
        - 错误码必须是public的
        - 错误码必须是final的
        - 错误码必须是int类型
    - 增加`@ErrorCode.Key`注解，用来定义错误码I18N下的键
    - 增加`@ErrorCode.Template`注解，用来指定明确的template，如此值非空，则直接使用此模板
    - 废弃`org.coodex.concrete.common.AbstractErrorCodes`
    - 删除`@ErrorMsg`

## 2020-04-17

- concrete-client缺陷修复，LocalDestinationFactory的选择判定有误
- `org.coodex.concrete.Client`提供`newBuilder`接口，可以传递附加信息，使用方法

```java
  Client.Builder<SomeService> someServiceBuilder=Client.newBuilder(SomeService.class);
        // ......
        Map<String, String> subjoin=new HashMap<>();
        // 设置需要传递的附加信息
        someServiceBuilder.withSubjoin(subjoin).someMethod();
```

## 2020-04-13

- coodex-utilities: 增加IDGenerator，分布式字符串ID生成器的Facade；增加UUID和SnowFlake的实现;
- bug fixed: SPI中使用Supplier作为默认值的提供者时会重复创建实例的问题
- 分离[coodex-libraries](https://github.com/coodex2016/coodex-libraries)

## 2020-03-27

- SPI机制优化,强化了泛型匹配,强化了基于java.util.ServiceLoader的加载器
- 结构继续完善调整,为项目分拆做准备

## 2020-03-21

- 0.5.0初次提交
- 结构优化，大部分早期代码调整到java8语言规范
- SingletonMap改为Builder模式

## 2020-03-08

- 发布0.4.0，0.4.x分支仅用于缺陷修复，不再增加新能力
- 开启0.5.x分支，TODO
    - 放弃对java8以前版本的支持
    - 重构SPI部分
    - 项目结构分拆
    - 其他优化
