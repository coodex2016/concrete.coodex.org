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

import org.coodex.concurrent.components.PriorityRunnable;

import java.util.AbstractQueue;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by davidoff shen on 2016-09-05.
 */
public class ExecutorsHelper {

    static class ConcreteLinkedBlockingQueue extends LinkedBlockingQueue<Runnable> {
        private ThreadPoolExecutor threadPoolExecutor;
        private int maximumPoolSize;

        public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
            this.threadPoolExecutor = threadPoolExecutor;
            this.maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();
        }

        @Override
        public boolean offer(Runnable runnable) {
            if (threadPoolExecutor == null) return super.offer(runnable);
            return threadPoolExecutor.getPoolSize() < maximumPoolSize ?
                    false : super.offer(runnable);
        }
    }


    static class ConcretePriorityBlockingQueue extends PriorityBlockingQueue<Runnable> {
        private ThreadPoolExecutor threadPoolExecutor;
        private int maximumPoolSize;


        public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
            this.threadPoolExecutor = threadPoolExecutor;
            this.maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();
        }


        @Override
        public boolean offer(Runnable runnable) {
            if (threadPoolExecutor == null) return super.offer(runnable);
            runnable = getPriorityRunnable(runnable);
            return threadPoolExecutor.getPoolSize() < maximumPoolSize ?
                    false : super.offer(runnable);
        }
    }

    private static PriorityRunnable getPriorityRunnable(Runnable runnable) {
        return runnable instanceof PriorityRunnable ?
                (PriorityRunnable) runnable :
                new PriorityRunnable(Thread.NORM_PRIORITY, runnable);
    }


    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize) {
        return newPriorityThreadPool(coreSize, maxSize, 60L);
    }

    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize, long keepAliveTime) {
        int finalCoreSize = Math.max(coreSize, 1);
        int finalMaxSize = maxSize >= coreSize ? maxSize : Integer.MAX_VALUE;
        if (finalMaxSize == Integer.MAX_VALUE) finalMaxSize = Integer.MAX_VALUE;

        ConcretePriorityBlockingQueue priorityBlockingQueue = new ConcretePriorityBlockingQueue();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                finalCoreSize, finalMaxSize, keepAliveTime, TimeUnit.SECONDS,
                priorityBlockingQueue
        );
        priorityBlockingQueue.setThreadPoolExecutor(threadPool);
        return ExecutorWrapper.wrap(threadPool);
    }

    public static ExecutorService newLinkedThreadPool(final int coreSize, int maxSize, long keepAliveTime) {
        int finalCoreSize = Math.max(coreSize, 1);
        int finalMaxSize = maxSize >= coreSize ? maxSize : Integer.MAX_VALUE;
        if (finalMaxSize == Integer.MAX_VALUE) finalMaxSize = Integer.MAX_VALUE;

        ConcreteLinkedBlockingQueue linkedBlockingQueue = new ConcreteLinkedBlockingQueue();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                finalCoreSize, finalMaxSize, keepAliveTime, TimeUnit.SECONDS,
                linkedBlockingQueue
        );
        linkedBlockingQueue.setThreadPoolExecutor(threadPool);
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
