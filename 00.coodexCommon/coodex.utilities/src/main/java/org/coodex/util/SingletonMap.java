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

import org.coodex.concurrent.ExecutorsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SingletonMap<K, V> {

    private final static Logger log = LoggerFactory.getLogger(SingletonMap.class);

    private final Builder<K, V> builder;
    private long maxAge = 0;
    private ScheduledExecutorService scheduledExecutorService;
    private Map<K, V> map = new ConcurrentHashMap<K, V>();

    public SingletonMap(Builder<K, V> builder) {
        if (builder == null) throw new NullPointerException("builder MUST NOT be null.");
        this.builder = builder;
    }

    /**
     * @param builder
     * @param maxAge  map内对象的最大存在时长，单位为毫秒
     */
    public SingletonMap(Builder<K, V> builder, long maxAge) {
        this(builder);
        this.maxAge = maxAge;
        if (maxAge > 0) {
            scheduledExecutorService = ExecutorsHelper.newSingleThreadScheduledExecutor();
        }
    }


    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public V getInstance(final K key) {
        if (!map.containsKey(key)) {
            synchronized (map) {
                if (!map.containsKey(key)) {
                    V o = builder.build(key);
                    map.put(key, o);
                    if (maxAge > 0) {
                        scheduledExecutorService.schedule(new Runnable() {
                            @Override
                            public void run() {
                                log.debug("{} die.", key);
                                remove(key);
                            }
                        }, maxAge, TimeUnit.MILLISECONDS);
                    }
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

    public Collection<V> values() {
        synchronized (map) {
            return map.values();
        }
    }

    public void clear() {
        synchronized (map) {
            if (map.size() > 0)
                map.clear();
        }
    }


    public interface Builder<K, V> {
        V build(K key);
    }


}
