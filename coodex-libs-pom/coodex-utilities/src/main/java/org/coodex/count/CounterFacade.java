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

import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by davidoff shen on 2017-04-18.
 */
public class CounterFacade {
    private static final ServiceLoader<CountFacade> COUNTER_FACTORY = new ServiceLoaderImpl<CountFacade>() {
    };

    /**
     * 扔一个数进去统计
     *
     * @param value
     * @param <T>
     */
    public static <T extends Countable> void count(T value) {
        COUNTER_FACTORY.get().count(value);
    }

    /**
     * 扔一堆数进去计算
     *
     * @param value
     * @param <T>
     */
    @SafeVarargs
    public static <T extends Countable> void count(T... value) {
//        COUNTER_FACTORY.get().count(value);
        count(Arrays.asList(value));
    }

    /**
     * 扔一堆数进去计算
     *
     * @param value
     * @param <T>
     */
    public static <T extends Countable> void count(Collection<T> value) {
//        COUNTER_FACTORY.get().count(value);
        for (T t : value) {
            COUNTER_FACTORY.get().count(t);
        }
    }


}
