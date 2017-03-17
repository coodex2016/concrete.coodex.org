/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import java.util.List;
import java.util.concurrent.*;

/**
 * Created by davidoff shen on 2016-09-05.
 */
public class ExecutorsHelper {


    public static ExecutorService newPriorityThreadPool(int coreSize, int maxSize) {
        coreSize = Math.max(coreSize, 1);
        maxSize = maxSize >= coreSize ? maxSize : Integer.MAX_VALUE;
        ExecutorService threadPool = new ThreadPoolExecutor(
                coreSize, maxSize, 60L, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>()
        );

        return ExecutorWrapper.wrap(threadPool);
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return ExecutorWrapper.wrap(Executors.newFixedThreadPool(nThreads));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newFixedThreadPool(nThreads, threadFactory));
    }


    public static ExecutorService newSingleThreadExecutor() {
        return ExecutorWrapper.wrap(Executors.newSingleThreadExecutor());
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newSingleThreadExecutor(threadFactory));
    }


    public static ExecutorService newCachedThreadPool() {
        return ExecutorWrapper.wrap(Executors.newCachedThreadPool());
    }


    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newCachedThreadPool(threadFactory));
    }


    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return ExecutorWrapper.wrap(Executors.newSingleThreadScheduledExecutor());
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newSingleThreadScheduledExecutor(threadFactory));
    }


    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return ExecutorWrapper.wrap(Executors.newScheduledThreadPool(corePoolSize));
    }


    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newScheduledThreadPool(corePoolSize, threadFactory));
    }


    public static final void shutdownAll() {
        ExecutorWrapper.shutdown();
    }

    public static final List<Runnable> shutdownAllNOW() {
        return ExecutorWrapper.shutdownNow();
    }
}
