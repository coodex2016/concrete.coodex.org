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

import org.coodex.util.Common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * 提供一个多任务并行处理功能
 */
public class Parallel {

    private static RunnerWrapper defaultWrapper = new RunnerWrapper() {
        @Override
        public Runnable wrap(Runnable runnable) {
            return runnable;
        }
    };
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
        this.wrapper = wrapper == null ? defaultWrapper : wrapper;
    }

    public Batch run(Runnable... runnables) {

        Batch batch = new Batch();
        batch.start = Calendar.getInstance();

        if (runnables != null && runnables.length > 0) {
            CountDownLatch latch = new CountDownLatch(runnables.length);
            int i = 1;
            for (Runnable runnable : runnables) {
                batch.getTasks().add(newTask(
                        wrapper.wrap(runnable),
                        i++, latch/*, batch*/));
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        batch.end = Calendar.getInstance();
        return batch;
    }

    private Task newTask(final Runnable runnable, int i, final CountDownLatch latch/*, final Object lock*/) {
        final Task task = new Task();
        task.id = i;
        Runnable run = new Runnable() {
            @Override
            public void run() {
                task.start = Calendar.getInstance();
                try {
                    runnable.run();
                } catch (Throwable th) {
                    task.throwable = th;
                } finally {
                    task.end = Calendar.getInstance();
                    task.finished = true;
                    latch.countDown();
//                    synchronized (lock) {
//                        lock.notify();
//                    }
                }
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

    public static class Task {
        private Calendar start;
        private Calendar end;
        private Throwable throwable;
        private boolean finished = false;
        private Integer id;

        public long getTimeConsuming() {
            return end.getTimeInMillis() - start.getTimeInMillis();
        }

        public Calendar getStart() {
            return start;
        }

        public Calendar getEnd() {
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

    public static class Batch {
        private List<Task> tasks = new ArrayList<Task>();
        private String id = Common.getUUIDStr();
        private Calendar start;
        private Calendar end;

        public long getTimeConsuming() {
            return end.getTimeInMillis() - start.getTimeInMillis();
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public String getId() {
            return id;
        }


        public Calendar getStart() {
            return start;
        }

        public Calendar getEnd() {
            return end;
        }

//        public boolean isAllFinished() {
//            for (Task task : tasks) {
//                if (!task.isFinished()) return false;
//            }
//            return true;
//        }

    }

}
