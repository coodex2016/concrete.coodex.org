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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.coodex.util.GenericTypeHelper.solveFromInstance;
import static org.coodex.util.GenericTypeHelper.typeToClass;


/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class SelectableServiceLoaderImpl<Param_Type, T extends SelectableService<Param_Type>>
        implements SelectableServiceLoader<Param_Type, T>, ServiceLoader<T> {

    public static final Common.Function<Method, RuntimeException> EXCEPTION_FUNCTION = new Common.Function<Method, RuntimeException>() {
        @Override
        public RuntimeException apply(Method method) {
            return new RuntimeException("no instance found."
                    + method.getDeclaringClass().getName()
                    + "." + method.getName());
        }
    };


    private final static Logger log = LoggerFactory.getLogger(SelectableServiceLoaderImpl.class);

    private ServiceLoader<T> serviceLoaderFacade;
    private T defaultService = null;
    private Common.Function<Method, RuntimeException> exceptionFunction = null;

    public SelectableServiceLoaderImpl() {
        this((T) null);
    }

    public SelectableServiceLoaderImpl(final T defaultService) {
        this.defaultService = defaultService;
    }

    /**
     * 根据给出的function提供一个默认loader
     *
     * @param exceptionFunction
     */
    public SelectableServiceLoaderImpl(Common.Function<Method, RuntimeException> exceptionFunction) {
        this.exceptionFunction = exceptionFunction;
    }

    @Deprecated
    public SelectableServiceLoaderImpl(ServiceLoader<T> serviceLoaderFacade) {
        this.serviceLoaderFacade = serviceLoaderFacade;
    }

    @SuppressWarnings("unchecked")
    protected Class<Param_Type> getParamType() {
        return typeToClass(solveFromInstance(SelectableServiceLoaderImpl.class.getTypeParameters()[0], this));
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getInterfaceClass() {
        return typeToClass(solveFromInstance(SelectableServiceLoaderImpl.class.getTypeParameters()[1], this));
    }


    private ServiceLoader<T> getServiceLoaderFacade() {
        if (serviceLoaderFacade == null) {
            synchronized (this) {
                if (serviceLoaderFacade == null) {
                    // 如果没有指定默认服务，并且指定了异常的函数提供，则代理出一个所有方法
                    if (defaultService == null && exceptionFunction != null) {
                        //noinspection unchecked
                        defaultService = (T) Proxy.newProxyInstance(
                                SelectableService.class.getClassLoader(),
                                new Class[]{getInterfaceClass()},
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
                        );
                    }
                    this.serviceLoaderFacade = new ServiceLoaderImpl<T>() {
                        @Override
                        public T getDefault() {
                            return defaultService == null ? super.getDefault() : defaultService;
                        }

                        @Override
                        protected Class<T> getInterfaceClass() {
                            return SelectableServiceLoaderImpl.this.getInterfaceClass();
                        }
                    };
                }
            }
        }
        return serviceLoaderFacade;
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean accept(T instance, Param_Type param) {
//        Class tClass = instance.getClass();
        Class paramClass = param == null ? null : param.getClass();
//        Type t = ;
        if (paramClass == null)
            return instance.accept(null);

        // todo 需要考虑泛型数组的问题
        Class required = typeToClass(solveFromInstance(
                SelectableService.class.getTypeParameters()[0], instance));
        //(t instanceof ParameterizedType) ? (Class) ((ParameterizedType) t).getRawType() : (Class) t;

        if (required != null && required.isAssignableFrom(paramClass)) {
            return instance.accept(param);
        } else {
            return false;
        }
    }

    /**
     * @param param param
     * @return {@link SelectableServiceLoaderImpl#selectAll(Object)}
     */
//    @Override
    @Deprecated
    public List<T> getServiceInstances(Param_Type param) {
        return selectAll(param);
    }

    @Override
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
     * @return {@link SelectableServiceLoaderImpl#select(Object)}
     */
//    @Override
    @Deprecated
    public T getServiceInstance(Param_Type param) {
        return select(param);
    }

    @Override
    public T select(Param_Type param) {
        for (T instance : getAll().values()) {
            if (accept(instance, param))
                return instance;
        }
        try {
            T instance = getServiceLoaderFacade().getDefault();
            if (accept(instance, param))
                return instance;
            if (instance.accept(param))
                return instance;
        } catch (Throwable ignored) {
        }
        log.info("no service instance accept this: {}", param);

        return null;
    }

//    @Override
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
