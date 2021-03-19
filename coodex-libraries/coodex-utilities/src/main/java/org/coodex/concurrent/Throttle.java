package org.coodex.concurrent;

import org.coodex.util.Clock;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Throttle implements FrequencyReducer {

    private ScheduledFuture<?> prevFuture;
    private volatile long prevTimestamp;
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void submit() {
        submit(runnable);
    }

    private long next() {
        long n = Clock.currentTimeMillis() - prevTimestamp;
        return n > interval ? 0 : Math.max(n, 0);
    }

    @Override
    public void submit(Runnable runnable) {
        if (runnable == null) throw new NullPointerException("runnable instance is null.");
        lock.lock();
        try {
            if (prevFuture != null) prevFuture.cancel(false);

            Runnable body = () -> {
                lock.lock();
                try {
                    prevTimestamp = Clock.currentTimeMillis();
                    runnable.run();
                } finally {
                    prevFuture = null;
                    lock.unlock();
                }
            };

            long n = next();
            if (n == 0) {
                if (asyncAlways)
                    scheduledExecutorService.execute(body);
                else
                    body.run();
            } else {
                prevFuture = scheduledExecutorService.schedule(body, interval - n, TimeUnit.MILLISECONDS);
            }

        } finally {
            lock.unlock();
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private long interval = 100L;
        private boolean asyncAlways = false;
        private ScheduledExecutorService scheduledExecutorService;
        private Runnable runnable;

        private Builder() {
        }

        public Builder interval(long interval) {
            this.interval = interval;
            return this;
        }

        public Builder asyncAlways(boolean asyncAlways) {
            this.asyncAlways = asyncAlways;
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

        public Throttle build(){
            return new Throttle(this);
        }

    }

    private final long interval;
    private final boolean asyncAlways;
    private final Runnable runnable;
    private final ScheduledExecutorService scheduledExecutorService;

    private Throttle(Builder builder) {
        this.interval = Math.max(0, builder.interval);
        this.asyncAlways = builder.asyncAlways;
        this.runnable = builder.runnable;
        this.scheduledExecutorService = builder.scheduledExecutorService == null ?
                DEFAULT_REDUCER_EXECUTOR_SERVICE_SINGLETON.get() :
                builder.scheduledExecutorService;
    }
}
