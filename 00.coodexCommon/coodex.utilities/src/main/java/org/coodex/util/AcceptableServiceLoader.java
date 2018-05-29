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

import static org.coodex.util.TypeHelper.solve;
import static org.coodex.util.TypeHelper.typeToClass;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public class AcceptableServiceLoader<Param_Type, T extends AcceptableService<Param_Type>> implements ServiceLoader<T> {

    private final static Logger log = LoggerFactory.getLogger(AcceptableServiceLoader.class);

    private final ServiceLoaderFacade<T> serviceLoaderFacade;

    public AcceptableServiceLoader(ServiceLoaderFacade<T> serviceLoaderFacade) {
        this.serviceLoaderFacade = serviceLoaderFacade;
    }


    @SuppressWarnings("unchecked")
    private boolean accept(T instance, Param_Type param) {
        Class tClass = instance.getClass();
        Class paramClass = param == null ? null : param.getClass();
//        Type t = ;
        if (paramClass == null)
            return instance.accept(null);

        Class required = typeToClass(solve(
                AcceptableService.class.getTypeParameters()[0], tClass));
        //(t instanceof ParameterizedType) ? (Class) ((ParameterizedType) t).getRawType() : (Class) t;

        if (required != null && required.isAssignableFrom(paramClass)) {
            return instance.accept(param);
        } else {
            return false;
        }
    }

    public List<T> getServiceInstances(Param_Type param) {
        List<T> list = new ArrayList<T>();
        for (T instance : getAllInstances()) {
            if (accept(instance, param))
                list.add(instance);
        }
        try {
            T instance = serviceLoaderFacade.getDefaultProvider();
            if (accept(instance, param))
                list.add(instance);
        } catch (Throwable th) {
        }
        return list;
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
    public T getInstance(Class<? extends T> providerClass) {
        return serviceLoaderFacade.getInstance(providerClass);
    }

    @Override
    public T getInstance(String name) {
        return serviceLoaderFacade.getInstance(name);
    }

    @Override
    public T getInstance() {
        return serviceLoaderFacade.getInstance();
    }

    @Override
    public Map<String, T> getInstances() {
        return serviceLoaderFacade.getInstances();
    }
}
