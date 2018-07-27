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

package org.coodex.concrete.support.websocket;

import org.coodex.closure.CallableClosure;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;
import org.coodex.concrete.common.*;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.common.struct.AbstractParam;
import org.coodex.concrete.websocket.ConcreteWebSocketEndPoint;
import org.coodex.concrete.websocket.*;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.concurrent.components.PriorityRunnable;
import org.coodex.pojomocker.MockerFacade;
import org.coodex.util.Common;
import org.coodex.util.GenericType;
import org.coodex.util.ReflectHelper;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteContext.runWithContext;
import static org.coodex.concrete.common.ConcreteHelper.*;
import static org.coodex.concrete.support.websocket.CallerHackConfigurator.WEB_SOCKET_CALLER_INFO;
import static org.coodex.concrete.websocket.Constants.*;

class WebSocketServerHandle implements ConcreteWebSocketEndPoint {

    private final static ScheduledExecutorService scheduledExecutorService = ExecutorsHelper.newScheduledThreadPool(1);

    private final static Logger log = LoggerFactory.getLogger(WebSocketServerHandle.class);

    private final static Map<Session, String> peers = Collections.synchronizedMap(new HashMap<Session, String>());

    private final Map<String, WebSocketUnit> unitMap = new HashMap<String, WebSocketUnit>();
    private ThreadLocal<Class> context = new ThreadLocal<>();

//    @Deprecated
//    public WebSocketServerHandle(String endPoint) {
//        registerPackage(ErrorCodes.class.getPackage().getName());
//    }

    public WebSocketServerHandle() {
        registerPackage(ErrorCodes.class.getPackage().getName());
    }

    private static void $sendText(final String text, final Session session, AtomicInteger retry) {
        final AtomicInteger toRetry = retry == null ? new AtomicInteger(0) : retry;
        if (toRetry.get() >= 5) {
            log.warn("send text failed after retry 5 times. sessionId: {}, text: {}", session.getId(), text);
            return;
        }
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(text);
            }
        } catch (IllegalStateException | IOException e) {
            log.warn("send text failed. session: {}", session.getId(), e);
            scheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    toRetry.incrementAndGet();
                    $sendText(text, session, toRetry);
                }
            }, 20, TimeUnit.MILLISECONDS);
        }

    }

    static <T> void sendMessage(Message<T> message, String tokenId) {
        for (Session session : peers.keySet()) {
            if (tokenId.equals(peers.get(session))) {
                $sendText(JSONSerializerFactory.getInstance().toJson(buildPackage(message)),
                        session, null);
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ResponsePackage<T> buildPackage(Message<T> message) {
        ResponsePackage responsePackage = new ResponsePackage();
        Map<String, String> subjoin = new HashMap<>();
        subjoin.put(BROADCAST, "true");
        subjoin.put(HOST_ID, message.getHost());
        subjoin.put(SUBJECT, message.getSubject());
        responsePackage.setSubjoin(subjoin);
        responsePackage.setContent(message.getBody());
        responsePackage.setMsgId(message.getId());
        return responsePackage;
    }

    @Override
    public void onOpen(Session peer) {
        if (!peers.containsKey(peer)) {
            peers.put(peer, Common.getUUIDStr()/* session id*/);
        }
        peer.setMaxIdleTimeout(0);

        log.debug("session opened: {}, concrete token id: {}, total sessions: {}", peer, peers.get(peer), peers.size());
    }

    @Override
    public void onClose(Session peer) {
//        String sessionId =
        peers.remove(peer);
//        if (sessionId != null) {
//            BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(sessionId, true).invalidate();
//            log.debug("token [{}] invalidate.", sessionId);
//        }
        log.debug("session closed: {}, total sessions: {}", peer, peers.size());
    }

//    @Override
//    @Deprecated
//    public <T> void broadcast(String subject, T content) {
//        broadcast(subject, content, null, null);
//    }

//    @Override
//    @Deprecated
//    public <T> void broadcast(String subject, T content, Map<String, String> subjoin) {
//        broadcast(subject, content, subjoin, null);
//    }

//    static void sendText(String text, String tokenId){
//
//    }

//    @Override
//    @Deprecated
//    public <T> void broadcast(String subject, T content, SessionFilter sessionFilter) {
//        broadcast(subject, content, null, sessionFilter);
//    }

//    @Override
//    @Deprecated
//    public <T> void broadcast(String subject, T content, Map<String, String> subjoin, SessionFilter sessionFilter) {
//
//        String text = JSONSerializerFactory.getInstance().toJson(buildPackage(subject, content, subjoin));
//        for (Session session : peers.keySet()) {
//            if (sessionFilter == null || sessionFilter.filter(session) != null) {
//                broadcastText(text, session);
//            }
//        }
//    }

    private void broadcastText(String text, Session session) {
        log.debug("broadcast, async send to {}:\n{}", session.getId(), text);
        $sendText(text, session);
    }

    private void $sendText(final String text, final Session session) {
        $sendText(text, session, null);
    }

    private void sendText(String text, Session session) {
        log.debug("async send to {}:\n{}", session.getId(), text);
        $sendText(text, session);
    }

    private void sendError(String msgId, ConcreteException exception, Session session) {
        ResponsePackage<ErrorInfo> responsePackage = new ResponsePackage<ErrorInfo>();
        responsePackage.setOk(false);
        responsePackage.setMsgId(msgId);
        responsePackage.setContent(new ErrorInfo(exception));
        sendText(JSONSerializerFactory.getInstance().toJson(responsePackage), session);
    }

    @SuppressWarnings("unchecked")
    private <T> ResponsePackage buildPackage(String subject, T content, Map<String, String> subjoin) {

        if (subjoin == null) subjoin = new HashMap<>();
        subjoin.put(BROADCAST, "true");
        subjoin.put(HOST_ID, getHostId());
        subjoin.put(SUBJECT, subject);
        ResponsePackage responsePackage = new ResponsePackage();
        responsePackage.setSubjoin(new HashMap<String, String>(subjoin));
        if (content != null) {
            responsePackage.setContent(content);
        }
        return responsePackage;
    }

    public final void registerPackage(String... packages) {
        foreachClassInPackages(new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                registerClasses(serviceClass);
            }
        }, packages);
//        if (packages == null || packages.length == 0) {
//            packages = ConcreteHelper.getApiPackages();
//        }
//        List<WebSocketModule> modules = ConcreteHelper.loadModules(WebSocketModuleMaker.WEB_SOCKET_SUPPORT, packages);
//        for (WebSocketModule module : modules) {
//            appendUnits(module);
//        }
    }

    @SuppressWarnings("unchecked")
    public final void registerClasses(Class<?>... classes) {
        for (final Class<?> clz : classes) {
            if (AbstractErrorCodes.class.isAssignableFrom(clz)) {
                ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) clz);
            } else if (ConcreteHelper.isConcreteService(clz)) {
                appendUnits(new WebSocketModule(clz));
            } else {
                throw new RuntimeException("cannot register class:" + clz.getName());
            }
        }
    }

//    public final void registerService(Class<? extends ConcreteService>... serviceClasses) {
//        for (Class<? extends ConcreteService> clz : serviceClasses) {
//            if (ConcreteHelper.isConcreteService(clz)) {
//                appendUnits(new WebSocketModule(clz));
//            }
//        }
//    }

    private void appendUnits(WebSocketModule module) {
        for (WebSocketUnit unit : module.getUnits()) {
            unitMap.put(unit.getKey(), unit);
        }
    }

//    @Override
//    public Token getToken(Session session) {
//        return BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(peers.get(session), true);
//    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {

        log.debug("message from {}:\n {}", session.getId(), message);
        // 1、解析
        RequestPackage<Object> requestPackage = analysisRequest(message, session);
        if (requestPackage == null) return;

        // 2、调用服务
        try {
            invokeService(requestPackage, session);
        } catch (Throwable th) {
            sendError(requestPackage.getMsgId(), ConcreteHelper.getException(th), session);
        }
    }

//    private synchronized Token getToken(Session session, RequestPackage requestPackage) {
//
//        Token token = BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(peers.get(session));
//        peers.put(session, token == null ? null : token.getTokenId());
//        return token;
//    }

    /**
     * serviceId定义：className#method.paramCount
     *
     * @param requestPackage
     * @param session
     */
    @SuppressWarnings("unchecked")
    private void invokeService(final RequestPackage<Object> requestPackage, final Session session) {

        //1 找到方法
        final WebSocketUnit unit = IF.isNull(unitMap.get(requestPackage.getServiceId()),
                WebSocketErrorCodes.SERVICE_ID_NOT_EXISTS, requestPackage.getServiceId());

        //2 解析数据
        final Object[] objects = analysisParameters(
                JSONSerializerFactory.getInstance().toJson(requestPackage.getContent()), unit);

        //3 调用并返回结果
        final String tokenId = requestPackage.getConcreteTokenId();
//        Token t = getToken(session, requestPackage);

//        final boolean isNew = t == null;

//        final Token token = t == null ?
//                BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(Common.getUUIDStr(), true)
//                : t;//getToken(session);
//        peers.put(session, token.getTokenId());
//        final String
//        session.getUserProperties()
        ConcreteHelper.getExecutor().execute(new PriorityRunnable(ConcreteHelper.getPriority(unit), new Runnable() {
            private Method method = unit.getMethod();

            @Override
            public void run() {
                WebSocketServiceContext context = new WebSocketServiceContext(
                        tokenId, getSubjoin(requestPackage.getSubjoin()),
                        (Caller) session.getUserProperties().get(WEB_SOCKET_CALLER_INFO)
                        , null /* TODO 从subjoin或者session里获取Locale */);
                Trace trace = APM.build(context.getSubjoin())
                        .tag("remote", context.getCaller().getAddress())
                        .tag("agent", context.getCaller().getClientProvider())
                        .start(String.format("websocket: %s.%s", method.getDeclaringClass().getName(), method.getName()));
                try {

                    Object result = runWithContext(
                            context,
                            new CallableClosure() {

                                public Object call() throws Throwable {
                                    if (isDevModel("websocket")) {
                                        return void.class.equals(unit.getGenericReturnType()) ?
                                                null :
                                                MockerFacade.mock(unit.getMethod(), unit.getDeclaringModule().getInterfaceClass());
                                    } else {
                                        Object instance = BeanProviderFacade.getBeanProvider().getBean(unit.getDeclaringModule().getInterfaceClass());
                                        if (objects == null)
                                            return method.invoke(instance);
                                        else
                                            return method.invoke(instance, objects);
                                    }

                                }
                            });

                    ResponsePackage responsePackage = new ResponsePackage();
                    String tokenIdAfterInvoke = context.getTokenId();
                    if (!Common.isSameStr(tokenId, tokenIdAfterInvoke)
                            && !Common.isBlank(tokenIdAfterInvoke)) {

                    }
//                    if (isNew)
//                        responsePackage.setConcreteTokenId(token.getTokenId());
                    responsePackage.setSubjoin(updatedMap(context.getSubjoin()));
                    responsePackage.setMsgId(requestPackage.getMsgId());
                    responsePackage.setOk(true);
                    responsePackage.setContent(result);
                    sendText(JSONSerializerFactory.getInstance().toJson(responsePackage), session);
                } catch (final Throwable th) {
                    trace.error(th);
                    runWithContext(context, new CallableClosure() {
                        @Override
                        public Object call() throws Throwable {
                            sendError(requestPackage.getMsgId(), ConcreteHelper.getException(th), session);
                            return null;
                        }
                    });

                } finally {
                    trace.finish();
                }

            }

            private Subjoin getSubjoin(Map<String, String> map) {
                return new WebSocketSubjoin(map);
            }
        }));
    }

    private Type paramType(AbstractParam param) {
        return TypeHelper.isPrimitive(param.getType()) ? param.getType() :
                TypeHelper.toTypeReference(param.getGenericType(), context.get());
    }

    private Object[] analysisParameters(String content, WebSocketUnit unit) {
//        if (content == null) return null;
        AbstractParam[] abstractParams = unit.getParameters();
        if (abstractParams.length == 0) return null;

//        Class<?> context = unit.getDeclaringModule().getInterfaceClass();
        context.set(unit.getDeclaringModule().getInterfaceClass());
        try {
            JSONSerializer serializer = JSONSerializerFactory.getInstance();

            List<Object> objects = new ArrayList<Object>();
            if (abstractParams.length == 1) {
                objects.add(content == null ? null :
                        serializer.parse(content, paramType(abstractParams[0])));
            } else {
                Map<String, String> map = serializer.parse(
                        content,
                        new GenericType<Map<String, String>>() {
                        }.genericType());

                for (AbstractParam param : abstractParams) {
                    String value = map.get(param.getName());
                    objects.add(value == null ? null :
                            serializer.parse(value, paramType(param))
                    );
                }
            }
            return objects.toArray();
        } finally {
            context.remove();
        }
    }


    private RequestPackage<Object> analysisRequest(String message, Session session) {
        try {
            return JSONSerializerFactory.getInstance()
                    .parse(message, new GenericType<RequestPackage<Object>>() {
                    }.genericType());

        } catch (Throwable throwable) {
            broadcastText(JSONSerializerFactory.getInstance().toJson(
                    buildPackage(Subjects.INVALID_REQUEST,
                            new InvalidRequest(ConcreteHelper.getException(throwable), message),
                            null)
            ), session);
            return null;
        }
    }

    String getHostId() {
        return ConcreteHelper.getProfile().getString("websocket.hostId", Common.getUUIDStr());
    }


}
