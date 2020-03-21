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

package org.coodex.config;

import java.util.function.Supplier;

public interface Configuration {


    /**
     * <pre>
     * 在多级命名空间中获取指定key的值，下级命名空间的值覆盖上级，例如
     * config.get("key","a","b","c")
     * 则
     * a/b/c/key 高于
     * a/b/key 高于
     * a/key 高于
     * key
     * </pre>
     *
     * @param key
     * @return
     */
    String get(String key, String... namespaces);

    <T> T getValue(String key, T defaultValue, String... namespace);

    <T> T getValue(String key, Supplier<T> defaultValueSupplier, String... namespace);

}
