# 并发通用库

## 线程池助手

`org.coodex.concurrent.ExecutorsHelper`类似于`java.util.concurrent.Executors`的作用，不同的是：

1. 接口提供了定义线程池名称的功能
2. 提供了基于任务优先级的线程池，将要执行的任务会按照优先级送入线程池执行，并且，线程优先级也会设置成按照带优先级任务的优先级，高优先级的任务将拿到更多的系统资源
3. 扩展了有界阻塞队列，剥离队列大小与线程池最大线程数数的耦合，线程池最大线程数 低于 任务最大值时可用，线程满时，任务队列可以继续容纳一定数量的任务，尝试在线程数和任务数之间达到一个平衡
4. TODO: 编写文档的时候想起来的，可以做一个令牌桶的有界队列模型，去峰值

## 降频

前端用得比较多的技术，主要作用是减少频繁发生的事件的处理次数。

可以用于实时性要求不是极高的级频繁数据写入场景。

```java
package test.org.coodex.util;

import org.coodex.concurrent.Debounce;
import org.coodex.concurrent.Throttle;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class FDDemo {

    public static class CountableRunnable implements Runnable{
        private AtomicInteger count = new AtomicInteger(0);
        private final String info;

        public CountableRunnable(String info) {
            this.info = info;
        }

        @Override
        public void run() {
            log.info(info);
            count.incrementAndGet();
        }

        public int getCount(){
            return count.get();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(FDDemo.class);

    public static void main(String[] args) {
        CountableRunnable debounceRunnable = new CountableRunnable("debounce executed.");
        Debounce debounce = Debounce.newBuilder()
                .idle(15)// 空闲15毫秒才执行
                .runnable(debounceRunnable)
                .build();
        for (int i = 0; i < 1000; i++) {
            debounce.run(
                    // debounceRunnable // 另一种方式
            );
            Common.sleep(Common.random(15));
        }
        Common.sleep(15);
        log.info("debounce: executed {} times.", debounceRunnable.getCount());

        CountableRunnable throttleRunnable = new CountableRunnable("biu~");

        Throttle throttle = Throttle.newBuilder()
                .interval(500)
                .runnable(throttleRunnable)
                .build();

        for(int i = 0; i < 1000; i ++){
            throttle.run(
                    // throttleRunnable
            );
            Common.sleep(Common.random(15));
        }
        Common.sleep(500);
        log.info("throttle: executed {} times.", throttleRunnable.getCount());
    }

}

```

```txt
2020-12-14 19:34:38.833 [main][test.org.coodex.util.FDDemo.main(FDDemo.java:47)]
[INFO] debounce: executed 44 times.
```

```txt
2020-12-14 19:34:47.339 [main][test.org.coodex.util.FDDemo.main(FDDemo.java:63)]
[INFO] throttle: executed 17 times.
```

可以看到，原本需要执行2000次的场景，只进行了60次上下

## 并行任务

`org.coodex.concurrent.Parallel` 提供了一个多任务并行处理功能，从根本上来讲，其实就是简化了`CountDownLatch`的使用。

```java
package org.coodex.concrete.demo.boot;

import com.alibaba.fastjson.JSON;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.concurrent.Parallel;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParallelDemo {
    private final static Logger log = LoggerFactory.getLogger(ParallelDemo.class);


    public static void main(String[] args) {
        List<Runnable> runnables = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            runnables.add(() -> {
                try {
                    Clock.sleep(Common.random(3000));
                    log.info("task {} finished.", finalI);
                } catch (InterruptedException ignore) {
                }
            });
        }

        Parallel.Batch batch = new Parallel(
                // 5个线程来执行并行任务
                ExecutorsHelper.newFixedThreadPool(5, "parallel")
        ).run(runnables.toArray(new Runnable[0]));
        log.info("all task finished.");
        log.info("\n{}", JSON.toJSONString(batch, true));
    }
}
```

```txt
14:48:34.945 [parallel-3] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 2 finished.
14:48:35.908 [parallel-5] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 4 finished.
14:48:37.242 [parallel-2] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 1 finished.
14:48:37.288 [parallel-4] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 3 finished.
14:48:37.377 [parallel-1] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 0 finished.
14:48:37.398 [parallel-3] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 5 finished.
14:48:37.776 [parallel-3] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 10 finished.
14:48:38.640 [parallel-5] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 6 finished.
14:48:38.827 [parallel-4] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 8 finished.
14:48:39.782 [parallel-4] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 13 finished.
14:48:39.892 [parallel-4] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 14 finished.
14:48:39.910 [parallel-1] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 9 finished.
14:48:39.991 [parallel-2] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 7 finished.
14:48:40.596 [parallel-5] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 12 finished.
14:48:40.719 [parallel-3] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 11 finished.
14:48:41.043 [parallel-2] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 17 finished.
14:48:41.321 [parallel-3] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 19 finished.
14:48:41.478 [parallel-5] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 18 finished.
14:48:42.145 [parallel-4] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 15 finished.
14:48:42.333 [parallel-1] INFO org.coodex.concrete.demo.boot.ParallelDemo - task 16 finished.
14:48:42.333 [main] INFO org.coodex.concrete.demo.boot.ParallelDemo - all task finished.
```

可以看到，main线程等到所有任务执行完成后才输出`all task finished.`

```json
{
	"end":1565765322333,
	"id":"2c963b1966f941e2870fcdd68fdb74f6",
	"start":1565765314391,
	"tasks":[
		{
			"end":1565765317377,
			"finished":true,
			"id":1,
			"start":1565765314393,
			"timeConsuming":2984
		},
		{
			"end":1565765317242,
			"finished":true,
			"id":2,
			"start":1565765314393,
			"timeConsuming":2849
		},
		{
			"end":1565765314945,
			"finished":true,
			"id":3,
			"start":1565765314394,
			"timeConsuming":551
		},
		{
			"end":1565765317288,
			"finished":true,
			"id":4,
			"start":1565765314394,
			"timeConsuming":2894
		},
		{
			"end":1565765315908,
			"finished":true,
			"id":5,
			"start":1565765314397,
			"timeConsuming":1511
		},
		{
			"end":1565765317398,
			"finished":true,
			"id":6,
			"start":1565765314945,
			"timeConsuming":2453
		},
		{
			"end":1565765318640,
			"finished":true,
			"id":7,
			"start":1565765315908,
			"timeConsuming":2732
		},
		{
			"end":1565765319991,
			"finished":true,
			"id":8,
			"start":1565765317242,
			"timeConsuming":2749
		},
		{
			"end":1565765318827,
			"finished":true,
			"id":9,
			"start":1565765317288,
			"timeConsuming":1539
		},
		{
			"end":1565765319910,
			"finished":true,
			"id":10,
			"start":1565765317377,
			"timeConsuming":2533
		},
		{
			"end":1565765317776,
			"finished":true,
			"id":11,
			"start":1565765317398,
			"timeConsuming":378
		},
		{
			"end":1565765320719,
			"finished":true,
			"id":12,
			"start":1565765317776,
			"timeConsuming":2943
		},
		{
			"end":1565765320596,
			"finished":true,
			"id":13,
			"start":1565765318640,
			"timeConsuming":1956
		},
		{
			"end":1565765319782,
			"finished":true,
			"id":14,
			"start":1565765318827,
			"timeConsuming":955
		},
		{
			"end":1565765319892,
			"finished":true,
			"id":15,
			"start":1565765319782,
			"timeConsuming":110
		},
		{
			"end":1565765322145,
			"finished":true,
			"id":16,
			"start":1565765319892,
			"timeConsuming":2253
		},
		{
			"end":1565765322333,
			"finished":true,
			"id":17,
			"start":1565765319910,
			"timeConsuming":2423
		},
		{
			"end":1565765321043,
			"finished":true,
			"id":18,
			"start":1565765319991,
			"timeConsuming":1052
		},
		{
			"end":1565765321478,
			"finished":true,
			"id":19,
			"start":1565765320596,
			"timeConsuming":882
		},
		{
			"end":1565765321321,
			"finished":true,
			"id":20,
			"start":1565765320719,
			"timeConsuming":602
		}
	],
	"timeConsuming":7942
}
```

返回值对象中，会把每个任务的信息和总览详细返回。
