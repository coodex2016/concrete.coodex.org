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

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.struct.AbstractParam;
import org.coodex.concrete.core.intercept.AsyncInterceptorChain;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.websocket.*;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.coodex.concrete.common.ConcreteContext.*;
import static org.coodex.concrete.websocket.Constants.*;

@ClientEndpoint(configurator = SetUserAgentConfigurator.class)
public class WebSocketClientHandle {

    private static class Client extends WebSocket {
        static Set<BroadcastListener> getRegisteredListeners() {
            return getListeners();
        }
    }

    private static final WebSocketClientHandle handler = new WebSocketClientHandle();

    public static WebSocketClientHandle getInstance() {
        return handler;
    }

    private final static Logger log = LoggerFactory.getLogger(WebSocketClientHandle.class);

    private Map<String, WebSocketUnit> unitMap = new HashMap<String, WebSocketUnit>();
    private Set<Class> loaded = new HashSet<Class>();

    private Map<String, WebSocketCallback> callbackMap = new HashMap<String, WebSocketCallback>();

    private Map<String, Session> sessionMap = new HashMap<String, Session>();
    private Map<String, Map<String, String>> subjoinMap = new HashMap<String, Map<String, String>>();

    private static ScheduledExecutorService scheduledExecutorService =
            ExecutorsHelper.newScheduledThreadPool(1);


    private WebSocketClientHandle() {
    }

    ObservableOnSubscribe buildObservable(final String domain, final Class serviceClass, final Method method, final Object[] args) {
        synchronized (this) {
            if (!loaded.contains(serviceClass)) {
                WebSocketModule webSocketModule = new WebSocketModule(serviceClass);
                for (WebSocketUnit unit : webSocketModule.getUnits()) {
                    unitMap.put(unit.getKey(), unit);
                }
                loaded.add(serviceClass);
            }
        }

        final String unitKey = buildKey(serviceClass, method);
        final WebSocketUnit unit = unitMap.get(unitKey);


        return new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                try {
                    Assert.isNull(unit, WebSocketErrorCodes.UNIT_NOT_EXISTS, keyBase(serviceClass, method));

                    final Session session = Assert.isNull(getSession(domain), WebSocketErrorCodes.CANNOT_OPEN_SESSION, domain);

                    String msgId = Common.getUUIDStr();
                    final RequestPackage requestPackage = buildRequest(msgId, unit, args);
                    final WebSocketCallback callback = registerCallback(
                            msgId, unit, e,
                            toRuntimeContext(unit), new AsyncMethodInvocation(unit.getMethod(), args));

                    try {
                        runWithContext(
                                new WebSocketClientServiceContext(callback.getUnit(), new WebSocketSubjoin(getSubjoin(domain))),
                                new ConcreteClosure() {
                                    @Override
                                    public Object concreteRun() throws Throwable {
                                        getInterceptorChain().before(callback.getContext(), callback.getInvocation());
                                        requestPackage.setSubjoin(((WebSocketSubjoin) getServiceContext().getSubjoin()).toMap());
                                        sendRequest(requestPackage, session);
                                        return null;
                                    }
                                });

                    } catch (Throwable th) {
                        callbackMap.remove(msgId);
                        callback.getFuture().cancel(true);
                        throw th;
                    }

                } catch (Throwable th) {
                    e.onError(ConcreteHelper.getException(th));
                }


            }
        };
    }

    private static RuntimeContext toRuntimeContext(WebSocketUnit unit) {
        return RuntimeContext.getRuntimeContext(unit.getMethod(), unit.getDeclaringModule().getInterfaceClass());
    }

    private WebSocketCallback registerCallback(final String msgId, WebSocketUnit unit, final ObservableEmitter e, RuntimeContext context, MethodInvocation invocation) {
        WebSocketCallback webSocketCallback = new WebSocketCallback(msgId, unit,
                scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        callbackMap.remove(msgId);
                        e.onError(new ConcreteException(WebSocketErrorCodes.RESPONSE_TIMEOUT));
                    }
                }, 15, TimeUnit.MINUTES), e, context, invocation);
        callbackMap.put(msgId, webSocketCallback);
        return webSocketCallback;
    }

    private RequestPackage buildRequest(String msgId, WebSocketUnit unit, Object[] args) {
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMsgId(msgId);
        requestPackage.setServiceId(unit.getKey());

        AbstractParam[] parameters = unit.getParameters();
        switch (parameters.length) {
            case 0:
                break;
            case 1:
                requestPackage.setContent(args[0]);
                break;
            default:
                Map<String, Object> toSend = new HashMap<String, Object>();
                for (int i = 0; i < parameters.length; i++) {
                    toSend.put(parameters[i].getName(), args[i]);
                }
                requestPackage.setContent(toSend);
                break;
        }
        return requestPackage;
    }

    private String toJson(Object o) {
        return JSONSerializerFactory.getInstance().toJson(o);
    }

    private void sendRequest(RequestPackage requestPackage, Session session) {
        String content = toJson(requestPackage);
        log.debug("session {} send message:\n{}", session.getId(), content);
        session.getAsyncRemote().sendText(content);
    }

    private Map<String, String> getSubjoin(String domain) {
        return subjoinMap.get(domain);
    }

    private Session getSession(String domain) throws InterruptedException, URISyntaxException, IOException, DeploymentException {
        synchronized (sessionMap) {
            Session session = sessionMap.get(domain);
            if (session == null || !session.isOpen()) {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                session = container.connectToServer(this, new URI(domain));
                session.setMaxIdleTimeout(0);
                sessionMap.put(domain, session);
            }

            int maxRetryTimes = 10, retried = 0;
            while (!session.isOpen() && retried++ < maxRetryTimes) {
                Thread.sleep(100);
            }
            if (!session.isOpen()) {
                sessionMap.remove(domain);
                return null;
            } else
                return session;
        }
    }

    private String keyBase(Class serviceClass, Method method) {
        return String.format("%s:%s(%d)",
                serviceClass.getName(),
                method.getName(),
                method.getParameterTypes().length);
    }

    private String buildKey(Class serviceClass, Method method) {
        return Common.sha1(keyBase(serviceClass, method));
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        log.debug("session {} closed.", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {

        log.debug("session {} received msg : \n{}", session.getId(), message);


        ResponsePackage<Object> responsePackage = JSONSerializerFactory.getInstance().parse(
                message, new GenericType<ResponsePackage<Object>>() {
                }.genericType()
        );

        Map<String, String> subjoin = responsePackage.getSubjoin();

        if (subjoin != null) {

        }

        if (subjoin == null || !"true".equals(subjoin.get(BROADCAST))) {
            onReturn(responsePackage, session);
        } else {
            onBroadcast(responsePackage, session);
        }
    }


    private void onReturn(ResponsePackage<Object> responsePackage, final Session session) {
        final WebSocketCallback callback = callbackMap.get(responsePackage.getMsgId());
        if (callback == null) {
            log.warn("cannot found callback for {}", responsePackage.getMsgId());
            return;
        }


        callbackMap.remove(responsePackage.getMsgId());
        callback.getFuture().cancel(true);

        try {
            Object result = null;
            if (!responsePackage.isOk()) {
                throw new WebSocketClientException(
                        JSONSerializerFactory.getInstance().<ErrorInfo>parse(
                                responsePackage.getContent(), ErrorInfo.class));

            }
            if (responsePackage.getContent() != null) {
                result = JSONSerializerFactory.getInstance().parse(responsePackage.getContent(),
                        TypeHelper.toTypeReference(callback.getUnit().getGenericReturnType(),
                                callback.getUnit().getDeclaringModule().getInterfaceClass()));
            }
            final Object o = result;

            runWithContext(
                    new WebSocketClientServiceContext(callback.getUnit(), new WebSocketSubjoin(responsePackage.getSubjoin())),
                    new ConcreteClosure() {
                        @Override
                        public Object concreteRun() throws Throwable {
                            Object r = o;
                            r = getInterceptorChain().after(callback.getContext(), callback.getInvocation(), r);
                            if (r != null) {
                                callback.getEmitter().onNext(r);
                            }
                            callback.getEmitter().onComplete();
                            return null;
                        }
                    });

        } catch (Throwable th) {
            callback.getEmitter().onError(ConcreteHelper.getException(th));
        }
    }

//    WebSocketUnit getUnit(DefinitionContext definitionContext) {
//        return Assert.isNull(
//                unitMap.get(buildKey(definitionContext.getDeclaringClass(), definitionContext.getDeclaringMethod())),
//                WebSocketErrorCodes.UNIT_NOT_EXISTS,
//                keyBase(definitionContext.getDeclaringClass(), definitionContext.getDeclaringMethod())
//        );
//    }

    private final static AcceptableServiceLoader<String, BroadcastListener> listenerLoader
            = new AcceptableServiceLoader<String, BroadcastListener>(new ConcreteServiceLoader<BroadcastListener>() {
    });

    private boolean handleBroadcast(BroadcastListener listener, ResponsePackage<Object> responsePackage) {
        try {
            String subject = responsePackage.getSubjoin().get(SUBJECT);
            if (listener.accept(subject)) {
                listener.onBroadcast(responsePackage.getMsgId(),
                        responsePackage.getSubjoin().get(HOST_ID),
                        subject,
                        toJson(responsePackage.getContent()));
                return true;
            }
        } catch (Throwable th) {
            log.warn("{}", th.getLocalizedMessage(), th);
        }
        return false;
    }

    private void onBroadcast(ResponsePackage<Object> responsePackage, Session session) {
        boolean handle = false;
        String subject = responsePackage.getSubjoin().get(SUBJECT);
        for (BroadcastListener listener : Client.getRegisteredListeners()) {
            if (handleBroadcast(listener, responsePackage)) handle = true;
        }
        for (BroadcastListener listener : listenerLoader.getAllInstances()) {
            if (handleBroadcast(listener, responsePackage)) handle = true;
        }
        if (!handle) {
            log.warn("no listener found for subject [{}]: \n {}", subject, responsePackage.getContent());
        }
    }


    private static AsyncInterceptorChain asyncInterceptorChain;

    private synchronized static AsyncInterceptorChain getInterceptorChain() {
        if (asyncInterceptorChain == null) {
            ServiceLoader<ConcreteInterceptor> spiFacade = new ConcreteServiceLoader<ConcreteInterceptor>() {
            };
            asyncInterceptorChain = new AsyncInterceptorChain();
            for (ConcreteInterceptor interceptor : spiFacade.getAllInstances()) {
                asyncInterceptorChain.add(interceptor);
            }
        }
        return asyncInterceptorChain;
    }
}
