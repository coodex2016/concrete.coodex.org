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

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by davidoff shen on 2016-09-05.
 */
public class ExecutorsHelper {
    private ExecutorsHelper() {
    }


    private static PriorityRunnable getPriorityRunnable(Runnable runnable) {
        return runnable instanceof PriorityRunnable ?
                (PriorityRunnable) runnable :
                new PriorityRunnable(Thread.NORM_PRIORITY, runnable);
    }

    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize, int maxWait, String namePrefix) {
        return newPriorityThreadPool(coreSize, maxSize, maxWait, 60L, namePrefix);
    }

    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize, int maxWait, long keepAliveTime, String namePrefix) {
        final PoolSize poolSize = new PoolSize(coreSize, maxSize).invoke();
        return newThreadPool(keepAliveTime, namePrefix, poolSize, new CoodexPriorityBlockingQueue(maxWait));
    }

    private static ExecutorService newThreadPool(long keepAliveTime, String namePrefix, PoolSize poolSize, CoodexBlockingQueue priorityBlockingQueue) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                poolSize.getFinalCoreSize(),
                poolSize.getFinalMaxSize(),
                keepAliveTime, TimeUnit.SECONDS,
                priorityBlockingQueue,
                new DefaultNamedThreadFactory(namePrefix)
        ) {
            @Override
            public void execute(Runnable command) {
                synchronized (this) {
                    super.execute(command);
                }
            }
        };
        priorityBlockingQueue.setThreadPoolExecutor(threadPool);
        return ExecutorWrapper.wrap(threadPool);
    }

    public static ExecutorService newLinkedThreadPool(final int coreSize, int maxSize, int maxWait, String namePrefix) {
        return newLinkedThreadPool(coreSize, maxSize, maxWait, 60L, namePrefix);
    }

    public static ExecutorService newLinkedThreadPool(final int coreSize, int maxSize, int maxWait, long keepAliveTime, String namePrefix) {
        final PoolSize poolSize = new PoolSize(coreSize, maxSize).invoke();
        return newThreadPool(keepAliveTime, namePrefix, poolSize, new CoodexLinkedBlockingQueue(maxWait));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, String namePrefix) {
        return newFixedThreadPool(nThreads, new DefaultNamedThreadFactory(namePrefix));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newFixedThreadPool(nThreads, threadFactory));
    }

    public static ExecutorService newSingleThreadExecutor(String namePrefix) {
        return newSingleThreadExecutor(new DefaultNamedThreadFactory(namePrefix));
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newSingleThreadExecutor(threadFactory));
    }

    public static ExecutorService newCachedThreadPool(String namePrefix) {
        return newCachedThreadPool(new DefaultNamedThreadFactory(namePrefix));
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newCachedThreadPool(threadFactory));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String namePrefix) {
        return newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory(namePrefix));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newSingleThreadScheduledExecutor(threadFactory));
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String namePrefix) {
        return newScheduledThreadPool(corePoolSize, new DefaultNamedThreadFactory(namePrefix));
    }

    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newScheduledThreadPool(corePoolSize, threadFactory));
    }

    public static void shutdownAll() {
        ExecutorWrapper.shutdown();
    }

    public static List<Runnable> shutdownAllNOW() {
        return ExecutorWrapper.shutdownNow();
    }

    interface CoodexBlockingQueue extends BlockingQueue<Runnable> {
        void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor);
    }

    /**
     * 基于 Executors.DefaultThreadFactory 改造
     */
    static class DefaultNamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultNamedThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix == null ? ("coodex-pool-" + poolNumber.getAndIncrement() + "-thread") : namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + "-" + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    static class CoodexLinkedBlockingQueue extends LinkedBlockingQueue<Runnable>
            implements CoodexBlockingQueue {

        private final int maximumSize;
        private ThreadPoolExecutor threadPoolExecutor;

        public CoodexLinkedBlockingQueue(int maximumSize) {
            this.maximumSize = maximumSize;
        }

        @Override
        public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
            this.threadPoolExecutor = threadPoolExecutor;
        }

        @Override
        public boolean offer(Runnable runnable) {
//            if (threadPoolExecutor == null) return super.offer(runnable);
            return threadPoolExecutor.getPoolSize() >= threadPoolExecutor.getMaximumPoolSize() &&
                    size() < maximumSize &&
                    super.offer(runnable);
        }
    }

    static class CoodexPriorityBlockingQueue extends PriorityBlockingQueue<Runnable>
            implements CoodexBlockingQueue {
        private final int maximumSize;
        private ThreadPoolExecutor threadPoolExecutor;


        CoodexPriorityBlockingQueue(int maximumSize) {
            this.maximumSize = maximumSize;
        }

        @Override
        public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
            this.threadPoolExecutor = threadPoolExecutor;
        }


        @Override
        public boolean offer(Runnable runnable) {
            runnable = getPriorityRunnable(runnable);
            return threadPoolExecutor.getPoolSize() >= threadPoolExecutor.getMaximumPoolSize() &&
                    size() < maximumSize &&
                    super.offer(runnable);

        }
    }

    private static class PoolSize {
        private final int coreSize;
        private final int maxSize;
        private int finalCoreSize;
        private int finalMaxSize;

        public PoolSize(int coreSize, int maxSize) {
            this.coreSize = coreSize;
            this.maxSize = maxSize;
        }

        public int getFinalCoreSize() {
            return finalCoreSize;
        }

        public int getFinalMaxSize() {
            return finalMaxSize;
        }

        public PoolSize invoke() {
            finalCoreSize = Math.max(coreSize, 1);
            finalMaxSize = Math.max(maxSize, coreSize);
            return this;
        }
    }
}
