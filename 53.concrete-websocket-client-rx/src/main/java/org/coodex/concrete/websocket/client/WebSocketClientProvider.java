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

package org.coodex.concrete.websocket.client;

import io.reactivex.Observable;
import org.coodex.concrete.client.ClientCommon;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.rx.RXClientProvider;
import org.coodex.concrete.rx.ReactiveExtensionFor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class WebSocketClientProvider implements RXClientProvider {
    @Override
    public <T> T getInstance(Class<T> clz, final ClientCommon.Domain domain, final String tokenManagerKey) {
        try {

            final Class serviceClass = clz.getAnnotation(ReactiveExtensionFor.class).value();

            return (T) Proxy.newProxyInstance(WebSocketClientProvider.class.getClassLoader(), new Class[]{clz},
                    new InvocationHandler() {

                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            return Observable.create(
                                    WebSocketClientHandle.getInstance()
                                            .buildObservable(
                                                    domain.getIdentify(),
                                                    tokenManagerKey,
                                                    serviceClass,
                                                    method,
                                                    args));
                        }
                    });
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

    @Override
    public boolean accept(ClientCommon.Domain param) {
        String host = param.getIdentify().toLowerCase();
        return host.startsWith("ws://") || host.startsWith("wss://");
    }
}
