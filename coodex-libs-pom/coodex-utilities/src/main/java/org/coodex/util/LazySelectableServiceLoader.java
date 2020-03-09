/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;
import static org.coodex.util.GenericTypeHelper.typeToClass;

public abstract class LazySelectableServiceLoader<Param_Type, T extends SelectableService<Param_Type>>
//        extends Singleton<SelectableServiceLoader<Param_Type, T>>
        implements SelectableServiceLoader<Param_Type, T> {

    private final Singleton<SelectableServiceLoader<Param_Type, T>> singleton;

    public LazySelectableServiceLoader() {
        this((T) null);
    }

    public LazySelectableServiceLoader(final T defaultProvider) {
        singleton = new Singleton<SelectableServiceLoader<Param_Type, T>>(new Singleton.Builder<SelectableServiceLoader<Param_Type, T>>() {
            @Override
            public SelectableServiceLoaderImpl<Param_Type, T> build() {
                return new SelectableServiceLoaderImpl<Param_Type, T>(defaultProvider) {

                    @Override
                    protected Class<Param_Type> getParamType() {
                        return LazySelectableServiceLoader.this.getParamType();
                    }

                    @Override
                    protected Class<T> getInterfaceClass() {
                        return LazySelectableServiceLoader.this.getInterfaceClass();
                    }
                };
            }
        });
    }

    public LazySelectableServiceLoader(final Common.Function<Method, RuntimeException> exceptionFunction) {
        singleton = new Singleton<SelectableServiceLoader<Param_Type, T>>(new Singleton.Builder<SelectableServiceLoader<Param_Type, T>>() {
            @Override
            public SelectableServiceLoaderImpl<Param_Type, T> build() {
                return new SelectableServiceLoaderImpl<Param_Type, T>(exceptionFunction) {
                    @Override
                    protected Class<Param_Type> getParamType() {
                        return LazySelectableServiceLoader.this.getParamType();
                    }

                    @Override
                    protected Class<T> getInterfaceClass() {
                        return LazySelectableServiceLoader.this.getInterfaceClass();
                    }
                };

            }
        });
    }

    @Override
    public T select(Param_Type param) {
        return singleton.get().select(param);
    }

//    @Override
//    public List<T> getServiceInstances(Param_Type param) {
//        return selectAll(param);
//    }

    @Override
    public List<T> selectAll(Param_Type param_type) {
        return singleton.get().selectAll(param_type);
    }

//    @Override
//    public T getServiceInstance(Param_Type param) {
//        return select(param);
//    }

//    @Override
//    @Deprecated
//    public Collection<T> getAllInstances() {
//        return get().getAllInstances();
//    }

//    @Override
//    public Map<String, T> getInstances() {
//        return getAll();
//    }

    @Override
    public Map<String, T> getAll() {
        return singleton.get().getAll();
    }

//    @Override
//    public T getInstance(Class<? extends T> providerClass) {
//        return get(providerClass);
//    }

//    @Override
//    public T get(Class<? extends T> serviceClass) {
//        return get().get(serviceClass);
//    }

//    @Override
//    public T getInstance(String name) {
//        return get(name);
//    }

    //    @Override
//    public T get(String name) {
//        return get().get(name);
//    }
//
    @Override
    public T getDefault() {
        return singleton.get().getDefault();
    }
//
//    @Override
//    public T getDefaultProvider() {
//        return getDefault();
//    }


    @SuppressWarnings("unchecked")
    protected Class<Param_Type> getParamType() {
        return typeToClass(solveFromInstance(LazySelectableServiceLoader.class.getTypeParameters()[0], this));
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getInterfaceClass() {
        return typeToClass(solveFromInstance(LazySelectableServiceLoader.class.getTypeParameters()[1], this));
    }
}
