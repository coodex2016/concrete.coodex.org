# junit 单元调试场景工具

一个相对复杂的系统，需要多个测试场景的用例。如果你也碰到了以下问题，可以试一下`coodex-junit-enhance`

- 需要为每一个测试场景的用例单独输出日志，可是数量是不定的，传统做法也有，每个用例定义一个Logger，然后logging工具配置输出，麻烦
- 需要模拟时间流逝，[天上人间](../coodex-utilities/org.coodex.util.clock.md)虽然也能做到，但是不管几倍流逝速率，都还是慢，比如说，模拟一个3小时的活动，活动结束后推送消息，哪怕倍率是100倍，依然需要等待1.8分钟，要是一年呢？我们希望越快越好

我们来看一下`coodex-junit-enhance`怎么解决上面的问题。话不多说，开搞。

测试作用域增加`coodex-junit-enhance`的依赖

```xml
        <dependency>
            <groupId>org.coodex</groupId>
            <artifactId>coodex-junit-enhance</artifactId>
            <version>${coodex.libraries.version}</version>
            <scope>test</scope>
        </dependency>
        <!-- 使用 log4j2 进行记录 -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <version>${coodex.libraries.version}</version>
            <scope>test</scope>
        </dependency>
```

把`org.coodex.junit.enhance.Log4j2LoggerProvider`放到[SPI](../coodex-utilities/SPI.md)中，本案例使用java默认的SPI

`org.coodex.junit.enhance.LoggerProvider`

```txt
org.coodex.junit.enhance.Log4j2LoggerProvider
```

测试用例代码

```java
package test.org.coodex.demo;


import org.coodex.junit.enhance.Context;
import org.coodex.junit.enhance.CoodexEnhanceTestRule;
import org.coodex.util.Common;
import org.junit.Rule;
import org.junit.Test;

import static org.coodex.junit.enhance.TestUtils.TIME;
import static org.coodex.junit.enhance.TestUtils.logger;

public class JUnitEnhanceExample {

    // 使用coodex enhance rule
    @Rule
    public CoodexEnhanceTestRule testRule = new CoodexEnhanceTestRule();

    @Test
    @Context(name = "如不指定会用方法名")
    public void test1() {
        logger.info("now {}", Common.now());
        /**
         * 时间跳转到明年元旦，TestUtil.TIME提供了丰富的接口
         *
         * @see org.coodex.junit.enhance.TestUtils.Time
         */
        TIME.nextYear();
        logger.info("now {}", Common.now());
    }

    @Test
    public void test2() {
        logger.info("第二个测试场景");
    }

}
```

回到前面的问题

1. 我们没有做任何的log4j2的配置，运行完以后，logs目录下会出现两个案例的日志文件(参见下图)，效果就是，你只管加测试用例就行，不需要管logging配置

    ![logs](../images/logs.png)

2. 时间流逝，9个ms跑了10个月，支持用通过`ExecutorsHelper`创建的线程池，`TestUtils`也有异步执行的接口，支持[天上人间](../coodex-utilities/org.coodex.util.clock.md)，也就是说，你在业务代码中，使用`Clock.now()`，在测试环境和生产环境都适用

    ```txt
    01:47:16.123 [main] INFO  如不指定会用方法名 - now 2020-03-02 01:47:16
    01:47:16.132 [main] INFO  如不指定会用方法名 - now 2021-01-01 00:00:00
    ```

同样的，这部分也有扩展点，可以方便的注入测试用例上下文
