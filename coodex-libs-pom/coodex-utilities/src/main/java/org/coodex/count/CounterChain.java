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

package org.coodex.count;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by davidoff shen on 2017-04-18.
 */
public abstract class CounterChain<T extends Countable> implements Counter<T> {

    private final static Logger log = LoggerFactory.getLogger(CounterChain.class);


    private final List<Counter<T>> counters = new ArrayList<Counter<T>>();

    public void addCounter(Counter<T> counter) {
        if (counter != null && !counters.contains(counter) && !CounterChain.class.isAssignableFrom(counter.getClass())) {
            counters.add(counter);
        }
    }

    protected abstract Executor getThreadPool();

    private boolean isSync(Counter<T> counter) {

        return counter.getClass().getAnnotation(Sync.class) != null;
    }

    @Override
    public void count(final T value) {
        if (value != null && counters.size() > 0) {
            for (final Counter<T> counter : counters) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (counter) {
                            try {
                                counter.count(value);
                            } catch (Throwable th) {
                                log.warn("count failed. {}, {}", counter.getClass().getName(), th.getLocalizedMessage(), th);
                            }
                        }
                    }
                };
                if (isSync(counter))
                    runnable.run();
                else {
                    getThreadPool().execute(runnable);
                }
            }
        }
    }
}
