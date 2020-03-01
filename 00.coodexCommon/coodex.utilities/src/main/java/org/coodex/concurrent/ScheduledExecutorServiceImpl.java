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

package org.coodex.concurrent;

import org.coodex.util.Clock;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class ScheduledExecutorServiceImpl extends ExecutorServiceImpl implements ScheduledExecutorService {
    private final ScheduledExecutorService scheduledExecutorService;

    ScheduledExecutorServiceImpl(ScheduledExecutorService scheduledExecutorService) {
        super(scheduledExecutorService);
        this.scheduledExecutorService = scheduledExecutorService;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        long d = Clock.toMillis(delay, unit);
        return scheduledExecutorService.schedule(wrapper.wrap(command, delay, unit), d, TimeUnit.MILLISECONDS);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        long d = Clock.toMillis(delay, unit);
        return scheduledExecutorService.schedule(wrapper.wrap(callable, delay, unit), d, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        long i = Clock.toMillis(initialDelay, unit);
        long p = Clock.toMillis(period, unit);
        return scheduledExecutorService.scheduleAtFixedRate(
                wrapper.wrap(command, initialDelay, period, unit),
                i, p, TimeUnit.MILLISECONDS
        );
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        long i = Clock.toMillis(initialDelay, unit);
        long d = Clock.toMillis(delay, unit);
        return scheduledExecutorService.scheduleAtFixedRate(
                wrapper.wrap(command, initialDelay, delay, unit),
                i, d, TimeUnit.MILLISECONDS
        );
    }
}
