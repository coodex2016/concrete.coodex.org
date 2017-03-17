/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * <S>待coodex utilities放弃1.5时移入org.coodex.util</S>
 * 2016-12-10从concrete中移入
 * <p>
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class SPIFacade<T> {

    public SPIFacade() {
        loadInstances();
    }

    private final static Logger log = LoggerFactory.getLogger(SPIFacade.class);

    protected Map<String, T> instances = null;

    @SuppressWarnings("unchecked")
    protected Class<T> getInterfaceClass() {
        return (Class<T>) TypeHelper.findActualClassFrom(SPIFacade.class.getTypeParameters()[0], getClass());
    }

    protected T getDefaultProvider() {
        throw new RuntimeException("no provider found for: " + getInterfaceClass().getName());
    }

    protected void loadInstances() {
        if (instances == null) {
            synchronized (this) {
                if (instances == null) {
                    instances = new HashMap<String, T>();
                    ServiceLoader<T> loader = ServiceLoader.load(getInterfaceClass());
                    for (T service : loader) {
                        if (service != null) {
                            instances.put(service.getClass().getCanonicalName(), service);
                        }
                    }
                    if (instances.size() == 0) {
                        log.debug("no ServiceProvider found for [{}]", getInterfaceClass().getCanonicalName());
                    }
                }
            }
        }
    }

    public Collection<T> getAllInstances() {
        return instances.values();
    }

    public T getInstance(Class<? extends T> providerClass) {
        return getInstance(providerClass.getCanonicalName());
    }

    public T getInstance(String className) {
        T instance = instances.get(className);
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

    @SuppressWarnings("unchecked")
    public T getInstance() {
        if (instances.size() == 0)
            return getDefaultProvider();
        else if (instances.size() == 1)
            return (T) instances.values().toArray()[0];
        else
            return conflict();
    }
}
