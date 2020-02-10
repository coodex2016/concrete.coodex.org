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

package org.coodex.util;

public class Singleton<T> {

    private final Builder<T> builder;
    private volatile T instance = null;
    private volatile boolean loaded = false;

    public Singleton(Builder<T> builder) {
        if (builder == null) throw new NullPointerException("builder MUST NOT be null.");
        this.builder = builder;
    }

    /**
     *
     * @return
     * @see Singleton#get()
     */
    @Deprecated
    public T getInstance() {
        return get();
    }

    public T get() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    instance = builder.build();
                    loaded = true;
                }
            }
        }
        return instance;
    }

    public interface Builder<T> {
        T build();
    }
}
