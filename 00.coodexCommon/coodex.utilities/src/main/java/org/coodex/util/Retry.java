/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import org.coodex.concurrent.ExecutorsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 重试模板
 */
public class Retry {

    private final static Logger log = LoggerFactory.getLogger(Retry.class);


    private static final Integer DEFAULT_MAX_TIMES = 5;
    private static final NextDelay DEFAULT_NEXT_DELAY = new TimeUnitNextDelay(TimeUnit.SECONDS) {
        @Override
        protected long delay(int times) {
            return 5;
        }
    };
    private static final Singleton<ScheduledExecutorService> SCHEDULED_EXECUTOR_SERVICE_SINGLETON = new Singleton<ScheduledExecutorService>(
            new Singleton.Builder<ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build() {
                    return ExecutorsHelper.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2, "retry");
                }
            }
    );
    private ScheduledExecutorService scheduledExecutorService;
    private Integer maxTimes;
    private long initDelay = 0L;
    private NextDelay nextDelay;
    private NameSupplier taskNameSupplier = null;
    private OnFailed onFailed = null;
    private AllFailedHandle allFailedHandle = null;


    // 第几次
    private int num = 1;
    // 状态
    private Status status = Status.INIT;
    private Task task;
    private Calendar start;


    private Retry() {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void execute(Task task) {
        if (task == null) throw new NullPointerException("task is null.");
        synchronized (this) {
            if (!Status.INIT.equals(status))
                throw new IllegalStateException("task has scheduled.");
            this.task = task;
            status = Status.WAITING;
        }
        postTask();
    }

    public void execute() {
        execute(this.task);
    }

    private String getTaskName() {
        return taskNameSupplier == null ? task.toString() : taskNameSupplier.getName();
    }

    private void postTask() {

        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (Retry.this) {
                    if (Status.FINISHED.equals(status)) return;
                    status = Status.RUNNING;
                    if (start == null) {
                        start = Clock.getCalendar();
                    }
                    Calendar thisTimes = Clock.getCalendar();

                    int times = num;
                    Throwable throwable = null;
                    boolean success = false;
                    try {
                        success = task.run(times);
                    } catch (Throwable th) {
                        throwable = th;
                    } finally {
                        num++;
                    }

                    if (success || num > maxTimes) {
                        status = Status.FINISHED;
                        if (success) {
                            if (log.isDebugEnabled())
                                log.debug("{} success. [{}]", getTaskName(), times);
                        } else {
                            if (log.isInfoEnabled())
                                log.info("{} all failed.", getTaskName());
                            onFailed(thisTimes, times, throwable);
                            if (allFailedHandle != null) {
                                try {
                                    allFailedHandle.allFailed(start, times);
                                } catch (Throwable t) {
                                    log.warn("handle error.", t);
                                }
                            }
                        }
                    } else {
                        onFailed(thisTimes, times, throwable);
                        if (throwable != null && log.isWarnEnabled()) {
                            log.warn("{} failed [{}] times. {}", getTaskName(), times,
                                    throwable.getLocalizedMessage(), throwable);
                        }

                        if (throwable == null && log.isInfoEnabled()) {
                            log.info("{} failed [{}] times.", getTaskName(), times);
                        }
                        status = Status.WAITING;
                        postTask();
                    }
                }
            }
        }, num == 1 ? initDelay : nextDelay.next(num), TimeUnit.MILLISECONDS);
    }

    public void onFailed(Calendar start, int times, Throwable throwable) {
        if (onFailed != null) {
            try {
                onFailed.onFailed(start, times, throwable);
            } catch (Throwable t) {
                log.warn("on failed handle error.", t);
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    /**
     * @param allFailedHandle allFailedHandle
     * @return
     * @see Builder#onAllFailed(AllFailedHandle)
     * @deprecated
     */
    @Deprecated
    public Retry handle(AllFailedHandle allFailedHandle) {
        synchronized (this) {
            if (status.status >= Status.RUNNING.getStatus())
                throw new IllegalStateException();
            this.allFailedHandle = allFailedHandle;
        }
        return this;
    }


    public enum Status {
        INIT(0),
        WAITING(1), // --> RUNNING
        RUNNING(2), // --> WAITING/ FINISHED
        FINISHED(3);

        private int status;

        Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public interface AllFailedHandle {
        void allFailed(Calendar start, int times);
    }

    @Deprecated
    public interface TaskNameSupplier extends NameSupplier {

    }

    public interface OnFailed {
        /**
         * @param start     本次任务何时开始
         * @param times     第几次
         * @param throwable 异常，如果运行无异常则为<code>null</code>
         */
        void onFailed(Calendar start, int times, Throwable throwable);
    }

    public interface Task {
        /**
         * @param times 第几次，从1开始
         * @return 是否完成
         * @throws Exception 异常
         */
        boolean run(int times) throws Exception;
    }

    /**
     * 第几次之后延迟多长时间重试
     */
    public interface NextDelay {
        /**
         * @param times 第几次
         * @return 毫秒数
         */
        long next(int times);
    }

    public static abstract class TimeUnitNextDelay implements NextDelay {
        private final TimeUnit unit;

        @SuppressWarnings("WeakerAccess")
        protected TimeUnitNextDelay(TimeUnit unit) {
            this.unit = unit;
        }

        protected abstract long delay(int times);

        @Override
        public long next(int times) {
            return unit.toMillis(delay(times));
        }
    }


    public static class Builder {

        private ScheduledExecutorService scheduledExecutorService;
        private Integer maxTimes;
        private long initDelay = 0L;
        private NextDelay nextDelay;
        private NameSupplier taskNameSupplier = null;
        private OnFailed onFailed = null;
        private AllFailedHandle allFailedHandle = null;

        private Builder() {
        }

        public Builder scheduler(ScheduledExecutorService scheduledExecutorService) {
            this.scheduledExecutorService = scheduledExecutorService;
            return this;
        }

        /**
         * @param maxRetryTimes 最大尝试次数，应不小于1，默认5次
         * @return Builder
         */
        public Builder maxTimes(int maxRetryTimes) {
            if (maxRetryTimes < 1) {
                throw new IllegalArgumentException("illegal maxRetryTimes: " + maxRetryTimes);
            }
            this.maxTimes = maxRetryTimes;
            return this;
        }

        /**
         * @param initDelay initDelay
         * @param unit      unit
         * @return Builder
         */
        public Builder initDelay(long initDelay, TimeUnit unit) {
            this.initDelay = unit.toMillis(initDelay);
            return this;
        }


        /**
         * @param nextDelay nextDelay，默认每5秒执行一次
         * @return Builder
         */
        public Builder next(NextDelay nextDelay) {
            this.nextDelay = nextDelay;
            return this;
        }

        /**
         * @param name 任务名
         * @return Builder
         */
        public Builder named(final String name) {
            this.taskNameSupplier = new NameSupplier() {
                @Override
                public String getName() {
                    return name;
                }
            };
            return this;
        }

        /**
         * @param taskNameSupplier 任务名Supplier
         * @return Builder
         */
        public Builder named(NameSupplier taskNameSupplier) {
            this.taskNameSupplier = taskNameSupplier;
            return this;
        }

        /**
         * @param onFailed 每次失败时的处理
         * @return Builder
         */
        public Builder onFailed(OnFailed onFailed) {
            this.onFailed = onFailed;
            return this;
        }

        /**
         * @param allFailedHandle 全部失败时的处理
         * @return Builder
         */
        public Builder onAllFailed(AllFailedHandle allFailedHandle) {
            this.allFailedHandle = allFailedHandle;
            return this;
        }

        /**
         * @return Retry 实例
         */
        public Retry build() {
            Retry retry = new Retry();
            retry.initDelay = this.initDelay;
            retry.maxTimes = this.maxTimes == null ? DEFAULT_MAX_TIMES : this.maxTimes;
            retry.nextDelay = this.nextDelay == null ? DEFAULT_NEXT_DELAY : this.nextDelay;
            retry.scheduledExecutorService = this.scheduledExecutorService == null ?
                    SCHEDULED_EXECUTOR_SERVICE_SINGLETON.get() :
                    this.scheduledExecutorService;
            retry.taskNameSupplier = this.taskNameSupplier;
            retry.onFailed = onFailed;
            retry.allFailedHandle = allFailedHandle;
            return retry;
        }
    }
}
