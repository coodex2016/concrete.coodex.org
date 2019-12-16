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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;
import static org.coodex.util.GenericTypeHelper.typeToClass;


/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class AcceptableServiceLoader<Param_Type, T extends AcceptableService<Param_Type>>
        implements ServiceLoader<T> {

    private final static Logger log = LoggerFactory.getLogger(AcceptableServiceLoader.class);

    private ServiceLoader<T> serviceLoaderFacade;
    private T defaultService = null;

    public AcceptableServiceLoader() {
        this((T) null);
    }

    public AcceptableServiceLoader(final T defaultService) {
        this.defaultService = defaultService;
    }

    @Deprecated
    public AcceptableServiceLoader(ServiceLoader<T> serviceLoaderFacade) {
        this.serviceLoaderFacade = serviceLoaderFacade;
    }

    private ServiceLoader<T> getServiceLoaderFacade() {
        if (serviceLoaderFacade == null) {
            synchronized (this) {
                if (serviceLoaderFacade == null) {
                    this.serviceLoaderFacade = new ServiceLoaderImpl<T>() {
                        @Override
                        public T getDefault() {
                            return defaultService == null ? super.getDefault() : defaultService;
                        }
                    };
                }
            }
        }
        return serviceLoaderFacade;
    }


    @SuppressWarnings("unchecked")
    private boolean accept(T instance, Param_Type param) {
//        Class tClass = instance.getClass();
        Class paramClass = param == null ? null : param.getClass();
//        Type t = ;
        if (paramClass == null)
            return instance.accept(null);

        // todo 需要考虑泛型数组的问题
        Class required = typeToClass(solveFromInstance(
                AcceptableService.class.getTypeParameters()[0], instance));
        //(t instanceof ParameterizedType) ? (Class) ((ParameterizedType) t).getRawType() : (Class) t;

        if (required != null && required.isAssignableFrom(paramClass)) {
            return instance.accept(param);
        } else {
            return false;
        }
    }

    /**
     * @param param param
     * @return {@link AcceptableServiceLoader#selectAll(Object)}
     */
    @Deprecated
    public List<T> getServiceInstances(Param_Type param) {
        return selectAll(param);
    }

    public List<T> selectAll(Param_Type param) {
        List<T> list = new ArrayList<T>();
        for (T instance : getAll().values()) {
            if (accept(instance, param))
                list.add(instance);
        }
        try {
            T instance = getServiceLoaderFacade().getDefault();
            if (accept(instance, param))
                list.add(instance);
        } catch (Throwable ignored) {
        }
        return list;
    }

    /**
     * @param param
     * @return {@link AcceptableServiceLoader#select(Object)}
     */
    @Deprecated
    public T getServiceInstance(Param_Type param) {
        return select(param);
    }

    public T select(Param_Type param) {
        for (T instance : getAll().values()) {
            if (accept(instance, param))
                return instance;
        }
        try {
            T instance = getServiceLoaderFacade().getDefault();
            if (accept(instance, param))
                return instance;
            if(instance.accept(param))
                return instance;
        } catch (Throwable ignored) {
        }
        log.info("no service instance accept this: {}", param);

        return null;
    }

    @Override
    @Deprecated
    public Collection<T> getAllInstances() {
        return getServiceLoaderFacade().getAll().values();
    }

    @Override
    public T get(Class<? extends T> providerClass) {
        return getServiceLoaderFacade().get(providerClass);
    }

    @Override
    public T get(String name) {
        return getServiceLoaderFacade().get(name);
    }

    @Override
    public T get() {
        return getServiceLoaderFacade().get();
    }

    @Override
    public T getDefault() {
        return getServiceLoaderFacade().getDefault();
    }

    @Override
    public Map<String, T> getAll() {
        return getServiceLoaderFacade().getAll();
    }

    @Override
    @Deprecated
    public Map<String, T> getInstances() {
        return getAll();
    }

    @Override
    @Deprecated
    public T getInstance(Class<? extends T> providerClass) {
        return get(providerClass);
    }

    @Override
    @Deprecated
    public T getInstance(String name) {
        return get(name);
    }

    @Override
    @Deprecated
    public T getInstance() {
        return get();
    }

    @Override
    @Deprecated
    public final T getDefaultProvider() {
        return getDefault();
    }
}
