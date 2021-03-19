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

package org.coodex.concurrent.locks;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class SyncWrapper {

    public static <V> V call(Lock lock, Callable<V> callable) throws Exception {
        return call(lock, 0L, callable);
    }


    public static <V> V call(final Lock lock, long time, Callable<V> callable) throws Exception {

        return call(new ResourceLock() {
            @Override
            public void lock() {
                lock.lock();
            }

            @Override
            public boolean tryLock() {
                return lock.tryLock();
            }

            @Override
            public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
                return lock.tryLock(time, unit);
            }

            @Override
            public void unlock() {
                lock.unlock();
            }

        }, time, callable);
    }

    public static <V> V call(ResourceLock lock, Callable<V> callable) throws Exception {
        return call(lock, 0L, callable);
    }


    public static <V> V call(ResourceLock lock, long time, Callable<V> callable) throws Exception {
        boolean locked;
        if (time > 0) {
            locked = lock.tryLock(time, TimeUnit.MILLISECONDS);
        } else {
            lock.lock();
            locked = true;
        }
        if (locked)
            try {
                return callable.call();
            } finally {
                lock.unlock();
            }
        else
            throw new RuntimeException(String.format("try lock failed[%d ms]", time));
    }
}
