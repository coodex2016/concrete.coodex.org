package org.coodex.concurrent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Debounce implements FrequencyReducer {
    private final ScheduledExecutorService scheduledExecutorService;
    private final long idle;
    private final Runnable runnable;
    private final ReentrantLock lock = new ReentrantLock();

    private ScheduledFuture<?> prevFuture;

    private Debounce(Builder builder) {
        this.idle = Math.max(0, builder.idle);
        this.runnable = builder.runnable;
        this.scheduledExecutorService = builder.scheduledExecutorService == null ?
                DEFAULT_REDUCER_EXECUTOR_SERVICE_SINGLETON.get() :
                builder.scheduledExecutorService;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void submit() {
        submit(runnable);
    }

    @Override
    public void submit(Runnable runnable) {
        if (runnable == null) throw new NullPointerException("runnable instance is null.");

        lock.lock();
        try {
            if (prevFuture != null) {
                // 可重入锁控制执行体是完整的
                prevFuture.cancel(false);
            }

            prevFuture = scheduledExecutorService.schedule(() -> {
                lock.lock();
                try {
                    runnable.run();
                } finally {
                    prevFuture = null;
                    lock.unlock();
                }
            }, idle, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }

    }

    public void cancel() {
        if (prevFuture != null) {
            lock.lock();
            try {
                if (prevFuture != null) {
                    prevFuture.cancel(false);
                    prevFuture = null;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public static class Builder {
        private long idle = 100L;
        private ScheduledExecutorService scheduledExecutorService;
        private Runnable runnable;

        private Builder() {
        }

        public Builder idle(long idle) {
            this.idle = idle;
            return this;
        }

        public Builder scheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
            this.scheduledExecutorService = scheduledExecutorService;
            return this;
        }

        public Builder runnable(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public Debounce build() {
            return new Debounce(this);
        }
    }
}
