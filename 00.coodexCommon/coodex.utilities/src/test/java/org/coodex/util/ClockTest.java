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

package org.coodex.util;


import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.clock.ClockAgentService;
import org.coodex.util.clock.DefaultClockAgent;
import org.coodex.util.clock.RemoteClockAgent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClockTest {

    private static ScheduledExecutorService scheduledExecutorService = ExecutorsHelper.newScheduledThreadPool(5,"ClockTest");

//    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private static void poll(final CountDownLatch countDownLatch) {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(Common.now("yyyy-MM-dd HH:mm:ss.SSS"));
                countDownLatch.countDown();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static void main(String[] args) throws InterruptedException {
        System.setProperty(Clock.KEY_MAGNIFICATION, Float.toString(300f));
        System.setProperty(DefaultClockAgent.KEY_BASELINE, "2019-01-28 12:00:00");
        final ClockAgentService service = new ClockAgentService();
        service.start();

        new Thread() {
            @Override
            public void run() {
                System.setProperty(RemoteClockAgent.KEY_REMOTE_HOST, "localhost");
                final RemoteClockAgent remoteClockAgent = new RemoteClockAgent();
                final CountDownLatch countDownLatch = new CountDownLatch(20);
                scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        countDownLatch.countDown();
                        System.out.println(Common.calendarToStr(
                                remoteClockAgent.getCalendar(),
                                "yyyy-MM-dd HH:mm:ss.SSS"));
                    }
                }, 0, 1, TimeUnit.MINUTES);

                try {
                    countDownLatch.await();
                    scheduledExecutorService.shutdown();
                    service.shutdown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();

//        CountDownLatch countDownLatch = new CountDownLatch(20);
//        poll(countDownLatch);
//        countDownLatch.await();
//        ExecutorsHelper.shutdownAll();
    }
}
