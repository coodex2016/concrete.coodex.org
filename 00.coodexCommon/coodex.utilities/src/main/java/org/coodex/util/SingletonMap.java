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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonMap<K, V> {

    public interface Builder<K, V> {
        V build(K key);
    }

    private final Builder<K, V> builder;

    public SingletonMap(Builder<K, V> builder) {
        if (builder == null) throw new NullPointerException("builder MUST NOT be null.");
        this.builder = builder;
    }

    private Map<K, V> map = new ConcurrentHashMap<K, V>();

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public V getInstance(K key) {
        if (!map.containsKey(key)) {
            synchronized (map) {
                if (!map.containsKey(key)) {
                    map.put(key, builder.build(key));
                }
            }
        }
        return map.get(key);
    }

    public V remove(K key) {
        if (map.containsKey(key)) {
            synchronized (map) {
                if (map.containsKey(key))
                    return map.remove(key);
            }
        }
        return null;
    }


}
