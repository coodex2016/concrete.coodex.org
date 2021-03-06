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


import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @param <T>
 * @deprecated 2020-12-11 降频重构
 */
@Deprecated
public class Debouncer<T> extends AbstractCoalition<T> {
    private ScheduledFuture<?> prevFuture = null;

    public Debouncer(Callback<T> c, long interval, ScheduledExecutorService scheduledExecutorService) {
        super(c, interval, scheduledExecutorService);
    }

    public Debouncer(Callback<T> c, long interval) {
        super(c, interval);
    }

    public synchronized void call(final T key) {

        if (prevFuture != null)
            prevFuture.cancel(true);

        prevFuture = scheduledExecutorService.schedule(() -> {
            synchronized (Debouncer.this) {
                callback.call(key);
                prevFuture = null;
            }
        }, interval, TimeUnit.MILLISECONDS);
    }
}
