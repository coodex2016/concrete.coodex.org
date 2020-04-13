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

package org.coodex.concrete.common;

import org.coodex.closure.ClosureContext;
import org.coodex.closure.StackClosureContext;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.ConcreteMethodInvocation;
import org.coodex.concrete.core.intercept.InterceptorChain;
import org.coodex.concrete.core.intercept.SyncInterceptorChain;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.coodex.util.Singleton;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public final class ConcreteContext {

    public static final String KEY_TOKEN = Token.CONCRETE_TOKEN_ID_KEY;
    public static final String KEY_LOCALE = "CONCRETE-LOCALE";
    public static final ClosureContext<Map<String, Object>> LOGGING = new StackClosureContext<>();
    private static final ClosureContext<ServiceContext> CONTEXT = new StackClosureContext<>();

    private static Singleton<SyncInterceptorChain> interceptorChainSingleton = Singleton.with(
            () -> {
                SyncInterceptorChain syncInterceptorChain = new SyncInterceptorChain();
                ServiceLoader<ConcreteInterceptor> serviceLoader = new ServiceLoaderImpl<ConcreteInterceptor>() {
                };
                for (ConcreteInterceptor interceptor : serviceLoader.getAll().values()) {
                    if (interceptor instanceof InterceptorChain) continue;
                    syncInterceptorChain.add(interceptor);
                }
                return syncInterceptorChain;
            }
    );
    private static Map<String, Object> emptyLogging = new Map<String, Object>() {
        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public Object put(String key, Object value) {
            return null;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {

        }

        @Override
        public void clear() {

        }

        @Override
        public Set<String> keySet() {
            return new HashSet<>();
        }

        @Override
        public Collection<Object> values() {
            return new HashSet<>();
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return new HashSet<>();
        }
    };

    public static final ServiceContext getServiceContext() {
        return CONTEXT.get();
    }

    /**
     * 放入记录日志所需的数据
     *
     * @param key   key
     * @param value value
     */
    public static void putLoggingData(String key, Object value) {
        getLogging().put(key, value);
    }

    public static Map<String, Object> getLoggingData() {
        return getLogging();
    }

    private static Map<String, Object> getLogging() {
        Map<String, Object> logging = LOGGING.get();
        return logging == null ? emptyLogging : logging;
    }


//    /**
//     * TO DO 需要考虑分离出去，Logging非必要模型
//     *
//     * @param callable
//     * @return
//     */
//    public static final Object runWithLoggingContext(final CallableClosure callable) {
//        try {
//            return LOGGING.call(new ConcurrentHashMap<String, Object>(), callable);
//        } catch (Throwable th) {
//            throw ConcreteHelper.getException(th);
//        }
//    }


    public static Object runWithContext(final ServiceContext context, final Supplier<?> supplier) {
        try {
            return CONTEXT.call(context, supplier);
        } catch (Throwable throwable) {
            throw ConcreteHelper.getException(throwable);
        }
    }


    /**
     * 服务端运行
     *
     * @param context        context
     * @param supplier       supplier
     * @param interfaceClass interfaceClass
     * @param method         method
     * @param params         params
     * @return 运行结果
     */
    public static Object runServiceWithContext(
            final ServiceContext context,
            final Supplier<?> supplier,
//            AbstractUnit unit,
            Class<?> interfaceClass,
            Method method,
            Object[] params) {
        if (context instanceof ClientSideContext) {
            return runWithContext(context, supplier);
        } else {
            try {
                return CONTEXT.call(context, () -> {
//                    RuntimeContext runtimeContext = RuntimeContext.getRuntimeContext(method, interfaceClass);
                    ServiceMethodInvocation invocation = new ServiceMethodInvocation(method, params) {
                        @Override
                        public Class<?> getInterfaceClass() {
                            return interfaceClass;
                        }

                        @Override
                        public Object proceed() throws Throwable {
                            return supplier.get();
                        }

                        @Override
                        public Object getThis() {
                            return BeanServiceLoaderProvider.getBeanProvider().getBean(interfaceClass);
                        }
                    };
                    try {
                        return interceptorChainSingleton.get().invoke(invocation);
                    } catch (Throwable throwable) {
                        throw Common.rte(throwable);
                    }
//                    interceptorChainSingleton.getInstance().before(runtimeContext, invocation);
//                    try {
//                        return interceptorChainSingleton.getInstance()
//                                .after(runtimeContext, invocation, callable.call());
//                    } catch (Throwable th) {
//                        throw ConcreteHelper.findException(interceptorChainSingleton.getInstance()
//                                .onError(runtimeContext, invocation, th));
//                    }
                });
            } catch (Throwable throwable) {
                throw ConcreteHelper.getException(throwable);
            }
        }
    }

    private abstract static class ServiceMethodInvocation implements ConcreteMethodInvocation {
        private final Object[] arguments;
        //        private final Object instance;
        private final Method method;

        private ServiceMethodInvocation(Method method, Object[] arguments) {
            this.arguments = arguments;
//            this.instance = instance;
            this.method = method;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return arguments;
        }

//        @Override
//        public Object getThis() {
//            return instance;
//        }

        @Override
        public AccessibleObject getStaticPart() {
            // TODO 待验证
            return null;
        }
    }
}
