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

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SingletonMap<K, V> {

    private static final Singleton<ScheduledExecutorService> DEFAULT_SCHEDULED_EXECUTOR_SERVICE = new Singleton<ScheduledExecutorService>(new Singleton.Builder<ScheduledExecutorService>() {
        @Override
        public ScheduledExecutorService build() {
            return ExecutorsHelper.newSingleThreadScheduledExecutor("singletonMap-DEFAULT");
        }
    });

    private final static Logger log = LoggerFactory.getLogger(SingletonMap.class);
    private final static AtomicInteger poolNumber = new AtomicInteger(1);
    private final Builder<K, V> builder;
    private final Map<K, V> map = new HashMap<K, V>();
    private final K nullKey;
    private long maxAge = 0;
    private ScheduledExecutorService scheduledExecutorService;

    public SingletonMap(Builder<K, V> builder) {
        this(builder, 0, null);
    }

    /**
     * @param builder builder
     * @param maxAge  map内对象的最大存在时长，单位为毫秒
     */
    public SingletonMap(Builder<K, V> builder, long maxAge) {
        this(builder, maxAge, null);
    }

    /**
     * @param builder                  builder
     * @param maxAge                   map内对象的最大存在时长，单位为毫秒
     * @param scheduledExecutorService scheduledExecutorService
     */
    public SingletonMap(Builder<K, V> builder, long maxAge, ScheduledExecutorService scheduledExecutorService) {
        if (builder == null) throw new NullPointerException("builder MUST NOT be null.");
        this.builder = builder;
        nullKey = getNullKeyOnce();
        this.maxAge = maxAge;
        if (maxAge > 0) {
            this.scheduledExecutorService = scheduledExecutorService == null ?
                    DEFAULT_SCHEDULED_EXECUTOR_SERVICE.get() :
                    scheduledExecutorService;
        }
    }

    protected K getNullKeyOnce() {
        return null;
    }


    public boolean containsKey(Object key) {
        return map.containsKey(key == null ? nullKey : key);
    }

    public V get(final K key) {
        final K finalKey = key == null ? nullKey : key;
        if (!map.containsKey(finalKey)) {
            synchronized (map) {
                if (!map.containsKey(finalKey)) {
                    V o = builder.build(key);
                    map.put(finalKey, o);
                    if (maxAge > 0) {
                        scheduledExecutorService.schedule(new Runnable() {
                            @Override
                            public void run() {
                                log.debug("{} die.", key);
                                remove(finalKey);
                            }
                        }, maxAge, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
        return map.get(finalKey);
    }

    public Set<K> keySet() {
        return new HashSet<K>(map.keySet());
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    public <C extends Collection<V>> C fill(C collection, Collection<K> keys) {
        if (collection == null) throw new NullPointerException("collection is null.");
        if (keys != null && keys.size() > 0) {
            for (K key : new LinkedHashSet<K>(keys)) {
                collection.add(get(key));
            }
        }
        return collection;
    }

    /**
     * @param key
     * @return
     * @see SingletonMap#get(Object)
     */
    @Deprecated
    public V getInstance(final K key) {
        return get(key);
    }

    public V remove(K key) {
        final K finalKey = key == null ? nullKey : key;
        if (map.containsKey(finalKey)) {
            synchronized (map) {
                if (map.containsKey(finalKey))
                    return map.remove(finalKey);
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
