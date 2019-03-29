/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concurrent;


import org.coodex.util.Clock;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Throttler<T> extends AbstractCoalition<T> {
    private ScheduledFuture prevFuture = null;
    private long prevTime = 0;


    public Throttler(Callback<T> c, int interval, ScheduledExecutorService scheduledExecutorService) {
        super(c, interval, scheduledExecutorService);
    }
    public Throttler(Callback<T> c, int interval) {
        super(c, interval);
    }

//    public static void main(String[] args) throws InterruptedException {
//        Throttler<String> throttler = new Throttler<String>(new Coalition.Callback<String>() {
//            @Override
//            public void call(String arg) {
//                System.out.println(arg);
//            }
//        }, 200);
//
//        for (int i = 0; i < 410; i++) {
//            Thread.sleep(20);
//            throttler.call(String.format("%d", i));
//        }
//
//
//        Thread.sleep(200);
//        throttler.terminate();
//    }

    private long getNextThrottle() {
        long l = Clock.currentTimeMillis() - prevTime;
        return l > interval ? 0 : l;
    }

    public void call(final T key) {
        synchronized (this) {
            if (prevFuture != null)
                prevFuture.cancel(true);

            long next = getNextThrottle();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (Throttler.this) {
                        prevTime = Clock.currentTimeMillis();
                        prevFuture = null;
                        callback.call(key);
                    }
                }
            };


            if (next == 0) {
                runnable.run();
            } else
                prevFuture = scheduledExecutorService.schedule(runnable, interval / 2, TimeUnit.MILLISECONDS);
        }
    }
}
