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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ExecutableWrapperImpl implements ExecutableWrapper {
    @Override
    public Runnable wrap(Runnable runnable) {
        return runnable;
    }

    @Override
    public <T> Callable<T> wrap(Callable<T> callable) {
        return callable;
    }

    @Override
    public Runnable wrap(Runnable command, long delay, TimeUnit unit) {
        return command;
    }

    @Override
    public <V> Callable<V> wrap(Callable<V> callable, long delay, TimeUnit unit) {
        return callable;
    }

    @Override
    public Runnable wrap(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return command;
    }

    @Override
    public <V> Callable<V> wrap(Callable<V> callable, long initialDelay, long period, TimeUnit unit) {
        return callable;
    }
}
