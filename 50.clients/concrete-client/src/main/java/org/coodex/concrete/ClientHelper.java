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

import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.DestinationFactory;
import org.coodex.concrete.client.InstanceBuilder;
import org.coodex.concrete.client.InvokerFactory;
import org.coodex.concrete.client.impl.JavaProxyInstanceBuilder;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.JSONSerializer;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.core.intercept.AsyncInterceptorChain;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.InterceptorChain;
import org.coodex.concrete.core.intercept.SyncInterceptorChain;
import org.coodex.ssl.SSLContextFactory;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.coodex.util.Singleton;

import javax.net.ssl.SSLContext;
import java.util.Set;

import static org.coodex.concrete.common.ConcreteHelper.TAG_CLIENT;

public class ClientHelper {

    private ClientHelper(){}


    private static Singleton<InstanceBuilder> instanceBuilder =
            new Singleton<>(() -> new ServiceLoaderImpl<InstanceBuilder>(new JavaProxyInstanceBuilder()) {
            }.getInstance());

    private static Singleton<AcceptableServiceLoader<Destination, InvokerFactory>> invokerFactoryProviders =
            new Singleton<>(AcceptableServiceLoader::new);

    private static Singleton<AcceptableServiceLoader<String, SSLContextFactory>>
            sslContextFactoryAcceptableServiceLoader
            = new Singleton<>(
            AcceptableServiceLoader::new
    );

    private static Singleton<ServiceLoader<ConcreteInterceptor>> interceptorServiceLoader = new Singleton<>(
            () -> new ServiceLoaderImpl<ConcreteInterceptor>() {
            }
    );

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

    public static JSONSerializer getJSONSerializer() {
        return JSONSerializerFactory.getInstance();
    }

    public static InstanceBuilder getInstanceBuilder() {
        return instanceBuilder.getInstance();
    }

    public static AcceptableServiceLoader<Destination, InvokerFactory> getInvokerFactoryProviders() {
        return invokerFactoryProviders.getInstance();
    }

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
     * @param module
     * @param key
     * @return
     */
    public static String getString(String module, String key) {
        return ConcreteHelper.getString(TAG_CLIENT, module, key);
    }

    public static Destination getDestination(String module) {
        return destinationFactoryAcceptableServiceLoader.getServiceInstance(module).build(module);
    }

    private static AcceptableServiceLoader<String, DestinationFactory<Destination,String>> destinationFactoryAcceptableServiceLoader
            = new AcceptableServiceLoader<>();

    private static AcceptableServiceLoader<String, SSLContextFactory> getSSLContextFactoryAcceptableServiceLoader() {
        return sslContextFactoryAcceptableServiceLoader.getInstance();
    }

    public static SSLContext getSSLContext(String ssl) {
        try {
            return getSSLContextFactoryAcceptableServiceLoader()
                    .getServiceInstance(ssl).getSSLContext(ssl);
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

    @Deprecated
    public static SSLContext getSSLContext(Destination destination) {
        String ssl = getString(destination.getIdentify(), "ssl");
        try {
            return getSSLContextFactoryAcceptableServiceLoader()
                    .getServiceInstance(ssl).getSSLContext(ssl);
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

    private static ServiceLoader<ConcreteInterceptor> getInterceptorServiceLoader() {
        return interceptorServiceLoader.getInstance();
    }

    private static void buildChain(Set<ConcreteInterceptor> chain) {
        for (ConcreteInterceptor interceptor : getInterceptorServiceLoader().getAllInstances()) {
            if (!(interceptor instanceof InterceptorChain))
                chain.add(interceptor);
        }
    }

    public static SyncInterceptorChain getSyncInterceptorChain() {
        return syncInterceptorChain.getInstance();
    }

    public static AsyncInterceptorChain getAsyncInterceptorChain() {
        return asyncInterceptorChain.getInstance();
    }

}
