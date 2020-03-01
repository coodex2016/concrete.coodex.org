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

import java.util.Collection;
import java.util.Map;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;
import static org.coodex.util.GenericTypeHelper.typeToClass;

public abstract class LazyServiceLoader<T> /*extends Singleton<ServiceLoader<T>>*/ implements ServiceLoader<T> {
    private final Singleton<ServiceLoader<T>> singleton;

    public LazyServiceLoader() {
        this((T) null);
//        singleton = new Singleton<ServiceLoader<T>>(new Singleton.Builder<ServiceLoader<T>>() {
//            @Override
//            public ServiceLoader<T> build() {
//                return new ServiceLoaderImpl<T>() {
//                    @Override
//                    protected Class<T> getInterfaceClass() {
//                        return LazyServiceLoader.this.getInterfaceClass();
//                    }
//                };
//            }
//        });
    }

    public LazyServiceLoader(final T defaultProvider) {
        singleton = new Singleton<ServiceLoader<T>>(new Singleton.Builder<ServiceLoader<T>>() {
            @Override
            public ServiceLoader<T> build() {
                return new ServiceLoaderImpl<T>(defaultProvider) {
                    @Override
                    protected Class<T> getInterfaceClass() {
                        return LazyServiceLoader.this.getInterfaceClass();
                    }
                };
            }
        });
    }

    public LazyServiceLoader(final Singleton.Builder<T> builder) {
        singleton = new Singleton<ServiceLoader<T>>(new Singleton.Builder<ServiceLoader<T>>() {
            @Override
            public ServiceLoader<T> build() {
                return new ServiceLoaderImpl<T>() {
                    @Override
                    protected Class<T> getInterfaceClass() {
                        return LazyServiceLoader.this.getInterfaceClass();
                    }

                    @Override
                    public T getDefault() {
                        return builder.build();
                    }
                };
            }
        });
    }

//    @Deprecated
//    public T getService() {
//        return singleton.get().get();
//    }

    @Override
    @Deprecated
    public Collection<T> getAllInstances() {
        return singleton.get().getAllInstances();
    }

    @Override
    public Map<String, T> getInstances() {
        return getAll();
    }

    @Override
    public Map<String, T> getAll() {
        return singleton.get().getAll();
    }

    @Override
    public T getInstance(Class<? extends T> providerClass) {
        return get(providerClass);
    }

    @Override
    public T get(Class<? extends T> serviceClass) {
        return singleton.get().get(serviceClass);
    }

    @Override
    public T getInstance(String name) {
        return get(name);
    }

    @Override
    public T get(String name) {
        return singleton.get().get(name);
    }

    @Override
    public T getInstance() {
        return get();
    }

    @Override
    public T get() {
        return singleton.get().get();
    }

    @Override
    public T getDefault() {
        return null;
    }

    @Override
    public T getDefaultProvider() {
        return null;
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getInterfaceClass() {
        return typeToClass(solveFromInstance(LazyServiceLoader.class.getTypeParameters()[0], this));
    }
}
