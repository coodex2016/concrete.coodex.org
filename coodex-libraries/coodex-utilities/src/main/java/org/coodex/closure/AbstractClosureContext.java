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

package org.coodex.closure;


import java.util.function.Supplier;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public abstract class AbstractClosureContext<T> {

    // 本地线程变量，用于存储上下文变量信息
    private final ThreadLocal<T> threadLocal = new ThreadLocal<>();

    protected final T getVariant() {
        return threadLocal.get();
    }

    protected final Object get(T variant, Supplier<?> supplier) {
        if (supplier == null) return null;
        threadLocal.set(variant);
        try {
            return supplier.get();
        } finally {
            threadLocal.remove();
        }
    }

}
