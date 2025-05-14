# Config

在设计Config之前，`coodex-utitlies`和`concrete`一直使用[`Profile`](org.coodex.util.Profile.md)作为系统配置信息获取的途径，随着`concrete`应用架构的扩展，对系统配置信息进行了封装，通过Config来隔离应用对实现的依赖。

## 接口定义

```java
package org.coodex.config;

public interface Configuration {


    /**
     * <pre>
     * 在多级命名空间中获取指定key的值，下级命名空间的值覆盖上级，例如
     * config.get("key","a","b","c")
     * 则
     * a/b/c/key 高于
     * a/b/key 高于
     * a/key 高于
     * key
     * </pre>
     *
     * @param key
     * @return
     */
    String get(String key, String... namespaces);

    <T> T getValue(String key, T defaultValue, String... namespace);

}
```

## usage

`coodex-utilities`中提供了一个`Configuartion`的门面，提供快捷获取配置的使用方式。

- Config.get(String key, String... namespace)
- Config.getValue(String key, T defaultValue, String... namespace)
- Config.getArray(String key, String... namespace)
- Config.getArray(String key, String delim, String[] defaultValue, String... namespaces)

`coodex-utitlies`基于[`Profile`](org.coodex.util.Profile.md)实现了一个Configuration，后续`concrete`会提供基于配置中心的实现，方便集群应用的配置获取

在基于`Profile`的实现中，命名空间使用`.`链接，如Configuratio注释中的案例优先级则为：

- Profile a.b.c 中的key
- Profile a.b 中的key
- Profile a 中的key
- 默认Profile中的key

> 默认Profile，非`concrete`环境是`coodex`,`concrete`环境是`concrete`
