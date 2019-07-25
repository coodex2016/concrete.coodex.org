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

package org.coodex.concrete.client.websocket;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.coodex.concrete.client.ClientTokenManagement;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.rx.AbstractRxInvoker;
import org.coodex.concrete.common.*;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.concrete.own.ResponsePackage;
import org.coodex.concrete.websocket.WebSocketUnit;
import org.coodex.util.Common;
import org.coodex.util.Singleton;

import static org.coodex.concrete.own.PackageHelper.buildRequest;
import static org.coodex.concrete.websocket.WebSocketHelper.findUnit;
import static org.coodex.util.GenericTypeHelper.toReference;


public class WSInvoker extends AbstractRxInvoker {


    private static Singleton<WSClientHandle> handle = new Singleton<>(
            WSClientHandle::new
    );
    private JSONSerializer serializer = JSONSerializerFactory.getInstance();

    WSInvoker(Destination destination) {
        super(destination);
    }

    @Override
    public ServiceContext buildContext(DefinitionContext context) {
        return new WSClientServiceContext(getDestination(), context);
    }

    private WSClientServiceContext getContext() {
        ServiceContext context = ConcreteContext.getServiceContext();
        if (context instanceof WSClientServiceContext)
            return (WSClientServiceContext) context;
        else
            throw new RuntimeException("context [" + context + "] is NOT WSClientServiceContext");
    }

    @Override
    public Observable invoke(final DefinitionContext context, final Object... args) {
        final WSClientServiceContext wsClientServiceContext = getContext();
        final WebSocketUnit unit = findUnit(context);

        //noinspection unchecked
        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(final ObservableEmitter emitter) {
                // build request
                String msgId = Common.getUUIDStr();
                final RequestPackage requestPackage = buildRequest(msgId, unit, args);
                requestPackage.setConcreteTokenId(
                        ClientTokenManagement.getTokenId(getDestination(), wsClientServiceContext.getTokenId())
                );

                // send with callback
                try {
                    handle.get()
                            .send((WebsocketDestination) getDestination(), requestPackage, new WSClientHandle.WSCallback() {

                        private boolean completed = false;

                        private void complete() {
                            if (!completed) {
                                synchronized (this) {
                                    if (!completed) {
                                        emitter.onComplete();
                                        completed = true;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onReturn(ResponsePackage<Object> responsePackage) {
                            try {
                                ClientTokenManagement.setTokenId(getDestination(), responsePackage.getConcreteTokenId());
                                if (responsePackage.getContent() != null && !void.class.equals(context.getDeclaringMethod().getReturnType())) {
                                    Object result = serializer.parse(responsePackage.getContent(),
                                            toReference(context.getDeclaringMethod().getGenericReturnType(),
                                                    context.getDeclaringClass()));
                                    if (result != null)
                                        //noinspection unchecked
                                        emitter.onNext(result);
                                }
                            } catch (Throwable throwable) {
                                emitter.onError(throwable);
                            } finally {
                                complete();
                            }
                        }

                        @Override
                        public void onError(Throwable th) {
                            try {
                                emitter.onError(th);
                            } finally {
                                complete();
                            }
                        }
                    });
                } catch (Throwable th) {
                    th.printStackTrace();
                    emitter.onError(th);
                    emitter.onComplete();
                }

            }
        }
        );
    }


}
