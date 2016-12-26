package cc.coodex.concurrent;

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
