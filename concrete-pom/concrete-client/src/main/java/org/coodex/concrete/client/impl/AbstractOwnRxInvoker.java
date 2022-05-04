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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.ClientException;
import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.ClientTokenManagement;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.*;
import org.coodex.concrete.own.MapSubjoin;
import org.coodex.concrete.own.OwnServiceUnit;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.concrete.own.ResponsePackage;
import org.coodex.id.IDGenerator;
import org.coodex.logging.Level;
import org.coodex.util.Common;
import org.coodex.util.GenericTypeHelper;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.coodex.concrete.own.PackageHelper.buildRequest;


public abstract class AbstractOwnRxInvoker extends AbstractRxInvoker {

    private final static Logger log = LoggerFactory.getLogger(AbstractOwnRxInvoker.class);
//    private static TimeLimitedMap<String, CompletableFutureCallBack> callbackMap = new TimeLimitedMap<>();

    private static final SingletonMap<String, CompletableFutureCallBack> CALL_BACK_MAP
            = SingletonMap.<String, CompletableFutureCallBack>builder()
            .maxAge(10000L) // 10秒
            .build();

    public AbstractOwnRxInvoker(Destination destination) {
        super(destination);
    }

    protected static JSONSerializer getSerializer() {
        return JSONSerializerFactory.getInstance();
    }

    private static ResponsePackage<Object> parse(String responseMessage) {
        try {
            return getSerializer().parse(responseMessage,
                    new GenericTypeHelper.GenericType<ResponsePackage<Object>>() {
                    }.getType());
        } catch (Throwable throwable) {
            log.warn("cannot parse ResponsePackage: {}", responseMessage, throwable);
            return null;
        }
    }


    // TODO 重构
    static void processMessage(final String message) {

        ResponsePackage<Object> responsePackage = parse(message);
        if (responsePackage == null) return;
        CompletableFutureCallBack completableFutureCallBack = CALL_BACK_MAP.get(responsePackage.getMsgId(), () -> null);
//        CompletableFutureCallBack completableFutureCallBack = callbackMap.getAndRemove(responsePackage.getMsgId());
        if (completableFutureCallBack == null) {
            log.debug("drop message: {}", message);
            return;
        }
        Level level = completableFutureCallBack.getLoggingLevel();
        Logger logger = completableFutureCallBack.getLogger();
        if (level.isEnabled(logger)) {
            level.log(logger, "message received: " + message);
        }

        Throwable throwable = null;
//        boolean completed = false;
        if (responsePackage.isOk()) {
            completableFutureCallBack.getClientSideContext().responseSubjoin(
                    new MapSubjoin(responsePackage.getSubjoin())
            );

            try {
                ClientTokenManagement.setTokenId(completableFutureCallBack.getDestination(), responsePackage.getConcreteTokenId());
                if (responsePackage.getContent() == null ||
                        void.class.equals(completableFutureCallBack.getContext().getDeclaringMethod().getReturnType())) {
                    completableFutureCallBack.getCompletableFuture().complete(null);
//                    completed = true;
                } else {
                    Object result =
                            getSerializer().parse(responsePackage.getContent(),
                                    GenericTypeHelper.toReference(
                                            completableFutureCallBack.getContext().getDeclaringMethod().getGenericReturnType(),
                                            completableFutureCallBack.getContext().getDeclaringClass()));
                    // warning

//                    if (result != null) {
                    completableFutureCallBack.getCompletableFuture().complete(Common.cast(result));
//                    }
//                    completed = true;
                }
            } catch (Throwable th) {
                throwable = th;
            }
        } else {
            try {
                throwable = new ClientException(getSerializer().parse(
                        responsePackage.getContent(),
                        ErrorInfo.class
                ));

            } catch (Throwable th) {
                throwable = th;
            }
        }

        if (throwable != null) {
            completableFutureCallBack.getCompletableFuture().completeExceptionally(throwable);
        }

//        if (completed) {
//            completableFutureCallBack.getEmitter().onComplete();
//        }

    }

    protected abstract ClientSideContext getContext();

    protected abstract OwnServiceUnit findUnit(DefinitionContext context);

    protected abstract Level getLoggingLevel();

    @Override
    protected CompletableFuture<?> futureInvoke(DefinitionContext runtimeContext, ServiceContext serviceContext, Object[] args) {

        CompletableFuture<?> completableFuture = new CompletableFuture<>();
        if (!(serviceContext instanceof ClientSideContext)) {
            completableFuture.completeExceptionally(new IllegalStateException("service context is not ClientSideContext:" + serviceContext));
            return completableFuture;
        }
        ClientSideContext clientSideContext = (ClientSideContext) serviceContext;
        ClientHelper.getRxClientScheduler().execute(() -> {
            final OwnServiceUnit unit = findUnit(runtimeContext);
            // build request
            String msgId = IDGenerator.newId();
            final RequestPackage<?> requestPackage = buildRequest(msgId, unit, args);
            try {
                CALL_BACK_MAP.get(
                        msgId,
                        () -> new CompletableFutureCallBack(
                                completableFuture,
                                runtimeContext,
                                getLogger(),
                                getLoggingLevel(),
                                getDestination(),
                                clientSideContext
                        ),
                        getDestination().getTimeout(),
                        (k, v) -> completableFuture.completeExceptionally(new TimeoutException())
                );
            } catch (Throwable th) {
                completableFuture.completeExceptionally(th);
                return;
            }

            try {
                requestPackage.setConcreteTokenId(
                        ClientTokenManagement.getTokenId(getDestination(),
                                clientSideContext.getTokenId())
                );
                send(requestPackage);
            } catch (Throwable th) {
                CALL_BACK_MAP.remove(msgId);
                completableFuture.completeExceptionally(th);
            }
        });
        return completableFuture;
    }


    protected abstract Logger getLogger();

    protected abstract void send(RequestPackage<?> requestPackage) throws Throwable;

    private static class BaseCallBack {

        private final DefinitionContext context;
        private final Logger logger;
        private final Level loggingLevel;
        private final Destination destination;
        private final ClientSideContext clientSideContext;

        private BaseCallBack(DefinitionContext context, Logger logger, Level loggingLevel, Destination destination, ClientSideContext clientSideContext) {
            this.context = context;
            this.logger = logger;
            this.loggingLevel = loggingLevel;
            this.destination = destination;
            this.clientSideContext = clientSideContext;
        }

        // todo onData/ onError/ onTimeout

        public DefinitionContext getContext() {
            return context;
        }

        public Logger getLogger() {
            return logger;
        }

        public Destination getDestination() {
            return destination;
        }

        public Level getLoggingLevel() {
            return loggingLevel;
        }

        public ClientSideContext getClientSideContext() {
            return clientSideContext;
        }
    }

    private static class CompletableFutureCallBack extends BaseCallBack {
        private final CompletableFuture<?> completableFuture;


        private CompletableFutureCallBack(
                CompletableFuture<?> completableFuture,
                DefinitionContext context,
                Logger logger,
                Level loggingLevel,
                Destination destination,
                ClientSideContext clientSideContext) {
            super(context, logger, loggingLevel, destination, clientSideContext);

            this.completableFuture = completableFuture;
        }

        public CompletableFuture<?> getCompletableFuture() {
            return completableFuture;
        }
    }

//    private static class ObservableCallBack extends BaseCallBack {
//        private final ObservableEmitter emitter;
//
//
//        private ObservableCallBack(ObservableEmitter emitter, DefinitionContext context, Logger logger, Level loggingLevel, Destination destination, ClientSideContext clientSideContext) {
//            super(context, logger, loggingLevel, destination, clientSideContext);
//            this.emitter = emitter;
//        }
//
//        ObservableEmitter getEmitter() {
//            return emitter;
//        }
//    }

}
