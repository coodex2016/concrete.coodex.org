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
import org.coodex.util.Common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Supplier;

import static org.coodex.concrete.ClientHelper.getDestination;
import static org.coodex.concrete.ClientHelper.getInstanceBuilder;
import static org.coodex.concrete.client.ClientSideContext.SUBJOIN_CONTEXT;


public class Client {
    public static <T> T getInstance(Class<T> concreteServiceClass) {
        return getInstance(concreteServiceClass, (String) null);
    }

    public static <T> T getInstance(Class<T> concreteServiceClass, Destination destination) {
        return getInstanceBuilder().build(destination, concreteServiceClass);
    }

    public static <T> T getInstance(Class<T> concreteServiceClass, String module) {
        return getInstance(concreteServiceClass, getDestination(module));
    }

    public static <T> Builder<T> newBuilder(Class<T> concreteServiceClass) {
        return new Builder<>(getInstance(concreteServiceClass), concreteServiceClass);
    }

    public static <T> Builder<T> newBuilder(Class<T> concreteServiceClass, Destination destination) {
        return new Builder<>(getInstance(concreteServiceClass, destination), concreteServiceClass);
    }

    public static <T> Builder<T> newBuilder(Class<T> concreteServiceClass, String module) {
        return new Builder<>(getInstance(concreteServiceClass, module), concreteServiceClass);
    }

    public static class Builder<T> {
        private final T instance;
        private final Class<T> concreteServiceClass;

        private Builder(T instance, Class<T> concreteServiceClass) {
            this.instance = instance;
            this.concreteServiceClass = concreteServiceClass;
        }

        public T withSubjoin(Map<String, String> subjoin) {
            if (subjoin != null) {
                return wrapInstance(() -> subjoin);
            } else {
                return instance;
            }
        }

        private T wrapInstance(Supplier<Map<String, String>> subjoinSupplier) {
            return Common.cast(Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class<?>[]{concreteServiceClass},
                    (proxy, method, args) -> SUBJOIN_CONTEXT.call(subjoinSupplier.get(), () -> {
                        try {
                            return args == null || args.length == 0 ?
                                    method.invoke(instance) :
                                    method.invoke(instance, args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw Common.rte(e);
                        }
                    }))
            );
        }

        public T withSubjoin(Supplier<Map<String, String>> subjoinSupplier) {
            if (subjoinSupplier != null) {
                return wrapInstance(subjoinSupplier);
            } else {
                return instance;
            }
        }
    }


}
