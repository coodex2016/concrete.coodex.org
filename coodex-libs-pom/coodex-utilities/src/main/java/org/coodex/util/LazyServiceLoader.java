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

import java.util.Map;
import java.util.function.Supplier;

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
        singleton = Singleton.with(() -> new ServiceLoaderImpl<T>(defaultProvider) {
//            @Override
//            protected Class<?> getInterfaceClass() {
//                return LazyServiceLoader.this.getInterfaceClass();
//            }

            @Override
            protected Object $getInstance() {
                return LazyServiceLoader.this;
            }
        });
    }

    public LazyServiceLoader(final Supplier<T> builder) {
        singleton = Singleton.with(() -> new ServiceLoaderImpl<T>() {
            //            @Override
//            protected Class<?> getInterfaceClass() {
//                return LazyServiceLoader.this.getInterfaceClass();
//            }
            @Override
            protected Object $getInstance() {
                return LazyServiceLoader.this;
            }
            @Override
            protected T getDefaultInstance() {
                return builder.get();
            }
        });
    }

//    @Deprecated
//    public T getService() {
//        return singleton.get().get();
//    }


    @Override
    public Map<String, T> getAll() {
        return singleton.get().getAll();
    }


    @Override
    public T get(Class<? extends T> serviceClass) {
        return singleton.get().get(serviceClass);
    }


    @Override
    public T get(String name) {
        return singleton.get().get(name);
    }


    @Override
    public T get() {
        return singleton.get().get();
    }

    @Override
    public T getDefault() {
        return null;
    }


//    @SuppressWarnings("unchecked")
//    protected Class<?> getInterfaceClass() {
//        return typeToClass(solveFromInstance(LazyServiceLoader.class.getTypeParameters()[0], this));
//    }
}
