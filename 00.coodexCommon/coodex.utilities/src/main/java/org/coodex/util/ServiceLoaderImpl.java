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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.coodex.util.GenericTypeHelper.solve;
import static org.coodex.util.GenericTypeHelper.typeToClass;

/**
 * <S>待coodex utilities放弃1.5时移入org.coodex.util</S>
 * 2016-12-10从concrete中移入
 *
 *
 * 2019-07-16 重构ServiceLoader机制
 * <p>
 * Created by davidoff shen on 2016-11-30.
 */
// TODO 排序
public abstract class ServiceLoaderImpl<T> implements ServiceLoader<T> {

    private final static Logger log = LoggerFactory.getLogger(ServiceLoaderImpl.class);
    private Map<String, T> instances = null;
    private T defaultProvider = null;

    public ServiceLoaderImpl() {
        this(null);
    }

    public ServiceLoaderImpl(T defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    protected void setDefaultProvider(T defaultProvider){
        this.defaultProvider = defaultProvider;
    }

    private void load() {
        if (instances == null) {
            synchronized (this) {
                if (instances == null) {
                    loadInstances();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")

    protected Class<T> getInterfaceClass() {
        return typeToClass(solve(ServiceLoaderImpl.class.getTypeParameters()[0], getClass()));
    }

    @Override
    public T getDefaultProvider() {
        if (defaultProvider == null) {
            throw new RuntimeException("no provider found for: " + getInterfaceClass().getName());
        } else {
            return defaultProvider;
        }
    }

    private void loadInstances() {
        if (instances == null) {
            instances = new HashMap<String, T>();

            java.util.ServiceLoader<ServiceLoaderProvider> serviceLoaderProviders =
                    java.util.ServiceLoader.load(ServiceLoaderProvider.class);

            for (ServiceLoaderProvider provider : serviceLoaderProviders) {
                instances.putAll(provider.load(getInterfaceClass()));
            }
            if (instances.size() == 0) {
                log.debug("no ServiceProvider found for [{}], using default provider.", getInterfaceClass().getCanonicalName());
            }
        }
    }

    @Override
    public Collection<T> getAllInstances() {
        load();
        return instances.values();
    }

//    protected Map<String, T> $getInstances() {
//        load();
//        return instances;
//    }

    @Override
    public Map<String, T> getInstances() {
        load();
        return new HashMap<String, T>(instances);
    }

    @Override
    public T getInstance(Class<? extends T> providerClass) {
        load();
//        return (P) getInstance(providerClass.getCanonicalName());
        Map<String, T> copy = new HashMap<String, T>();
        for (Map.Entry<String, T> entry : instances.entrySet()) {
//            T t = instances.get(key);
            if (entry.getValue() != null && providerClass.isAssignableFrom(entry.getValue().getClass())) {
                copy.put(entry.getKey(), entry.getValue());
            }
        }

        switch (copy.size()) {
            case 0:
                if(defaultProvider != null && providerClass.isAssignableFrom(defaultProvider.getClass())){
                    return defaultProvider;
                } else {
                    return null;
                }
            case 1:
                return copy.values().iterator().next();
        }
//        T t =  getInstance(providerClass.getName());
        return conflict(providerClass, copy);
    }

    protected T conflict(Class<? extends T> providerClass, Map<String, T> map) {
        T t = getInstance(providerClass.getName());
        if (t != null) return t;

        StringBuilder buffer = new StringBuilder(getInterfaceClass().getName());
        buffer.append("[providerClass: ").append(providerClass.getName()).append("]");
        buffer.append(" has ").append(map.size()).append(" services:[");
        for (T service : map.values()) {
            buffer.append("\n\t").append(service.getClass().getName());
        }
        buffer.append("]");
        throw new RuntimeException(buffer.toString());
    }

    @Override
    public T getInstance(String name) {
        load();
        T instance = instances.get(name);
        return instance == null ? getDefaultProvider() : instance;
    }

    protected T conflict() {
        StringBuilder buffer = new StringBuilder(getInterfaceClass().getName());
        buffer.append(" has ").append(instances.size()).append(" services:[");
        for (T service : instances.values()) {
            buffer.append("\n\t").append(service.getClass().getName());
        }
        buffer.append("]");
        throw new RuntimeException(buffer.toString());
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getInstance() {
        load();
        if (instances.size() == 0)
            return getDefaultProvider();
        else if (instances.size() == 1)
            return (T) instances.values().toArray()[0];
        else
            return conflict();
    }
}
