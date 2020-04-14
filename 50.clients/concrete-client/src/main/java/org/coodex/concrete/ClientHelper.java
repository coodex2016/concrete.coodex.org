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

package org.coodex.concrete;

import org.coodex.concrete.api.rx.CompletableFutureBridge;
import org.coodex.concrete.api.rx.ReactiveExtensionFor;
import org.coodex.concrete.client.*;
import org.coodex.concrete.client.impl.JavaProxyInstanceBuilder;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.JSONSerializer;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.core.intercept.AsyncInterceptorChain;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.InterceptorChain;
import org.coodex.concrete.core.intercept.SyncInterceptorChain;
import org.coodex.ssl.SSLContextFactory;
import org.coodex.util.*;

import javax.net.ssl.SSLContext;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import static org.coodex.concrete.common.ConcreteHelper.TAG_CLIENT;

public class ClientHelper {

    private static final Singleton<ScheduledExecutorService> SCHEDULED_EXECUTOR_SERVICE_SINGLETON = new Singleton<>(
            () -> ConcreteHelper.getScheduler("rx-client")
    );
    @SuppressWarnings("rawtypes")
    private static final LazySelectableServiceLoader<Class, CompletableFutureBridge> BRIDGE_LOADER =
            new LazySelectableServiceLoader<Class, CompletableFutureBridge>() {
            };
    //            new Singleton<>(() -> new SelectableServiceLoader<Class, CompletableFutureBridge>() {
//            });
    private static Singleton<InstanceBuilder> instanceBuilder =
            new Singleton<>(() -> new ServiceLoaderImpl<InstanceBuilder>(new JavaProxyInstanceBuilder()) {
            }.get());
    //            new Singleton<>(()->new SelectableServiceLoader<Destination, InvokerFactory>() {
//            });
    private static LazySelectableServiceLoader<Destination, InvokerFactory> invokerFactoryProviders =
            new LazySelectableServiceLoader<Destination, InvokerFactory>() {
            };
    private static LazySelectableServiceLoader<String, SSLContextFactory> sslContextFactoryAcceptableServiceLoader =
            new LazySelectableServiceLoader<String, SSLContextFactory>() {
            };

    //            = new Singleton<>(() -> new SelectableServiceLoader<String, SSLContextFactory>() {
//    });
    private static LazyServiceLoader<ConcreteInterceptor> interceptorServiceLoader =
            new LazyServiceLoader<ConcreteInterceptor>() {
            };
    //            new Singleton<>(
//            () -> new ServiceLoaderImpl<ConcreteInterceptor>() {
//            }
//    );
    private static Singleton<SyncInterceptorChain> syncInterceptorChain = new Singleton<>(
            () -> {
                SyncInterceptorChain instance = new SyncInterceptorChain();
                buildChain(instance);
                return instance;
            }
    );
    private static Singleton<AsyncInterceptorChain> asyncInterceptorChain =
            new Singleton<>(() -> {
                AsyncInterceptorChain instance = new AsyncInterceptorChain();
                buildChain(instance);
                return instance;
            });
    private static SelectableFactoryLoader<String, Destination, DestinationFactory<Destination,String>> destinationFactorySelectableServiceLoader
            = new SelectableFactoryLoaderImpl<String, Destination, DestinationFactory<Destination,String>>() {
    };

    private ClientHelper() {
    }

    public static JSONSerializer getJSONSerializer() {
        return JSONSerializerFactory.getInstance();
    }

    public static InstanceBuilder getInstanceBuilder() {
        return instanceBuilder.get();
    }

//    public static SelectableServiceLoader<Destination, InvokerFactory> getInvokerFactoryProviders() {
//        return invokerFactoryProviders.get();
//    }

    @SuppressWarnings("unchecked")
    public static boolean isReactiveExtension(Class<?> clz) {
        try {
            Class<? extends java.lang.annotation.Annotation> rx =
                    (Class<? extends java.lang.annotation.Annotation>)
                            Class.forName("org.coodex.concrete.rx.ReactiveExtensionFor");
            return clz.getAnnotation(rx) != null;
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean isConcreteService(Class<?> clz) {
        return ConcreteHelper.isConcreteService(clz) || isReactiveExtension(clz);
    }

    /**
     * @param module module
     * @param key    key
     * @return value
     */
    public static String getString(String module, String key) {
        return ConcreteHelper.getString(TAG_CLIENT, module, key);
    }

    public static Destination getDestination(String module) {
        return destinationFactorySelectableServiceLoader.build(module);
    }

//    private static SelectableServiceLoaderImpl<String, SSLContextFactory> getSSLContextFactoryAcceptableServiceLoader() {
//        return sslContextFactoryAcceptableServiceLoader.get();
//    }

    public static SSLContext getSSLContext(String ssl) {
        try {
            return sslContextFactoryAcceptableServiceLoader.select(ssl).getSSLContext(ssl);
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

    @Deprecated
    public static SSLContext getSSLContext(Destination destination) {
        String ssl = getString(destination.getIdentify(), "ssl");
        try {
            return sslContextFactoryAcceptableServiceLoader.select(ssl).getSSLContext(ssl);
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

//    private static ServiceLoader<ConcreteInterceptor> getInterceptorServiceLoader() {
//        return interceptorServiceLoader.get();
//    }

    private static void buildChain(Set<ConcreteInterceptor> chain) {
        for (ConcreteInterceptor interceptor : interceptorServiceLoader.getAll().values()) {
            if (!(interceptor instanceof InterceptorChain))
                chain.add(interceptor);
        }
    }

    public static SyncInterceptorChain getSyncInterceptorChain() {
        return syncInterceptorChain.get();
    }

    public static AsyncInterceptorChain getAsyncInterceptorChain() {
        return asyncInterceptorChain.get();
    }

    /**
     * @param destination destination
     * @param clazz       clazz
     * @return 根据指定的类确定实际的Invoker
     */
    public static Invoker getInvoker(Destination destination, Class<?> clazz) {
        InvokerFactory invokerFactory = IF.isNull(invokerFactoryProviders.select(destination),
                "Cannot found InvokerFactory for " + destination.toString());
        return clazz.getAnnotation(ReactiveExtensionFor.class) == null ?
                invokerFactory.getSyncInvoker(destination) :
                invokerFactory.getRxInvoker(destination);
    }

    public static ScheduledExecutorService getRxClientScheduler() {
        return SCHEDULED_EXECUTOR_SERVICE_SINGLETON.get();
    }

    public static CompletableFutureBridge getCompletableFutureBridge(Class<?> type) {
        return BRIDGE_LOADER.select(type);
    }


}
