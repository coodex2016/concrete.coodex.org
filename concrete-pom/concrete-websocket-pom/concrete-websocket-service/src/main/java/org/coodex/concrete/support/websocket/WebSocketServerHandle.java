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

import org.coodex.concrete.common.*;
import org.coodex.concrete.message.ServerSideMessage;
import org.coodex.concrete.own.OwnServiceProvider;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.concrete.own.ResponsePackage;
import org.coodex.concrete.websocket.ConcreteWebSocketEndPoint;
import org.coodex.concrete.websocket.InvalidRequest;
import org.coodex.concrete.websocket.Subjects;
import org.coodex.concrete.websocket.WebSocketModule;
import org.coodex.config.Config;
import org.coodex.id.IDGenerator;
import org.coodex.util.GenericTypeHelper;
import org.coodex.util.JSONSerializer;
import org.coodex.util.UUIDHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.common.ConcreteHelper.getScheduler;
import static org.coodex.concrete.support.websocket.CallerHackConfigurator.WEB_SOCKET_CALLER_INFO;
import static org.coodex.concrete.websocket.Constants.*;

class WebSocketServerHandle extends OwnServiceProvider implements ConcreteWebSocketEndPoint {

//    private final static ScheduledExecutorService scheduledExecutorService = ExecutorsHelper.newScheduledThreadPool
//    (1);

    private final static Logger log = LoggerFactory.getLogger(WebSocketServerHandle.class);

    private final static Map<Session, String> peers = Collections.synchronizedMap(new HashMap<>());

    private final static OwnServiceProvider.OwnModuleBuilder OWN_MODULE_BUILDER = WebSocketModule::new;

//    @Override
//    protected Subjoin getSubjoin(RequestPackage requestPackage) {
//        return getSubjoin(requestPackage.getSubjoin());
//    }

    WebSocketServerHandle() {
        registerPackage(ErrorCodes.class.getPackage().getName());
    }


//    @Deprecated
//    public WebSocketServerHandle(String endPoint) {
//        registerPackage(ErrorCodes.class.getPackage().getName());
//    }

    private static void $sendText(final String text, final Session session, AtomicInteger retry) {
        final AtomicInteger toRetry = retry == null ? new AtomicInteger(0) : retry;
        if (toRetry.get() >= 5) {
            log.warn("send text failed after retry 5 times. sessionId: {}, text: {}", session.getId(), text);
            return;
        }
        try {
//            synchronized (session) {
            session.getBasicRemote().sendText(text);
//            }
        } catch (IllegalStateException | IOException e) {
            log.warn("send text failed. session: {}", session.getId(), e);
            getScheduler("websocket.retry").schedule(() -> {
                toRetry.incrementAndGet();
                $sendText(text, session, toRetry);
            }, 20, TimeUnit.MILLISECONDS);
        }

    }

    private static <T> void sendMessage(ServerSideMessage<T> message, String tokenId) {
        for (Session session : peers.keySet()) {
            if (tokenId.equals(peers.get(session))) {
                $sendText(JSONSerializer.getInstance().toJson(buildPackage(message)),
                        session, null);
                break;
            }
        }
    }

    private static <T> ResponsePackage<T> buildPackage(ServerSideMessage<T> message) {
        ResponsePackage<T> responsePackage = new ResponsePackage<>();
        Map<String, String> subjoin = new HashMap<>();
        subjoin.put(BROADCAST, "true");
        subjoin.put(HOST_ID, message.getHost());
        subjoin.put(SUBJECT, message.getSubject());
        responsePackage.setSubjoin(subjoin);
        responsePackage.setContent(message.getBody());
        responsePackage.setMsgId(message.getId());
        return responsePackage;
    }

    protected OwnServiceProvider.OwnModuleBuilder getModuleBuilder() {
        return OWN_MODULE_BUILDER;
    }

    @Override
    public void onOpen(Session peer) {
        if (!peers.containsKey(peer)) {
            peers.put(peer, IDGenerator.newId());
        }
        peer.setMaxIdleTimeout(0);

        log.debug("session opened: {}, concrete token id: {}, total sessions: {}", peer, peers.get(peer), peers.size());
    }

    @Override
    public void onClose(Session peer) {
//        String sessionId =
        peers.remove(peer);
//        if (sessionId != null) {
//            BeanServiceLoaderProvider.getBeanProvider().getBean(TokenManager.class).getToken(sessionId, true)
//            .invalidate();
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

    private void sendError(String msgId, Throwable exception, Session session) {
        ResponsePackage<ErrorInfo> responsePackage = new ResponsePackage<>();
        responsePackage.setOk(false);
        responsePackage.setMsgId(msgId);
        responsePackage.setContent(ThrowableMapperFacade.toErrorInfo(exception));
        sendText(JSONSerializer.getInstance().toJson(responsePackage), session);
    }

    @SuppressWarnings("SameParameterValue")
    private <T> ResponsePackage<T> buildPackage(String subject, T content, Map<String, String> subjoin) {

        if (subjoin == null) subjoin = new HashMap<>();
        subjoin.put(BROADCAST, "true");
        subjoin.put(HOST_ID, getHostId());
        subjoin.put(SUBJECT, subject);
        ResponsePackage<T> responsePackage = new ResponsePackage<>();
        responsePackage.setSubjoin(new HashMap<>(subjoin));
        if (content != null) {
            responsePackage.setContent(content);
        }
        return responsePackage;
    }

//    public final void registerService(Class<? extends ConcreteService>... serviceClasses) {
//        for (Class<? extends ConcreteService> clz : serviceClasses) {
//            if (ConcreteHelper.isConcreteService(clz)) {
//                appendUnits(new WebSocketModule(clz));
//            }
//        }
//    }


//    @Override
//    public Token getToken(Session session) {
//        return BeanServiceLoaderProvider.getBeanProvider().getBean(TokenManager.class).getToken(peers.get(session),
//        true);
//    }

    @OnMessage
    public void onMessage(String message, Session session) {

        log.debug("message from {}:\n {}", session.getId(), message);
        // 1、解析
        RequestPackage<Object> requestPackage = analysisRequest(message, session);
        if (requestPackage == null) return;

        // 2、调用服务
        try {
            invokeService(requestPackage, session);
        } catch (Throwable th) {
            sendError(requestPackage.getMsgId(), th, session);
        }
    }

//    private synchronized Token getToken(Session session, RequestPackage requestPackage) {
//
//        Token token = BeanServiceLoaderProvider.getBeanProvider().getBean(TokenManager.class).getToken(peers.get
//        (session));
//        peers.put(session, token == null ? null : token.getTokenId());
//        return token;
//    }

    /**
     * serviceId定义：className#method.paramCount
     */
    private void invokeService(final RequestPackage<Object> requestPackage, final Session session) {

        final Caller caller = (Caller) session.getUserProperties().get(WEB_SOCKET_CALLER_INFO);

        final OwnServiceProvider.JSONResponseVisitor responseVisitor = json -> sendText(json, session);

//        final OwnServiceProvider.ErrorVisitor errorVisitor = (msgId, th) -> sendError(msgId, th, session);

        final OwnServiceProvider.ServerSideMessageVisitor serverSideMessageVisitor = WebSocketServerHandle::sendMessage;

        final OwnServiceProvider.TBMNewTokenVisitor newTokenVisitor = tokenId -> {
            peers.put(session, tokenId);//???
        };

        invokeService(requestPackage, caller, responseVisitor,/* errorVisitor, */serverSideMessageVisitor,
                newTokenVisitor);
    }

    @Override
    protected ServerSideContext getServerSideContext(RequestPackage<Object> requestPackage,
                                                     String tokenId, Caller caller) {

        return new WebSocketServiceContext(
                tokenId, getSubjoin(requestPackage.getSubjoin()),
                caller, null /* TODO 从subjoin或者session里获取Locale */);
    }


    @Override
    protected String getModuleName() {
        return "websocket";
    }

//    @Override
//    protected Subjoin getSubjoin(Map<String, String> map) {
//        return new WebSocketSubjoin(map);
//    }

    private RequestPackage<Object> analysisRequest(String message, Session session) {
        try {
            return JSONSerializer.getInstance()
                    .parse(message, new GenericTypeHelper.GenericType<RequestPackage<Object>>() {
                    }.getType());

        } catch (Throwable throwable) {
            broadcastText(JSONSerializer.getInstance().toJson(
                    buildPackage(Subjects.INVALID_REQUEST,
                            new InvalidRequest(ConcreteHelper.getException(throwable), message),
                            null)
            ), session);
            return null;
        }
    }

    private String getHostId() {
        // TODO
        return Config.getValue("websocket.hostId", UUIDHelper::getUUIDString, getAppSet());
    }


    @Override
    public String getNamespace() {
        return "websocket";
    }
}
