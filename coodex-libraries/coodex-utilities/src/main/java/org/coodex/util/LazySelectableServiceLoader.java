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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;
import static org.coodex.util.GenericTypeHelper.typeToClass;


/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class LazySelectableServiceLoader<Param_Type, T extends SelectableService<Param_Type>>
        implements SelectableServiceLoader<Param_Type, T>, ServiceLoader<T> {

    public static final Function<Method, RuntimeException> EXCEPTION_FUNCTION =
            method -> new RuntimeException("no instance found."
                    + method.getDeclaringClass().getName()
                    + "." + method.getName());
    private static final Logger log = LoggerFactory.getLogger(LazySelectableServiceLoader.class);


    private ServiceLoader<T> serviceLoaderFacade;
    private Supplier<T> defaultServiceSupplier;

    private final Singleton<T> defaultServiceSingleton = Singleton.with(() ->
            defaultServiceSupplier == null ? null : defaultServiceSupplier.get()
    );

    private Function<Method, RuntimeException> exceptionFunction = null;
    private final Singleton<List<T>> sortedServices = Singleton.with(this::sorted);

    public LazySelectableServiceLoader() {
        this((T) null);
    }

    public LazySelectableServiceLoader(final T defaultService) {
        this(defaultService == null ? null : () -> defaultService);
    }

    public LazySelectableServiceLoader(Supplier<T> defaultServiceSupplier) {
        this.defaultServiceSupplier = defaultServiceSupplier;
    }

    /**
     * 根据给出的function提供一个默认loader
     *
     * @param exceptionFunction exceptionFunction
     */
    public LazySelectableServiceLoader(Function<Method, RuntimeException> exceptionFunction) {
        this.exceptionFunction = exceptionFunction;
    }

    protected Type getParameterType() {
        return solveFromInstance(
                SelectableServiceLoader.class.getTypeParameters()[0],
                getGenericTypeSearchContextObject()
        );
    }

    protected Object getGenericTypeSearchContextObject() {
        return this;
    }

//    @Deprecated
//    protected Class<?> getInterfaceClass() {
//        return typeToClass(solveFromInstance(SelectableServiceLoaderImpl.class.getTypeParameters()[1], $getInstance()));
//    }

    protected Type getServiceType() {
        return solveFromInstance(
                SelectableServiceLoader.class.getTypeParameters()[1],
                getGenericTypeSearchContextObject()
        );
    }

    private ServiceLoader<T> getServiceLoaderFacade() {
        if (serviceLoaderFacade == null) {
            synchronized (this) {
                if (serviceLoaderFacade == null) {
                    // 如果没有指定默认服务，并且指定了异常的函数提供，则代理出一个所有方法
                    T defaultService = defaultServiceSingleton.get();
                    if (defaultService == null && exceptionFunction != null) {
                        // todo 动态继承
                        defaultService = Common.cast(Proxy.newProxyInstance(
                                SelectableService.class.getClassLoader(),
                                new Class<?>[]{typeToClass(getServiceType())},
                                new InvocationHandler() {
                                    @Override
                                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                        if (method.getDeclaringClass().equals(Object.class)) {
                                            return method.invoke(this, args);
                                        } else if (method.getDeclaringClass().equals(SelectableService.class)) {
                                            return true;
                                        } else {
                                            throw exceptionFunction.apply(method);
                                        }
                                    }
                                }
                        ));
                    }
                    this.serviceLoaderFacade = new LazyServiceLoader<T>() {
                        @Override
                        protected T getDefaultInstance() {
                            T defaultService = defaultServiceSingleton.get();
                            return defaultService == null ? super.getDefault() : defaultService;
                        }

                        @Override
                        protected Type getServiceType() {
                            return LazySelectableServiceLoader.this.getServiceType();
                        }

                        //                        @Override
//                        protected Class<?> getInterfaceClass() {
//                            return SelectableServiceLoaderImpl.this.getInterfaceClass();
//                        }
                    };
                }
            }
        }
        return serviceLoaderFacade;
    }

    private boolean accept(T instance, Param_Type param) {
//        Class tClass = instance.getClass();
        Class<?> paramClass = param == null ? null : param.getClass();
//        Type t = ;
        if (paramClass == null) {
            return instance.accept(null);
        }


//         to do 需要考虑泛型数组的问题
//        Class required = typeToClass(
//        solveFromInstance(
//                SelectableService.class.getTypeParameters()[0], instance));
        //(t instanceof ParameterizedType) ? (Class) ((ParameterizedType) t).getRawType() : (Class) t;

//        return instance.accept(param);
        if (ReflectHelper.isMatch(
                paramClass,
                solveFromInstance(SelectableService.class.getTypeParameters()[0], instance))
        ) {
            return instance.accept(param);
        } else {
            return false;
        }
    }

    @Override
    public List<T> selectAll(Param_Type param) {
        List<T> list = new ArrayList<>();
        for (T instance : /*getAll().values()*/ sortedServices.get()) {
            if (accept(instance, param)) {
                list.add(instance);
            }
        }
        try {
            T instance = getServiceLoaderFacade().getDefault();
            if (accept(instance, param)) {
                list.add(instance);
            }
        } catch (Throwable ignored) {
        }
        return list;
    }

    @Override
    public T select(Param_Type param) {
        for (T instance : /*getAll().values()*/ sortedServices.get()) {
            if (accept(instance, param)) {
                return instance;
            }
        }
        try {
            T instance = getServiceLoaderFacade().getDefault();
            if (accept(instance, param)) {
                return instance;
            }
        } catch (Throwable ignored) {
        }
        log.info("no service instance accept this: {}", param);

        return null;
    }

//    //    @Override
//    @Deprecated
//    public Collection<T> getAllInstances() {
//        return getServiceLoaderFacade().getAll().values();
//    }

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
}
