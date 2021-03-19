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

/**
 * @param <T>
 * @deprecated 2020-12-11 降频重构
 */
@SuppressWarnings("unused")
@Deprecated
public class Throttler<T> extends AbstractCoalition<T> {
    private final boolean asyncAlways;
    private ScheduledFuture<?> prevFuture = null;
    private long prevTime = 0;

    public Throttler(Callback<T> c, long interval, boolean asyncAlways) {
        super(c, interval);
        this.asyncAlways = asyncAlways;
    }

    public Throttler(Callback<T> c, long interval, boolean asyncAlways, ScheduledExecutorService scheduledExecutorService) {
        super(c, interval, scheduledExecutorService);
        this.asyncAlways = asyncAlways;
    }

    public Throttler(Callback<T> c, long interval, ScheduledExecutorService scheduledExecutorService) {
        this(c, interval, false, scheduledExecutorService);
    }

    public Throttler(Callback<T> c, int interval) {
        this(c, interval, false);
    }

    private long getNextThrottle() {
        long l = Clock.currentTimeMillis() - prevTime;
        return l > interval ? 0 : l;
    }

    public void call(final T key) {
        synchronized (this) {
            if (prevFuture != null)
                prevFuture.cancel(true);

            long next = getNextThrottle();

            Runnable runnable = () -> {
                synchronized (Throttler.this) {
                    prevTime = Clock.currentTimeMillis();
                    prevFuture = null;
                    callback.call(key);
                }
            };


            if (next == 0) {
                if (asyncAlways) {
                    scheduledExecutorService.execute(runnable);
                } else {
                    runnable.run();
                }
            } else
                prevFuture = scheduledExecutorService.schedule(runnable, interval / 2, TimeUnit.MILLISECONDS);
        }
    }
}
