# 天上人间

重要的事情说三遍：不是那个“天上人间”！！！不是那个“天上人间”！！！不是那个“天上人间”！！！因为那个天上人间已经没了，摊手

记得小时候看西游记，“天上一天，人间一年”，相同的时间在不同的“界”有不同的流失速度。`coodex`的`天上人间`提供了一种应用环境界（人间）与客观时间界（天上）不同时间流速的方案。

假设，我们的系统需要通过一种测试手段来证明，系统在一年内能够稳定运行，难道说我们花一年的时间来运行测试证明么？能不能用更少的时间？如果一个有个环境可以通过1个月来完成一年的运行情况（12倍率）肯定能够极大的提高效率。如果一天完成一年呢（365倍率）？这就是`天上人间`了。

我们做个案例来看，每隔一小时输出一下当前时刻，执行10次，正常情况下需要10个小时，我们用`天上人间`看看：

```java
package org.coodex.concrete.demo.boot;

import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClockDemo {
    private final static Logger log = LoggerFactory.getLogger(ClockDemo.class);

    private static final long AN_HOUR = 60 * 60 * 1000;

    public static void main(String[] args) throws InterruptedException {

        for (int i = 1; i <= 10; i++) {
            Clock.sleep(AN_HOUR);
            log.info("{}: {}", i, Common.calendarToStr(Clock.getCalendar()));
        }
    }
}
```

跑起来，我们十小时以后见。。。。

好吧，我们来开启时间加速，加上参数`-Dorg.coodex.util.Clock.magnification=365`

```txt
09:47:24.270 [main] DEBUG org.coodex.util.clock.AbstractClockAgent - ClockAgent[org.coodex.util.clock.DefaultClockAgent]: 
	magnification: 365.0
	baseLine: 2019-08-14 09:47:23.039
	start at: 2019-08-14 09:47:23.039
09:47:34.138 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 1: 2019-08-14 10:54:54
09:47:44.003 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 2: 2019-08-14 11:54:54
09:47:53.866 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 3: 2019-08-14 12:54:54
09:48:03.730 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 4: 2019-08-14 13:54:55
09:48:13.593 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 5: 2019-08-14 14:54:55
09:48:23.457 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 6: 2019-08-14 15:54:55
09:48:33.358 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 7: 2019-08-14 16:55:09
09:48:43.221 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 8: 2019-08-14 17:55:09
09:48:53.085 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 9: 2019-08-14 18:55:09
09:49:02.948 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 10: 2019-08-14 19:55:09
```

我们看到，`天上`在一分半钟完成了`人间`的10个小时 :)

好，继续，我们为了模拟`人间`运行，会有多个应用来模拟请求，每个应用都有自己的环境，咋整？

`coodex`提供了一个`ClockAgentService`和`RemoteClockAgent`来让各个应用采用统一的`人间`环境。

启动`时间管理中心`

```java
package org.coodex.concrete.demo.boot;

import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.clock.ClockAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClockDemo {
    private final static Logger log = LoggerFactory.getLogger(ClockDemo.class);

    private static final long AN_HOUR = 60 * 60 * 1000;

    public static void main(String[] args) throws InterruptedException {

        // 启动时间管理中心
        new ClockAgentService().start();

        for (int i = 1; i <= 10; i++) {
            Clock.sleep(AN_HOUR);
            log.info("{}: {}", i, Common.calendarToStr(Clock.getCalendar()));
        }
    }
}
```

在SPI`META-INF/services/org.coodex.util.clock.ClockAgent`中选择使用`RemoteClockAgent`

```txt
org.coodex.util.clock.RemoteClockAgent
```

增加`-Dorg.coodex.util.Clock.remoteHost=localhost` `-Dorg.coodex.util.Clock.remotePort=8360`

```txt
10:03:01.604 [main] DEBUG org.coodex.util.clock.AbstractClockAgent - ClockAgent[org.coodex.util.clock.DefaultClockAgent]: 
	magnification: 365.0
	baseLine: 2019-08-14 10:03:00.495
	start at: 2019-08-14 10:03:00.495
10:03:01.609 [main] INFO org.coodex.concrete.spring.SpringBeanProvider - spring bean provider not initialized, org.coodex.util.clock.ClockAgent not load from spring bean provider.
10:03:01.620 [Thread-0] INFO org.coodex.util.clock.ClockAgentService - Clock Agent Service start [0.0.0.0:8360]....
10:03:02.502 [main] DEBUG org.coodex.util.clock.AbstractClockAgent - ClockAgent[org.coodex.util.clock.RemoteClockAgent]: 
	magnification: 365.0
	baseLine: 2019-08-14 10:03:00.495
	start at: 2019-08-14 10:03:00.495
10:03:12.367 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 1: 2019-08-14 11:15:13
10:03:22.231 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 2: 2019-08-14 12:15:14
10:03:32.095 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 3: 2019-08-14 13:15:14
10:03:41.959 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 4: 2019-08-14 14:15:14
10:03:51.822 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 5: 2019-08-14 15:15:14
10:04:01.686 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 6: 2019-08-14 16:15:15
10:04:11.549 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 7: 2019-08-14 17:15:15
10:04:21.413 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 8: 2019-08-14 18:15:15
10:04:31.278 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 9: 2019-08-14 19:15:16
10:04:41.141 [main] INFO org.coodex.concrete.demo.boot.ClockDemo - 10: 2019-08-14 20:15:16
```

上面的案例中，我们看到了使用参数运行的方式，同样的，`天上人间`也支持[`Configuration`](config.md)，命名空间为`clock`

参数说明：

- `org.coodex.util.Clock.magnification`: Float，人间时间倍率
- `org.coodex.util.Clock.baseline`: String，`人间`时间基线，不设置则使用`人间`环境启动时间
- `org.coodex.util.Clock.remoteHost`: String，`人间时间管理`的主机地址，使用`RemoteClockAgent`时生效
- `org.coodex.util.Clock.remotePort`: int, `人间时间管理`的服务端口，默认`8360`(0x1978 + 0x0730，嗯，好像暴露年龄了)，使用`RemoteClockAgent`时生效

## 接口说明

- `Clock.currentTimeMillis(long)`: 对标`System.currentTimeMillis(long)`，获取`人间`当前时间的毫秒数
- `Clock.getCalendar()`: 对标`Calendar.getInstance()`，获取`人间`当前时间的Calendar对象
- `Clocl.objWait(Object, long)`: 对标`Object.wait(long)`，根据对象获得最长`人间`时间为指定参数的锁
- `Clock.toMillis()`: 对标`TimeUnit.toMillis()`
- `Clock.sleep(long)`: 对标`Thread.sleep(long)`，当前休眠一定`人间`时长(毫秒数)
- `Clock.sleep(TimeUnit, long)`: 对标`TimeUnit.sleep(long)`

> `coodex`及`concrete`时间相关都已经统一到`天上人间`。
