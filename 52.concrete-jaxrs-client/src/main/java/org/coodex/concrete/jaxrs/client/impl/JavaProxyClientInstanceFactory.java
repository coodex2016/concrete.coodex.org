/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.jaxrs.client.impl;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.client.AbstractClientInstanceFactory;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Unit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JavaProxyClientInstanceFactory extends AbstractClientInstanceFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T extends ConcreteService> T create(final Class<? extends T> type, final String domain) {
        try {

            InvocationHandler handler = new InvocationHandler() {
                //                private Module module = new Module(type);
                private final Module module = JaxRSHelper.getModule(type, ConcreteHelper.getRemoteApiPackages());

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getDeclaringClass() == Object.class)
                        return method.invoke(this, args);
                    else {
                        int count = args == null ? 0 : args.length;
                        for (Unit unit : module.getUnits()) {
                            if (method.getName().equals(unit.getMethod().getName())
                                    && count == unit.getParameters().length) {

                                return getInvoker(domain).invoke(unit, args, proxy);
                            }
                        }
                        throw new RuntimeException("method not found in [" + type.getName() + "]: [" + method.getName() + "] with " + count + " parameter(s).");
                    }
                }
            };

            return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{type}, handler);
        } catch (Throwable th) {
            throw new RuntimeException(th.getLocalizedMessage(), th);
        }
    }
}
