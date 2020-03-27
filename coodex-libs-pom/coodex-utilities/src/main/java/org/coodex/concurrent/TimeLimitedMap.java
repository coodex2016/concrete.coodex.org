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


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeLimitedMap<K, V> {

    private final static AtomicInteger poolNumber = new AtomicInteger(1);
    private final ScheduledExecutorService scheduledExecutorService;
    private final long timeOut;
    private Map<K, Task<V>> tasks = new ConcurrentHashMap<K, Task<V>>();

    public TimeLimitedMap() {
        this(10000);
    }

    public TimeLimitedMap(long timeOut) {
        this(timeOut, ExecutorsHelper.newScheduledThreadPool(1, "TLM-life-cycle-" + poolNumber.getAndIncrement()));
    }


    public TimeLimitedMap(long timeOut, ScheduledExecutorService scheduledExecutorService) {
        this.timeOut = timeOut;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public void put(K key, V value, TimeoutCallback callback) {
        put(key, value, this.timeOut, callback);
    }

    public void put(final K key, V value, long timeOut, final TimeoutCallback callback) {
        if (tasks.containsKey(key)) {
            synchronized (tasks) {
                if (tasks.containsKey(key)) {
                    Task<V> task = tasks.remove(key);
                    task.future.cancel(true);
                }
            }
        }

        synchronized (tasks) {
            Task<V> task = new Task<V>();
            task.value = value;
            task.future = scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    V v = getAndRemove(key);
                    if (v != null && callback != null)
                        callback.timeout();
                }
            }, getTimeOut(timeOut), TimeUnit.MILLISECONDS);
            tasks.put(key, task);
        }

    }

    public V getAndRemove(K key) {
        Task<V> task = null;
        if (tasks.containsKey(key)) {
            synchronized (tasks) {
                if (tasks.containsKey(key)) {
                    task = tasks.remove(key);
                }
            }
        }
        if (task != null) {
            task.future.cancel(true);
            return task.value;
        }
        return null;
    }

    private long getTimeOut(long timeOut) {
        return timeOut > 0 ? timeOut : (this.timeOut > 0 ? this.timeOut : 10000l);
    }

    public interface TimeoutCallback {
        void timeout();
    }

    private static class Task<V> {
        ScheduledFuture<?> future;
        V value;
    }


}
