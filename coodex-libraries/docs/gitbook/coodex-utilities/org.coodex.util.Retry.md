# 轻量级重试机制

在异步任务的场景中，为了保证正确及最终一致性，我们通常需要多次尝试直到任务成功或者尝试次数大于阈值。`org.coodex.util.Retry`基于这种场景提供了一个轻量级的解决方案。

示例：

```java
        Retry.newBuilder()
//                // 最大尝试次数
//                .maxTimes(3)
//                // 第一次执行延迟时间，默认为0
//                .initDelay(20, TimeUnit.SECONDS)
//                // 延迟策略
//                .next(new Retry.TimeUnitNextDelay(TimeUnit.SECONDS) {
//                    @Override
//                    protected long delay(int times) {
//                        return 5;
//                    }
//                })
//                // 指定调度线程池
//                .scheduler(ExecutorsHelper.newSingleThreadScheduledExecutor("test"))
//                // 指定任务执行线程池，ScheduledExecutorService的线程数没法伸缩，所以，通过两个线程池来完成，
//                // 维持一个较小的ScheduledExecutorService进行任务调度，使用可伸缩ExecutorService进行任务执行
//                .executor(ExecutorsHelper.newLinkedThreadPool(
//                        1, 16, Integer.MAX_VALUE >> 1, 1L, "test-executor"
//                ))
//                //指定任务名
//                .named("TaskTest")
//                // or supplier方式指定任务名
//                .named(() -> "TaskTest")
//                //每次失败触发
//                .onFailed((start, times, throwable) ->
//                        log.info("on failed: {}, {}, {}", Common.calendarToStr(start), times, throwable == null ? "" : throwable.getLocalizedMessage()))
//                // 当任务尝试数超出最大阈值依然失败时的handle
//                .onAllFailed((start, times) -> log.info("all failed"))
                .build()
                // 要多次尝试执行的任务
                .execute(times -> {
                    log.debug("times: {}", times);
                    if (times % 2 == 0) {
                        throw new RuntimeException("mock exception:" + times);
                    }
                    return times == 5;
                });
```
