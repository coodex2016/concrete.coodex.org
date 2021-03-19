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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Supplier;

import static org.coodex.util.Common.cast;
import static org.coodex.util.GenericTypeHelper.solveFromInstance;

/**
 * <S>待coodex utilities放弃1.5时移入org.coodex.util</S>
 * 2016-12-10从concrete中移入
 * <p>
 * <p>
 * 2019-07-16 重构ServiceLoader机制
 * <p>
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class LazyServiceLoader<T> implements ServiceLoader<T> {

    private final static Logger log = LoggerFactory.getLogger(LazyServiceLoader.class);
    private final Singleton<Instances> instances = Singleton.with(
            () -> {
                Instances instances = new Instances();
                instances.instancesMap = new HashMap<>();
                java.util.ServiceLoader<ServiceLoaderProvider> serviceLoaderProviders =
                        java.util.ServiceLoader.load(ServiceLoaderProvider.class);

                for (ServiceLoaderProvider provider : serviceLoaderProviders) {
                    instances.instancesMap.putAll(provider.load(getServiceType()));
                }
                if (log.isDebugEnabled()) {
                    if (instances.instancesMap.size() == 0) {
                        log.debug("no ServiceProvider found for [{}], using default provider.", getServiceType().getTypeName());

                    } else {
                        StringJoiner joiner = new StringJoiner("\n\t");
                        instances.instancesMap.forEach((k, v) -> joiner.add(k + "(" + SPI.getServiceOrder(v) + "): " + v.toString()));
                        log.debug("{} SPI instances loaded for: {} instances: \n\t{}",
                                instances.instancesMap.size(), getServiceType(), joiner.toString());
                    }
                }
                instances.unmodifiedMap = Collections.unmodifiableMap(instances.instancesMap);
                return instances;
            }
    );
    private final Singleton<Map<String, T>> allInstanceSingleton = Singleton.with(() -> {
        Map<String, T> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : instances.get().unmodifiedMap.entrySet()) {
            map.put(entry.getKey(), cast(entry.getValue()));
        }
        return map;
    });

    private Supplier<T> defaultProviderSupplier;
    private final Singleton<T> defaultProviderSingleton = Singleton.with(() -> {
        if (defaultProviderSupplier == null) {
            defaultProviderSupplier = this::getDefaultInstance;
        }
        return defaultProviderSupplier.get();
    });

    public LazyServiceLoader() {
        this((T) null);
    }

    public LazyServiceLoader(T defaultProvider) {
        this(defaultProvider == null ? null : () -> defaultProvider);
    }

    public LazyServiceLoader(Supplier<T> defaultProviderSupplier) {
        this.defaultProviderSupplier = defaultProviderSupplier;
    }

    protected Object getGenericTypeSearchContextObject() {
        return this;
    }

    protected Type getServiceType() {
        return solveFromInstance(
                ServiceLoader.class.getTypeParameters()[0],
                getGenericTypeSearchContextObject()
        );
    }

    @Override
    public final T getDefault() {
        return defaultProviderSingleton.get();
    }

    protected T getDefaultInstance() {
        if (getDefault() == null) {
            throw new RuntimeException("no provider found for: " + getServiceType().getTypeName());
        } else {
            return getDefault();
        }
    }

    @Override
    public Map<String, T> getAll() {
        return allInstanceSingleton.get();
    }


    @Override
    public T get(Class<? extends T> providerClass) {
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : instances.get().instancesMap.entrySet()) {
            if (entry.getValue() != null && providerClass.isAssignableFrom(entry.getValue().getClass())) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }

        switch (copy.size()) {
            case 0:
                T defaultProviderValue = getDefault();
                if (defaultProviderValue != null && providerClass.isAssignableFrom(defaultProviderValue.getClass())) {
                    return defaultProviderValue;
                } else {
                    return null;
                }
            case 1:
                return cast(copy.values().iterator().next());
        }
        return conflict(providerClass, copy);
    }

    protected T conflict(Class<? extends T> providerClass, Map<String, Object> map) {
        T t = get(providerClass.getName());
        if (t != null) return t;

        StringBuilder buffer = new StringBuilder(getServiceType().getTypeName());
        buffer.append("[providerClass: ").append(providerClass.getName()).append("]");
        buffer.append(" has ").append(map.size()).append(" services:[");
        for (Object service : map.values()) {
            buffer.append("\n\t").append(service.getClass().getName());
        }
        buffer.append("]");
        throw new RuntimeException(buffer.toString());
    }

    @Override
    public T get(String name) {
        T instance = cast(instances.get().instancesMap.get(name));
        return instance == null ? getDefault() : instance;
    }

    protected T conflict() {
        StringBuilder buffer = new StringBuilder(getServiceType().getTypeName());
        buffer.append(" has ").append(instances.get().instancesMap.size()).append(" services:[");
        for (Object service : instances.get().instancesMap.values()) {
            buffer.append("\n\t").append(service.getClass().getName());
        }
        buffer.append("]");
        throw new RuntimeException(buffer.toString());
    }

    @Override
    public T get() {
        if (instances.get().instancesMap.size() == 0)
            return defaultProviderSingleton.get();
        else if (instances.get().instancesMap.size() == 1)
            return cast(instances.get().instancesMap.values().toArray()[0]);
        else
            return conflict();
    }


    private static class Instances {
        Map<String, Object> instancesMap = null;
        Map<String, Object> unmodifiedMap = null;
    }
}
