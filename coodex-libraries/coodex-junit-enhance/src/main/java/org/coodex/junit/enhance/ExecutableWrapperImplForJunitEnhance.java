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

package org.coodex.junit.enhance;

import org.coodex.concurrent.ExecutableWrapper;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.coodex.junit.enhance.TestUtils.*;

public class ExecutableWrapperImplForJunitEnhance implements ExecutableWrapper {

    @Override
    public Runnable wrap(Runnable runnable) {
        Map<String, Object> objectMap = contextClone();
        return objectMap == null ? runnable : () -> {
            CONTEXT.set(objectMap);
            try {
                runnable.run();
            } finally {
                CONTEXT.remove();
            }
        };
    }

    @Override
    public <V> Callable<V> wrap(Callable<V> callable) {
        Map<String, Object> objectMap = contextClone();
        return objectMap == null ? callable : () -> {
            CONTEXT.set(objectMap);
            try {
                return callable.call();
            } finally {
                CONTEXT.remove();
            }
        };
    }

    @Override
    public Runnable wrap(Runnable command, long delay, TimeUnit unit) {
        Map<String, Object> objectMap = contextClone();
        return objectMap == null ? command : () -> {
            CONTEXT.set(objectMap);
            try {
                int x = (int) (unit.toMillis(delay));
                if (x > 0)
                    TIME.go(x);
                command.run();
            } finally {
                CONTEXT.remove();
            }
        };
    }

    @Override
    public <V> Callable<V> wrap(Callable<V> callable, long delay, TimeUnit unit) {
        Map<String, Object> objectMap = contextClone();
        return objectMap == null ? callable : () -> {
            CONTEXT.set(objectMap);
            try {
                int x = (int) (unit.toMillis(delay));
                if (x > 0)
                    TIME.go(x);
                return callable.call();
            } finally {
                CONTEXT.remove();
            }
        };
    }

    @Override
    public Runnable wrap(Runnable command, long initialDelay, long period, TimeUnit unit) {
        Map<String, Object> objectMap = contextClone();

        return objectMap == null ? command : new Runnable() {
            int times = 0;

            @Override
            public void run() {
                CONTEXT.set(objectMap);
                try {
                    int x = (int) (times++ == 0 ? unit.toMillis(initialDelay) : unit.toMillis(period));
                    if (x > 0)
                        TIME.go(x);
                    command.run();
                } finally {
                    CONTEXT.remove();
                }
            }
        };
    }

    @Override
    public <V> Callable<V> wrap(Callable<V> callable, long initialDelay, long period, TimeUnit unit) {
        Map<String, Object> objectMap = contextClone();
        return objectMap == null ? callable : new Callable<V>() {
            int times = 0;

            @Override
            public V call() throws Exception {
                CONTEXT.set(objectMap);
                try {
                    int x = (int) (times++ == 0 ? unit.toMillis(initialDelay) : unit.toMillis(period));
                    if (x > 0)
                        TIME.go(x);
                    return callable.call();
                } finally {
                    CONTEXT.remove();
                }
            }
        };
    }
}
