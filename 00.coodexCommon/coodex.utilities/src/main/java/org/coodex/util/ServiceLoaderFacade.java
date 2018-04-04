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

import static org.coodex.util.TypeHelper.solve;
import static org.coodex.util.TypeHelper.typeToClass;

/**
 * <S>待coodex utilities放弃1.5时移入org.coodex.util</S>
 * 2016-12-10从concrete中移入
 * <p>
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class ServiceLoaderFacade<T> implements ServiceLoader<T> {

    public ServiceLoaderFacade() {
//        loadInstances();
    }

    private void load(){
        synchronized (this){
            if(instances == null){
                loadInstances();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ServiceLoaderFacade.class);

    private Map<String, T> instances = null;

    @SuppressWarnings("unchecked")

    protected Class<T> getInterfaceClass() {
        load();
        return typeToClass(solve(ServiceLoaderFacade.class.getTypeParameters()[0],getClass()));
//        Type t = TypeHelper.findActualClassFrom(
//                ServiceLoaderFacade.class.getTypeParameters()[0],
//                getClass());
////        return (Class<T>) TypeHelper.findActualClassFrom(ServiceLoaderFacade.class.getTypeParameters()[0], getClass());
//        if (t instanceof ParameterizedType)
//            return (Class<T>) ((ParameterizedType) t).getRawType();
//        else
//            return (Class<T>) t;
    }

    public T getDefaultProvider() {
        load();
        throw new RuntimeException("no provider found for: " + getInterfaceClass().getName());
    }

    protected void loadInstances() {
        if (instances == null) {
            synchronized (this) {
                if (instances == null) {
                    instances = new HashMap<String, T>();
                    java.util.ServiceLoader<T> loader = java.util.ServiceLoader.<T>load(getInterfaceClass());
                    for (T service : loader) {
                        if (service != null) {
                            instances.put(service.getClass().getCanonicalName(), service);
                        }
                    }
                    if (instances.size() == 0) {
                        log.debug("no ServiceProvider found for [{}], using default provider.", getInterfaceClass().getCanonicalName());
                    }
                }
            }
        }
    }

    @Override
    public Collection<T> getAllInstances() {
        load();
        return instances.values();
    }

    protected Map<String, T> $getInstances(){
        load();
        return instances;
    }

    @Override
    public Map<String, T> getInstances() {
        load();
        return new HashMap<String, T>($getInstances());
    }

    @Override
    public T getInstance(Class<? extends T> providerClass) {
        load();
//        return (P) getInstance(providerClass.getCanonicalName());
        Map<String, T> copy = new HashMap<String, T>();
        for (String key : instances.keySet()) {
            T t = instances.get(key);
            if (t != null && providerClass.isAssignableFrom(t.getClass())) {
                copy.put(key, t);
            }
        }
        switch (copy.size()) {
            case 0:
                throw null;
            case 1:
                return copy.values().iterator().next();
        }
//        T t =  getInstance(providerClass.getName());
        return conflict(providerClass, copy);
    }

    protected T conflict(Class<? extends T> providerClass, Map<String, T> map) {
        T t = getInstance(providerClass.getName());
        if(t != null) return t;

        StringBuffer buffer = new StringBuffer(getInterfaceClass().getName());
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
        StringBuffer buffer = new StringBuffer(getInterfaceClass().getName());
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
