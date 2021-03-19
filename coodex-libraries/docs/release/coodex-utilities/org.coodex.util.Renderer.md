# 数据渲染器

## 接口

```java
    /**
     * @param template 待渲染的模板
     * @param objects  渲染参数，支持{@link java.util.function.Supplier}
     * @return 渲染后的字符串
     */
    public static String render(String template, Object... objects)
```

## 扩展

实现`org.coodex.util.RenderService`，放到[SPI](SPI.md)中，选择策略为基于模板的选择，如SPI中针对当前模板无可用服务时，则原样返回模板。

### 已有的实现

- 基于`java.text.MessageFormat`的实现，开箱即用，支持Supplier。

示例：

```java
    System.out.println(Renderer.render(
            "您好，{0}。今天是{1,date,yyyy-MM-dd}，当前时间{1,time,HH:mm:ss}，您的服务号是{2,number,000}。祝您生活愉快。",
            "Davidoff", new Date(), 3));
```

```txt
您好，Davidoff。今天是2020-05-10，当前时间09:27:57，您的服务号是003。祝您生活愉快。
```

- 基于`freemarker`的实现，渲染变量从o1开始顺序命名，支持Supplier。

```xml
    <dependency>
        <groupId>org.coodex</groupId>
        <artifactId>coodex-renderer-freemarker</artifactId>
        <version>${coodex.libraries.version}</version>
    </dependency>
```

使用示例：

```java
    System.out.println(Renderer.render("现在时刻是 ${o1}",Common.now()));
    Map<String,Object> map = new HashMap<>();
    map.put("test","test");
    System.out.println(Renderer.render("测试：${o1.test}", map));
    System.out.println(Renderer.render("测试：${o2!\"xxx\"}", map));
```

```txt
现在时刻是 2020-05-10 10:45:41
测试：test
测试：xxx
```
