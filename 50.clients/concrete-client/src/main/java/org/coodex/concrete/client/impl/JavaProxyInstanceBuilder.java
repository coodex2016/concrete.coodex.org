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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.InstanceBuilder;
import org.coodex.concrete.common.Assert;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JavaProxyInstanceBuilder implements InstanceBuilder {

    @Override
    public <T> T build(final Destination destination, final Class<T> clazz) {


        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{clazz},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getDeclaringClass().equals(Object.class))
                            return method.invoke(this, args);


                        return Assert.isNull(ClientHelper.getInvokerFactoryProviders().getServiceInstance(destination),
                                "Cannot found InvokerFactory for "+ destination.toString())
                                .getInvoker(destination).invoke(proxy, clazz, method, args);
                    }
                });
    }
}
