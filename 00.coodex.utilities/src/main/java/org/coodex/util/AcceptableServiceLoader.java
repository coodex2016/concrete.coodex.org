/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public class AcceptableServiceLoader<Param_Type, T extends AcceptableService<Param_Type>> implements ServiceLoader<T> {

    private final static Logger log = LoggerFactory.getLogger(AcceptableServiceLoader.class);

    private final ServiceLoaderFacade<T> serviceLoaderFacade;

    public AcceptableServiceLoader(ServiceLoaderFacade<T> serviceLoaderFacade) {
        this.serviceLoaderFacade = serviceLoaderFacade;
    }

    private boolean accept(T instance, Param_Type param) {
        Class tClass = instance.getClass();
        Class paramClass = param.getClass();
        Type t = TypeHelper.findActualClassFrom(
                AcceptableService.class.getTypeParameters()[0],
                tClass);
        Class required = (t instanceof ParameterizedType) ? (Class) ((ParameterizedType) t).getRawType() : (Class) t;

        if (required.isAssignableFrom(paramClass)) {
            return instance.accept(param);
        } else {
            return false;
        }
    }

    public T getServiceInstance(Param_Type param) {
        for (T instance : getAllInstances()) {
            if (accept(instance, param))
                return instance;
        }
        try {
            T instance = serviceLoaderFacade.getDefaultProvider();
            if (accept(instance, param))
                return instance;
        } catch (Throwable th) {
        }
        log.warn("no service instance accept this: {}", param);

        return null;
    }

    @Override
    public Collection<T> getAllInstances() {
        return serviceLoaderFacade.getAllInstances();
    }

    @Override
    public <P extends T> P getInstance(Class<P> providerClass) {
        return serviceLoaderFacade.getInstance(providerClass);
    }

    @Override
    public T getInstance(String className) {
        return serviceLoaderFacade.getInstance(className);
    }

    @Override
    public T getInstance() {
        return serviceLoaderFacade.getInstance();
    }
}
