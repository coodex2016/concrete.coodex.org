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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

class ExecutorServiceImpl implements ExecutorService {

    private static LazyServiceLoader<ExecutableWrapper> executableWrapperLoader =
            new LazyServiceLoader<ExecutableWrapper>(new ExecutableWrapperImpl()) {
            };

    private final ExecutorService executorService;
    protected final ExecutableWrapper wrapper;

    ExecutorServiceImpl(ExecutorService executorService) {
        this.executorService = executorService;
        wrapper = executableWrapperLoader.get();
    }

    <V> Collection<? extends Callable<V>> wrap(Collection<? extends Callable<V>> coll) {
        List<Callable<V>> list = new ArrayList<Callable<V>>();
        for (Callable<V> c : coll) {
            list.add(wrapper.wrap(c));
        }
        return list;
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
        return awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(wrapper.wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(wrapper.wrap(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(wrapper.wrap(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return invokeAll(wrap(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return invokeAll(wrap(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return invokeAny(wrap(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return invokeAny(wrap(tasks), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(wrapper.wrap(command));
    }
}
