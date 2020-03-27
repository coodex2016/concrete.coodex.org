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

import org.coodex.util.Clock;
import org.coodex.util.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * 提供一个多任务并行处理功能
 */
public class Parallel {

    //    private static RunnerWrapper defaultWrapper = new RunnerWrapper() {
//        @Override
//        public Runnable wrap(Runnable runnable) {
//            return runnable;
//        }
//    };
    private final ExecutorService executorService;
    private final RunnerWrapper wrapper;

    public Parallel() {
        this(null);
    }

    public Parallel(ExecutorService executorService) {
        this(executorService, null);
    }

    public Parallel(ExecutorService executorService, RunnerWrapper wrapper) {
        this.executorService = executorService;
        this.wrapper = wrapper;
    }

    public Batch run(Runnable... runnables) {

        Batch batch = new Batch();
        batch.start = Clock.currentTimeMillis();

        if (runnables != null && runnables.length > 0) {
            CountDownLatch latch = new CountDownLatch(runnables.length);
            int i = 1;
            for (Runnable runnable : runnables) {
                batch.getTasks().add(newTask(
                        wrapper == null ? runnable : wrapper.wrap(runnable),
                        i++, latch/*, batch*/));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
//            while (!batch.isAllFinished()) {
//                synchronized (batch) {
//                    try {
//                        batch.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
        batch.end = Clock.currentTimeMillis();
        return batch;
    }

    @SafeVarargs
    public final <V> CallableBatch<V> call(Callable<V>... callable) {
        return call(null, callable);
    }

    @SafeVarargs
    public final <V> CallableBatch<V> call(CallableWrapper<V> wrapper, Callable<V>... callable) {

        CallableBatch<V> batch = new CallableBatch<>();
        batch.start = Clock.currentTimeMillis();

        if (callable != null && callable.length > 0) {
            CountDownLatch latch = new CountDownLatch(callable.length);
            int i = 1;
            for (Callable<V> c : callable) {
                batch.getTasks().add(newTask(
                        wrapper == null ? c : wrapper.wrap(c),
                        i++, latch/*, batch*/));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
//            while (!batch.isAllFinished()) {
//                synchronized (batch) {
//                    try {
//                        batch.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
        }
        batch.end = Clock.currentTimeMillis();
        return batch;
    }

    private Task newTask(final Runnable runnable, int i, final CountDownLatch latch/*, final Object lock*/) {
        final Task task = new Task();
        task.id = i;
        Runnable run = () -> {
            task.start = Clock.currentTimeMillis();
            try {
                runnable.run();
            } catch (Throwable th) {
                task.throwable = th;
            } finally {
                task.end = Clock.currentTimeMillis();
                task.finished = true;
                latch.countDown();
            }
        };

        if (executorService != null) {
            executorService.execute(run);
        } else {
            new Thread(run).start();
        }
        return task;
    }

    private <V> CallableTask<V> newTask(final Callable<V> callable, int i, final CountDownLatch latch) {
        final CallableTask<V> task = new CallableTask<>();
        task.id = i;
        Runnable run = () -> {
            task.start = Clock.currentTimeMillis();
            try {
                task.result = callable.call();
            } catch (Throwable th) {
                task.throwable = th;
            } finally {
                task.end = Clock.currentTimeMillis();
                task.finished = true;
                latch.countDown();
            }
        };

        if (executorService != null) {
            executorService.execute(run);
        } else {
            new Thread(run).start();
        }
        return task;
    }

    public interface RunnerWrapper {
        Runnable wrap(Runnable runnable);
    }

    public interface CallableWrapper<V> {
        Callable<V> wrap(Callable<V> runnable);
    }

    public static class Task {
        long start;
        long end;
        Throwable throwable;
        boolean finished = false;
        Integer id;

        @SuppressWarnings("unused")
        public long getTimeConsuming() {
            return end - start;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public Integer getId() {
            return id;
        }

        public boolean isFinished() {
            return finished;
        }
    }

    public static class CallableTask<V> extends Task {
        V result;

        public V getResult() {
            return result;
        }
    }


    private static class AbstractBatch {
        String id = Common.getUUIDStr();
        long start;
        long end;

        public long getTimeConsuming() {
            return end - start;
        }

        public String getId() {
            return id;
        }


        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }


    }


    public static class Batch extends AbstractBatch {
        private List<Task> tasks = new ArrayList<>();

        public List<Task> getTasks() {
            return tasks;
        }
    }

    public static class CallableBatch<V> extends AbstractBatch {
        private List<CallableTask<V>> tasks = new ArrayList<>();

        public List<CallableTask<V>> getTasks() {
            return tasks;
        }
    }
}
