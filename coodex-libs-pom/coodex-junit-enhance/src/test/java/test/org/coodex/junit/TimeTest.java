/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.junit;

import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.junit.enhance.Context;
import org.coodex.junit.enhance.CoodexEnhanceTestRule;
import org.coodex.junit.enhance.Entry;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.junit.enhance.TestUtils.*;
import static org.coodex.util.Common.now;

public class TimeTest {

    private static ScheduledExecutorService scheduledExecutorService = ExecutorsHelper.newScheduledThreadPool(3, "scheduledExecutorService");

    private static ExecutorService executorService = ExecutorsHelper.newFixedThreadPool(3, "executorService");

    @Rule
    public CoodexEnhanceTestRule rule = new CoodexEnhanceTestRule();

    @Test
    @Context(name = "testCase_0001")
//    @MapContext(@Entry(key = "key", value = "testValue"))
    @Entry(key = "key2", value = "123")
    @Entry(key = "key3", value = "777")
    public void test() {
        AtomicInteger integer = new AtomicInteger(0);
        Runnable runnable = () -> {
            logger.info("\n\n times: {}", integer.incrementAndGet());
            logger.info("now: {},  next two years: {}", now(), TIME.nextYears(2));
            logger.info("{}", get("key"));
            logger.info("{}", get("key2"));
            logger.info("{}", get("key3"));
            logger.info("{}", testCaseName());
            logger.info("{}", now());
        };
        runnable.run();

        Callable<Void> callable = () -> {
            runnable.run();
            return null;
        };

        asyncRun(runnable);

        scheduledExecutorService.execute(runnable);

        executorService.execute(runnable);

        executorService.submit(runnable);

        executorService.submit(callable);

        scheduledExecutorService.schedule(runnable, 10, TimeUnit.SECONDS);


    }

    @Test
    public void test1() {
        System.out.println(TIME.nextWeek());
    }

    @Test
    public void test2() {
        System.out.println(TIME.nextMonths(2));
    }


    @Test
    public void test3() {
        System.out.println(TIME.nextDays(2));
    }

    @Test
    public void test4() {
        System.out.println(TIME.nextHours(2));
    }

}
