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

import java.util.Collection;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-26.
 */
public interface ServiceLoader<T> {

    @Deprecated
    Collection<T> getAllInstances();

    /**
     * @return
     * @see ServiceLoader#getAll()
     */
    @Deprecated
    Map<String, T> getInstances();

    Map<String, T> getAll();

    /**
     * @param providerClass
     * @return
     * @see ServiceLoader#get(Class)
     */
    @Deprecated
    T getInstance(Class<? extends T> providerClass);

    T get(Class<? extends T> serviceClass);

    /**
     * @param name
     * @return
     * @see ServiceLoader#get(String)
     */
    @Deprecated
    T getInstance(String name);

    T get(String name);

    /**
     *
     * @return
     * @see ServiceLoader#get()
     */
    @Deprecated
    T getInstance();


    T get();

    T getDefault();

    /**
     * @return
     * @see ServiceLoader#getDefault()
     */
    @Deprecated
    T getDefaultProvider();
}
