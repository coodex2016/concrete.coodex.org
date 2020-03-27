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
