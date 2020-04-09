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

import lombok.Builder;
import org.coodex.concurrent.Debouncer;
import org.coodex.concurrent.ExecutorsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Builder
public class SingletonMap<K, V> {

    private static final Singleton<ScheduledExecutorService> DEFAULT_SCHEDULED_EXECUTOR_SERVICE
            = Singleton.with(() -> ExecutorsHelper.newSingleThreadScheduledExecutor("singletonMap-DEFAULT"));

    private final static Logger log = LoggerFactory.getLogger(org.coodex.util.SingletonMap.class);
    private final Map<K, Value<K, V>> map = new HashMap<>();

    private Function<K, V> function;
    private K nullKey;
    @Builder.Default
    private boolean activeOnGet = false;
    @Builder.Default
    private long maxAge = 0;
    private ScheduledExecutorService scheduledExecutorService;

//    @Deprecated
//    public SingletonMap(Function<K, V> builder) {
//        this(builder, 0, null);
//    }
//
//    /**
//     * @param builder builder
//     * @param maxAge  map内对象的最大存在时长，单位为毫秒
//     */
//    @Deprecated
//    public SingletonMap(Function<K, V> builder, long maxAge) {
//        this(builder, maxAge, null);
//    }
//
//    /**
//     * @param builder                  builder
//     * @param maxAge                   map内对象的最大存在时长，单位为毫秒
//     * @param scheduledExecutorService scheduledExecutorService
//     */
//    @Deprecated
//    public SingletonMap(Function<K, V> builder, long maxAge, ScheduledExecutorService scheduledExecutorService) {
//        this(builder, null, false, maxAge, scheduledExecutorService);
//    }

    @SuppressWarnings("unused")
    private SingletonMap(Function<K, V> function,
                         K nullKey, boolean activeOnGet,
                         long maxAge, ScheduledExecutorService scheduledExecutorService) {
        this.function = function;
        this.nullKey = nullKey;
        this.maxAge = Math.max(0, maxAge);
        if (this.maxAge > 0) {
            this.activeOnGet = activeOnGet;
            this.scheduledExecutorService = scheduledExecutorService == null ? DEFAULT_SCHEDULED_EXECUTOR_SERVICE.get() : scheduledExecutorService;
        }
    }


    public boolean containsKey(Object key) {
        return map.containsKey(key == null ? nullKey : key);
    }

    public V get(K key, Supplier<V> supplier) {
        if (supplier == null) throw new NullPointerException("supplier is null");
        return get(key, (k) -> supplier.get());
    }

    public V get(final K key) {
        return get(key, function);
    }

    public V get(final K key, Function<K, V> function) {
        if (function == null) throw new NullPointerException("function is null.");
        final K finalKey = key == null ? nullKey : key;
        if (!map.containsKey(finalKey)) {
            synchronized (map) {
                if (!map.containsKey(finalKey)) {
                    V o = function.apply(finalKey);
                    Value<K, V> value = new Value<>();
                    value.value = o;
                    if (maxAge > 0) {
                        value.debouncer = new Debouncer<>(k -> {
                            log.debug("{} die.", k);
                            map.remove(k);
                        }, maxAge, scheduledExecutorService);
                    }
                    map.put(finalKey, value);
                }
            }
        }
        Value<K, V> value = map.get(finalKey);
        if (activeOnGet) {
            value.debouncer.call(finalKey);
            log.debug("{} active.", finalKey);
        }
        return value.value;
    }

    public Set<K> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet().stream().map(entry -> new Map.Entry<K, V>() {

            @Override
            public K getKey() {
                return entry.getKey();
            }

            @Override
            public V getValue() {
                return entry.getValue().value;
            }

            @Override
            public V setValue(V value) {
                return null;
            }
        }).collect(Collectors.toSet());
    }

    public <C extends Collection<V>> C fill(C collection, Collection<K> keys) {
        if (collection == null) throw new NullPointerException("collection is null.");
        if (keys != null && keys.size() > 0) {
            for (K key : new LinkedHashSet<>(keys)) {
                collection.add(get(key));
            }
        }
        return collection;
    }

    public V remove(K key) {
        final K finalKey = key == null ? nullKey : key;
        if (map.containsKey(finalKey)) {
            synchronized (map) {
                if (map.containsKey(finalKey)) {
                    Value<K, V> value = map.remove(finalKey);
                    return value == null ? null : value.value;
                }
            }
        }
        return null;
    }

    public Collection<V> values() {
        return map.values().stream().map(value -> value.value).collect(Collectors.toList());
    }

    public void clear() {
        synchronized (map) {
            if (map.size() > 0)
                map.clear();
        }
    }

    static class Value<K, V> {
        private Debouncer<K> debouncer;
        private V value;
    }

//    @lombok.Builder(access = AccessLevel.PUBLIC)
//    @Getter
//    @NoArgsConstructor(access = AccessLevel.PRIVATE)
//    @AllArgsConstructor(access = AccessLevel.PRIVATE)
//    public static class Builder<K, V> {
//        private Function<K, V> function;
//        private K nullKey;
//        private boolean activeOnGet = false;
//        private long maxAge;
//        private ScheduledExecutorService scheduledExecutorService;
//
//        Builder<K, V> _clone() {
//            Builder<K, V> builder = new Builder<>();
//            builder.function = function;
//            builder.nullKey = nullKey;
//            builder.maxAge = Math.max(0, maxAge);
//            if (builder.maxAge > 0) {
//                builder.scheduledExecutorService = scheduledExecutorService == null ? DEFAULT_SCHEDULED_EXECUTOR_SERVICE.get() : scheduledExecutorService;
//                builder.activeOnGet = activeOnGet;
//            }
//            return builder;
//        }
//
//        public SingletonMap<K, V> buildMap() {
//            return new SingletonMap<>(this);
//        }
//    }

}