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

import org.coodex.concurrent.Debounce;
import org.coodex.concurrent.ExecutorsHelper;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SingletonMap<K, V> {

    private static final AtomicLong VERSION = new AtomicLong(Long.MIN_VALUE);
    private static final Singleton<ScheduledExecutorService> DEFAULT_SCHEDULED_EXECUTOR_SERVICE
            = Singleton.with(() -> ExecutorsHelper.newSingleThreadScheduledExecutor("singletonMap-DEFAULT"));
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SingletonMap.class);
    private final Map<K, Value<V>> map;
    private final Function<K, V> function;
    private final K nullKey;
    private final long maxAge;
    private final BiConsumer<K, V> deathListener;
    private final boolean activeOnGet;
    private final ScheduledExecutorService scheduledExecutorService;
    private long version = VERSION.get();

    private SingletonMap(Function<K, V> function,
                         K nullKey, boolean activeOnGet,
                         long maxAge,
                         BiConsumer<K, V> deathListener,
                         Supplier<Map<K, Value<V>>> mapSupplier,
                         ScheduledExecutorService scheduledExecutorService) {
        this.function = function;
        this.nullKey = nullKey;
        this.maxAge = Math.max(0, maxAge);
        this.map = mapSupplier == null ? new ConcurrentHashMap<>() : mapSupplier.get();
        this.deathListener = deathListener;
        this.activeOnGet = activeOnGet;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * 重置所有单例
     */
    public static void resetAll() {
        VERSION.incrementAndGet();
    }

    public static <K, V> SingletonMapBuilder<K, V> builder() {
        return new SingletonMapBuilder<>();
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService == null ? DEFAULT_SCHEDULED_EXECUTOR_SERVICE.get() : scheduledExecutorService;
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key == null ? nullKey : key);
    }


    public V get(K key, Supplier<V> supplier) {
        return get(key, supplier, maxAge);
    }

    public V get(K key, Supplier<V> supplier, long maxAge) {
        return get(key, supplier, maxAge, deathListener);
    }

    public V get(K key, Supplier<V> supplier, BiConsumer<K, V> deathListener) {
        return get(key, supplier, maxAge, deathListener);
    }

    public V get(K key, Supplier<V> supplier, long maxAge, BiConsumer<K, V> deathListener) {
        return get(key, (k) -> Objects.requireNonNull(supplier, "supplier is null").get(), maxAge, deathListener);
    }

    public V get(final K key) {
        return get(key, Objects.requireNonNull(function, "function is null"));
    }

    public V get(final K key, long maxAge) {
        return get(key, Objects.requireNonNull(function, "function is null"), maxAge);
    }

    public V get(final K key, BiConsumer<K, V> deathListener) {
        return get(key, Objects.requireNonNull(function, "function is null"), deathListener);
    }

    public V get(final K key, long maxAge, BiConsumer<K, V> deathListener) {
        return get(key, Objects.requireNonNull(function, "function is null"), maxAge, deathListener);
    }

    public V get(final K key, Function<K, V> function) {
        return get(key, function, maxAge);
    }

    public V get(final K key, Function<K, V> function, long maxAge) {
        return get(key, function, maxAge, null);
    }

    public V get(final K key, Function<K, V> function, BiConsumer<K, V> deathListener) {
        return get(key, function, maxAge, deathListener);
    }

    public V get(final K key, Function<K, V> function, long maxAge, BiConsumer<K, V> deathListener) {
        if (function == null) {
            throw new NullPointerException("function is null.");
        }
        if (version != VERSION.get()) {
            synchronized (map) {
                if (version != VERSION.get()) {
                    clear();
                    version = VERSION.get();
                }
            }
        }
        final K finalKey = key == null ? nullKey : key;
        if (!map.containsKey(finalKey)) {
            synchronized (map) {
                if (!map.containsKey(finalKey)) {
                    V o = function.apply(finalKey);
                    Value<V> value = new Value<>();
                    value.value = o;
                    if (maxAge > 0) {
                        value.debounce = Debounce.newBuilder()
                                .idle(maxAge)
                                .scheduledExecutorService(getScheduledExecutorService())
                                .runnable(() -> {
                                    Value<V> v = map.remove(finalKey);
                                    if (v != null) {
                                        log.debug("{} die.", finalKey);
                                        BiConsumer<K, V> listener = deathListener == null ? this.deathListener : deathListener;
                                        if (listener != null) {
                                            try {
                                                listener.accept(finalKey, v.value);
                                            } catch (Throwable th) {
                                                log.warn("listener process failed: {}", listener, th);
                                            }
                                        }
                                    }
                                })
                                .build();

//                                new Debouncer<>(
//                                        k -> {

//                        , maxAge, getScheduledExecutorService());
                        value.debounce.submit();
                    }
                    map.put(finalKey, value);
                }
            }
        }
        Value<V> value = map.get(finalKey);
        if (activeOnGet && value.debounce != null) {
            value.debounce.submit();
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
        if (collection == null) {
            throw new NullPointerException("collection is null.");
        }
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
                    Value<V> value = map.remove(finalKey);
                    if (value != null) {
                        if(value.debounce != null) {
                            value.debounce.cancel();
                        }
                        return value.value;
                    } else {
                        return null;
                    }
//                    return value == null ? null : value.value;
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
            if (map.size() > 0) {
                map.forEach((key, v) -> {
                    if (v != null && v.debounce != null) {
                        v.debounce.cancel();
                    }
                });
                map.clear();
            }
        }
    }

    public void reset() {
        clear();
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
//    }
//        }

    public static class Value<V> {
        private Debounce debounce;
        private V value;
    }

    public static class SingletonMapBuilder<K, V> {
        private Function<K, V> function;
        private K nullKey;
        private boolean activeOnGet;
        private long maxAge;
        private BiConsumer<K, V> deathListener;
        private Supplier<Map<K, Value<V>>> mapSupplier;
        private ScheduledExecutorService scheduledExecutorService;

        SingletonMapBuilder() {
        }

        public SingletonMapBuilder<K, V> function(Function<K, V> function) {
            this.function = function;
            return this;
        }

        public SingletonMapBuilder<K, V> nullKey(K nullKey) {
            this.nullKey = nullKey;
            return this;
        }

        public SingletonMapBuilder<K, V> activeOnGet(boolean activeOnGet) {
            this.activeOnGet = activeOnGet;
            return this;
        }

        public SingletonMapBuilder<K, V> maxAge(long maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public SingletonMapBuilder<K, V> deathListener(BiConsumer<K, V> deathListener) {
            this.deathListener = deathListener;
            return this;
        }

        public SingletonMapBuilder<K, V> mapSupplier(Supplier<Map<K, Value<V>>> mapSupplier) {
            this.mapSupplier = mapSupplier;
            return this;
        }

        public SingletonMapBuilder<K, V> scheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
            this.scheduledExecutorService = scheduledExecutorService;
            return this;
        }

        public SingletonMap<K, V> build() {
            return new SingletonMap<>(function, nullKey, activeOnGet, maxAge, deathListener, mapSupplier, scheduledExecutorService);
        }

    }
}