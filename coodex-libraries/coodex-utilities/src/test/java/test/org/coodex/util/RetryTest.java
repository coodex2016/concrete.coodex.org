/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.org.coodex.util;

import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Common;
import org.coodex.util.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class RetryTest {

    private final static Logger log = LoggerFactory.getLogger(RetryTest.class);


    public static void main(String[] args) {
        Retry.newBuilder()
                // 最大尝试次数
                .maxTimes(3)
                // 第一次执行延迟时间，默认为0
                .initDelay(20, TimeUnit.SECONDS)
                // 延迟策略
                .next(new Retry.TimeUnitNextDelay(TimeUnit.SECONDS) {
                    @Override
                    protected long delay(int times) {
                        return 5;
                    }
                })
                // 指定调度线程池
                .scheduler(ExecutorsHelper.newSingleThreadScheduledExecutor("test"))
                // 指定任务执行线程池，ScheduledExecutorService的线程数没法伸缩，所以，通过两个线程池来完成，
                // 维持一个较小的ScheduledExecutorService进行任务调度，使用可伸缩ExecutorService进行任务执行
                .executor(ExecutorsHelper.newLinkedThreadPool(
                        1, 16, Integer.MAX_VALUE >> 1, 1L, "test-executor"
                ))
                //指定任务名
                .named("TaskTest")
                // or supplier方式指定任务名
                .named(() -> "TaskTest")
                //每次失败触发
                .onFailed((start, times, throwable) ->
                        log.info("on failed: {}, {}, {}", Common.calendarToStr(start), times, throwable == null ? "" : throwable.getLocalizedMessage()))
                // 当任务尝试数超出最大阈值依然失败时的handle
                .onAllFailed((start, times) -> log.info("all failed"))
                .build()
                // 要多次尝试执行的任务
                .execute(times -> {
                    log.debug("times: {}", times);
                    if (times % 2 == 0) {
                        throw new RuntimeException("mock exception:" + times);
                    }
                    return times == 5;
                });
    }
}
