/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

class ExecutorServiceImpl implements ExecutorService {

    private final static Logger log = LoggerFactory.getLogger(ExecutorServiceImpl.class);

    private static final ServiceLoader<ExecutableWrapper> executableWrapperLoader =
            new LazyServiceLoader<ExecutableWrapper>(new ExecutableWrapperImpl()) {
            };
    protected final ExecutableWrapper wrapper;
    private final ExecutorService executorService;

    ExecutorServiceImpl(ExecutorService executorService) {
        this.executorService = executorService;
        wrapper = executableWrapperLoader.get();
    }

    <V> Collection<? extends Callable<V>> wrap(Collection<? extends Callable<V>> coll) {
        List<Callable<V>> list = new ArrayList<>();
        for (Callable<V> c : coll) {
            list.add(wrapper.wrap(c));
        }
        return list;
    }

    protected Runnable wrapRunnable(Runnable runnable) {
        return runnable == null ? null : () -> {
            try {
                runnable.run();
            } catch (Throwable th) {
                log.warn("Runnable run failed: {}", th.getLocalizedMessage(), th);
            }
        };
    }

    protected <V> Callable<V> wrapCallable(Callable<V> callable) {
        return callable == null ? null : () -> {
            try {
                return callable.call();
            } catch (Throwable th) {
                log.warn("Callable call failed: {}", th.getLocalizedMessage(), th);
                throw th;
            }
        };

    }


    @Override
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(wrapper.wrap(wrapCallable(task)));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(wrapper.wrap(wrapRunnable(task)), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(wrapper.wrap(wrapRunnable(task)));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(wrap(tasks.stream().map(this::wrapCallable).collect(Collectors.toList())));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(wrap(tasks.stream().map(this::wrapCallable).collect(Collectors.toList())), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(wrap(tasks.stream().map(this::wrapCallable).collect(Collectors.toList())));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(wrap(tasks.stream().map(this::wrapCallable).collect(Collectors.toList())), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(wrapper.wrap(wrapRunnable(command)));
    }
}
