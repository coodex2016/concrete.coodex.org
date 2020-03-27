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
public abstract class ServiceLoaderImpl<T> implements ServiceLoader<T> {

    private final static Logger log = LoggerFactory.getLogger(ServiceLoaderImpl.class);
    private Singleton<Instances> instances = new Singleton<>(
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
                        instances.instancesMap.forEach((k, v) -> joiner.add(k + ": " + v.toString()));
                        log.debug("{} SPI instances loaded for: {} instances: \n\t{}",
                                instances.instancesMap.size(), getServiceType(), joiner.toString());
                    }
                }
                instances.unmodifiedMap = Collections.unmodifiableMap(instances.instancesMap);
                return instances;
            }
    );

    private T defaultProvider;
    private Singleton<Map<String, T>> allInstanceSingleton = new Singleton<>(() -> {
        Map<String, T> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : instances.get().unmodifiedMap.entrySet()) {
            map.put(entry.getKey(), cast(entry.getValue()));
        }
        return map;
    });

    public ServiceLoaderImpl() {
        this(null);
    }

    public ServiceLoaderImpl(T defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    protected Object $getInstance() {
        return this;
    }

    protected Type getServiceType() {
        return solveFromInstance(
                ServiceLoader.class.getTypeParameters()[0],
                $getInstance()
        );
    }

//    @Deprecated
//    protected Class<?> getInterfaceClass() {
//        return typeToClass(getServiceType());
//    }

    @Override
    public T getDefault() {
        if (defaultProvider == null) {
            throw new RuntimeException("no provider found for: " + getServiceType().getTypeName());
        } else {
            return defaultProvider;
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
                if (defaultProvider != null && providerClass.isAssignableFrom(defaultProvider.getClass())) {
                    return defaultProvider;
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
            return getDefault();
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
