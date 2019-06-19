/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.client.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.coodex.concrete.ClientException;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.ClientTokenManagement;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ErrorInfo;
import org.coodex.concrete.common.JSONSerializer;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.own.OwnServiceUnit;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.concrete.own.ResponsePackage;
import org.coodex.concurrent.TimeLimitedMap;
import org.coodex.util.Common;
import org.coodex.util.GenericType;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

import static org.coodex.concrete.own.PackageHelper.buildRequest;


public abstract class AbstractOwnRxInvoker extends AbstractRxInvoker {

    private final static Logger log = LoggerFactory.getLogger(AbstractOwnRxInvoker.class);
    protected static JSONSerializer serializer = JSONSerializerFactory.getInstance();
    private static TimeLimitedMap<String, CallBack> callbackMap = new TimeLimitedMap<String, CallBack>();

    public AbstractOwnRxInvoker(Destination destination) {
        super(destination);
    }

    private static ResponsePackage<Object> parse(String reponseMessage) {
        try {
            return serializer.parse(reponseMessage,
                    new GenericType<ResponsePackage<Object>>() {
                    }.genericType());
        } catch (Throwable throwable) {
            log.warn("cannot parse ResponsePackage: {}", reponseMessage, throwable);
            return null;
        }
    }

    static void processMessage(final String message) {

        ResponsePackage<Object> responsePackage = parse(message);
        if (responsePackage == null) return;

        CallBack callBack = callbackMap.getAndRemove(responsePackage.getMsgId());
        if (callBack == null) {
            log.debug("drop message: {}", message);
            return;
        }

        if (callBack.getLogger().isDebugEnabled()) {
            callBack.getLogger().debug("message received: {}", message);
        }

        Throwable throwable = null;
        boolean completed = false;
        if (responsePackage.isOk()) {
            try {
                ClientTokenManagement.setTokenId(callBack.getDestination(), responsePackage.getConcreteTokenId());
                if (responsePackage.getContent() == null ||
                        void.class.equals(callBack.getContext().getDeclaringMethod().getReturnType())) {

                    completed = true;
                } else {
                    Object result =
                            serializer.parse(responsePackage.getContent(),
                                    TypeHelper.toTypeReference(
                                            callBack.getContext().getDeclaringMethod().getGenericReturnType(),
                                            callBack.getContext().getDeclaringClass()));
                    if (result != null)
                        callBack.getEmitter().onNext(result);
                    completed = true;
                }
            } catch (Throwable th) {
                throwable = th;
            }
        } else {
            try {
                throwable = new ClientException(serializer.<ErrorInfo>parse(
                        responsePackage.getContent(),
                        ErrorInfo.class
                ));

            } catch (Throwable th) {
                throwable = th;
            }
        }

        if (throwable != null) {
            callBack.getEmitter().onError(throwable);
        }

        if (completed) {
            callBack.getEmitter().onComplete();
        }

    }

    protected abstract ClientSideContext getContext();

    protected abstract OwnServiceUnit findUnit(DefinitionContext context);

    @Override
    protected Observable invoke(final DefinitionContext context, final Object... args) {
        final OwnServiceUnit unit = findUnit(context);
        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(final ObservableEmitter observableEmitter) throws Exception {
                // build request
                String msgId = Common.getUUIDStr();
                final RequestPackage requestPackage = buildRequest(msgId, unit, args);

                CallBack callBack = new CallBack(observableEmitter,
                        context, getLogger(), getDestination());

                callbackMap.put(msgId, callBack, getDestination().getTimeout(),
                        new TimeLimitedMap.TimeoutCallback() {
                            @Override
                            public void timeout() {
                                observableEmitter.onError(new TimeoutException());
                            }
                        });

                try {
                    requestPackage.setConcreteTokenId(
                            ClientTokenManagement.getTokenId(getDestination(),
                                    getContext().getTokenId())
                    );
                    send(requestPackage);
                } catch (Throwable th) {
                    callbackMap.getAndRemove(msgId);
                    observableEmitter.onError(th);
                }
            }
        });
    }

    protected abstract Logger getLogger();

    protected abstract void send(RequestPackage requestPackage) throws Throwable;

    private static class CallBack {
        private final ObservableEmitter emitter;
        private final DefinitionContext context;
        private final Logger logger;
        private final Destination destination;

        private CallBack(ObservableEmitter emitter, DefinitionContext context, Logger logger, Destination destination) {
            this.emitter = emitter;
            this.context = context;
            this.logger = logger;
            this.destination = destination;
        }

        public ObservableEmitter getEmitter() {
            return emitter;
        }

        public DefinitionContext getContext() {
            return context;
        }

        public Logger getLogger() {
            return logger;
        }

        public Destination getDestination() {
            return destination;
        }
    }

}
