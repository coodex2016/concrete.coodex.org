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

package org.coodex.concrete.rx.jaxrs;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.client.ClientCommon;
import org.coodex.concrete.jaxrs.Client;
import org.coodex.concrete.rx.RXClientProvider;
import org.coodex.concrete.rx.ReactiveExtensionFor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Deprecated
public class JaxRS_RXClientProvider implements RXClientProvider {


    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clz, final ClientCommon.Domain domain, String tokenManagerKey) {
        final Class<? extends ConcreteService> serviceClass = clz.getAnnotation(ReactiveExtensionFor.class).value();

        final ConcreteService instance = Client.getInstance(serviceClass, domain.getIdentify(), tokenManagerKey);

        return (T) Proxy.newProxyInstance(JaxRS_RXClientProvider.class.getClassLoader(), new Class[]{clz},
                new AbstractRxInvocationHandler(serviceClass) {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                return domain.isAsyncSupport() ? async(instance, findMethod(method), args) : sync(instance, findMethod(method), args);
            }
        });


    }

    @Override
    public boolean accept(ClientCommon.Domain param) {
        String host = param.getIdentify().toLowerCase();
        return host.startsWith("http://") || host.startsWith("https://");
    }
}
