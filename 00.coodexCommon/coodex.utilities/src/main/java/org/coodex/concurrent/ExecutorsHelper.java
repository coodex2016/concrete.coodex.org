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

    private static PriorityRunnable getPriorityRunnable(Runnable runnable) {
        return runnable instanceof PriorityRunnable ?
                (PriorityRunnable) runnable :
                new PriorityRunnable(Thread.NORM_PRIORITY, runnable);
    }


    /**
     * 使用 {@link ExecutorsHelper#newPriorityThreadPool(int, int, String)} 替代
     *
     * @param coreSize
     * @param maxSize
     * @return
     * @deprecated
     */
    @Deprecated
    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize) {
        return newPriorityThreadPool(coreSize, maxSize, null);
    }

    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize, String namePrefix) {
        return newPriorityThreadPool(coreSize, maxSize, 60L, namePrefix);
    }

    /**
     * 使用 {@link ExecutorsHelper#newPriorityThreadPool(int, int, long, String)} 替代
     *
     * @param coreSize
     * @param maxSize
     * @param keepAliveTime
     * @return
     */
    @Deprecated
    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize, long keepAliveTime) {
        return newPriorityThreadPool(coreSize, maxSize, keepAliveTime, null);
    }

    public static ExecutorService newPriorityThreadPool(final int coreSize, int maxSize, long keepAliveTime, String namePrefix) {
        final PoolSize poolSize = new PoolSize(coreSize, maxSize).invoke();
//        int finalCoreSize = poolSize.getFinalCoreSize();
//        int finalMaxSize = poolSize.getFinalMaxSize();

        ConcretePriorityBlockingQueue priorityBlockingQueue = new ConcretePriorityBlockingQueue();
        return newThreadPool(keepAliveTime, namePrefix, poolSize, priorityBlockingQueue);
    }

    private static ExecutorService newThreadPool(long keepAliveTime, String namePrefix, PoolSize poolSize, ConcreteBlockingQueue priorityBlockingQueue) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                poolSize.getFinalCoreSize(),
                poolSize.getFinalMaxSize(),
                keepAliveTime, TimeUnit.SECONDS,
                priorityBlockingQueue,
                new DefaultNamedThreadFactory(namePrefix)
        );
        priorityBlockingQueue.setThreadPoolExecutor(threadPool);
        return ExecutorWrapper.wrap(threadPool);
    }

    /**
     * 使用 {@link ExecutorsHelper#newLinkedThreadPool(int, int, long, String)} 替代
     *
     * @param coreSize
     * @param maxSize
     * @param keepAliveTime
     * @return
     * @deprecated
     */
    public static ExecutorService newLinkedThreadPool(final int coreSize, int maxSize, long keepAliveTime) {
        return newLinkedThreadPool(coreSize, maxSize, keepAliveTime, null);
    }

    public static ExecutorService newLinkedThreadPool(final int coreSize, int maxSize, long keepAliveTime, String namePrefix) {
//        int finalCoreSize = Math.max(coreSize, 1);
//        int finalMaxSize = maxSize >= coreSize ? maxSize : Integer.MAX_VALUE;
//        if (finalMaxSize == Integer.MAX_VALUE) finalMaxSize = Integer.MAX_VALUE;
        final PoolSize poolSize = new PoolSize(coreSize, maxSize).invoke();
        ConcreteLinkedBlockingQueue linkedBlockingQueue = new ConcreteLinkedBlockingQueue();
        return newThreadPool(keepAliveTime, namePrefix, poolSize, linkedBlockingQueue);
//        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
//                poolSize.getFinalCoreSize(),
//                poolSize.getFinalMaxSize(),
//                keepAliveTime, TimeUnit.SECONDS,
//                linkedBlockingQueue,
//                new DefaultNamedThreadFactory(namePrefix)
//        );
//        linkedBlockingQueue.setThreadPoolExecutor(threadPool);
//        return ExecutorWrapper.wrap(threadPool);
    }


    /**
     * 使用 {@link ExecutorsHelper#newFixedThreadPool(int, String)} 替代
     *
     * @param nThreads
     * @return
     * @deprecated
     */
    public static ExecutorService newFixedThreadPool(int nThreads) {
//        return ExecutorWrapper.wrap(Executors.newFixedThreadPool(nThreads));
        return newFixedThreadPool(nThreads, (String) null);
    }

    public static ExecutorService newFixedThreadPool(int nThreads, String namePrefix) {
        return newFixedThreadPool(nThreads, new DefaultNamedThreadFactory(namePrefix));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newFixedThreadPool(nThreads, threadFactory));
    }

    /**
     * 使用 {@link ExecutorsHelper#newSingleThreadExecutor(String)} 替代
     *
     * @return
     * @deprecated
     */
    @Deprecated
    public static ExecutorService newSingleThreadExecutor() {
//        return ExecutorWrapper.wrap(Executors.newSingleThreadExecutor());
        return newSingleThreadExecutor((String) null);
    }

    public static ExecutorService newSingleThreadExecutor(String namePrefix) {
        return newSingleThreadExecutor(new DefaultNamedThreadFactory(namePrefix));
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newSingleThreadExecutor(threadFactory));
    }

    /**
     * 使用 {@link ExecutorsHelper#newCachedThreadPool(String)}替代
     *
     * @return
     * @deprecated
     */
    @Deprecated
    public static ExecutorService newCachedThreadPool() {
        return ExecutorWrapper.wrap(Executors.newCachedThreadPool());
    }

    public static ExecutorService newCachedThreadPool(String namePrefix) {
        return newCachedThreadPool(new DefaultNamedThreadFactory(namePrefix));
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newCachedThreadPool(threadFactory));
    }

    /**
     * 使用 {@link ExecutorsHelper#newSingleThreadScheduledExecutor(String)}替代
     *
     * @return
     */
    @Deprecated
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return newSingleThreadScheduledExecutor((String) null);
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String namePrefix) {
        return newSingleThreadScheduledExecutor(new DefaultNamedThreadFactory(namePrefix));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return ExecutorWrapper.wrap(Executors.newSingleThreadScheduledExecutor(threadFactory));
    }

    /**
     * 使用 {@link ExecutorsHelper#newScheduledThreadPool(int, String)} 替代
     *
     * @param corePoolSize
     * @return
     * @deprecated
     */
    @Deprecated
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return newScheduledThreadPool(corePoolSize, (String) null);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String namePrefix) {
//        return ExecutorWrapper.wrap(Executors.newScheduledThreadPool(corePoolSize,
//                new DefaultNamedThreadFactory(namePrefix)));
        return newScheduledThreadPool(corePoolSize, new DefaultNamedThreadFactory(namePrefix));
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

    interface ConcreteBlockingQueue extends BlockingQueue<Runnable> {
        void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor);
    }

    static class ConcreteLinkedBlockingQueue extends LinkedBlockingQueue<Runnable> implements ConcreteBlockingQueue {
        private ThreadPoolExecutor threadPoolExecutor;
        private int maximumPoolSize;

        @Override
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

    static class ConcretePriorityBlockingQueue extends PriorityBlockingQueue<Runnable>
            implements ConcreteBlockingQueue {
        private ThreadPoolExecutor threadPoolExecutor;
        private int maximumPoolSize;

        @Override
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

    private static class PoolSize {
        private int coreSize;
        private int maxSize;
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
            finalMaxSize = maxSize >= coreSize ? maxSize : Integer.MAX_VALUE;
            if (finalMaxSize == Integer.MAX_VALUE) finalMaxSize = Integer.MAX_VALUE;
            return this;
        }
    }
}
