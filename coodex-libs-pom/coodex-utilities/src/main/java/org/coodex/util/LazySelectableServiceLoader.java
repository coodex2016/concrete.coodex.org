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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        singleton = Singleton.with(
                () -> new SelectableServiceLoaderImpl<Param_Type, T>(defaultProvider) {
//                    @Override
//                    protected Object $getInstance() {
//                        return LazySelectableServiceLoader.this;
//                    }

                    //                    @Override
//                    protected Class<?> getParamType() {
//                        return LazySelectableServiceLoader.this.getParamType();
//                    }
//
//                    @Override
//                    protected Class<?> getInterfaceClass() {
//                        return LazySelectableServiceLoader.this.getInterfaceClass();
//                    }
                    @Override
                    protected Object $getInstance() {
                        return LazySelectableServiceLoader.this;
                    }
                });
    }

    public LazySelectableServiceLoader(final Function<Method, RuntimeException> exceptionFunction) {
        singleton = Singleton.with(
                () -> new SelectableServiceLoaderImpl<Param_Type, T>(exceptionFunction) {
                    @Override
                    protected Object $getInstance() {
                        return LazySelectableServiceLoader.this;
                    }
                });
    }

    @Override
    public T select(Param_Type param) {
        return singleton.get().select(param);
    }


    @Override
    public List<T> selectAll(Param_Type param_type) {
        return singleton.get().selectAll(param_type);
    }


    @Override
    public Map<String, T> getAll() {
        return singleton.get().getAll();
    }


//    @Override
//    public T getDefault() {
//        return singleton.get().getDefault();
//    }


//    protected Type getParameterType() {
//        return solveFromInstance(
//                SelectableServiceLoaderImpl.class.getTypeParameters()[0],
//                $getInstance()
//        );
//    }
//
//
//    protected Object $getInstance() {
//        return this;
//    }
//
//    protected Type getServiceType() {
//        return solveFromInstance(
//                SelectableServiceLoaderImpl.class.getTypeParameters()[1],
//                $getInstance()
//        );
//    }

//    @SuppressWarnings("unchecked")
//    protected Class<?> getParamType() {
//        return typeToClass(solveFromInstance(LazySelectableServiceLoader.class.getTypeParameters()[0], this));
//    }
//
//    @SuppressWarnings("unchecked")
//    protected Class<?> getInterfaceClass() {
//        return typeToClass(solveFromInstance(LazySelectableServiceLoader.class.getTypeParameters()[1], this));
//    }
}
