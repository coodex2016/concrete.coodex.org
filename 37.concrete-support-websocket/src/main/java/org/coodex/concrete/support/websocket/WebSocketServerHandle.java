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

package org.coodex.concrete.support.websocket;

import org.coodex.concrete.common.*;
import org.coodex.concrete.common.struct.AbstractParam;
import org.coodex.concrete.core.token.TokenManager;
import org.coodex.concrete.websocket.ConcreteWebSocketEndPoint;
import org.coodex.concrete.websocket.*;
import org.coodex.concurrent.components.PriorityRunnable;
import org.coodex.util.Common;
import org.coodex.util.GenericType;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import static org.coodex.concrete.common.ConcreteContext.CURRENT_UNIT;
import static org.coodex.concrete.common.ConcreteContext.runWith;
import static org.coodex.concrete.websocket.Constants.*;

class WebSocketServerHandle extends WebSocket implements ConcreteWebSocketEndPoint {

    private final static Logger log = LoggerFactory.getLogger(WebSocketServerHandle.class);

    private final Map<Session, String> peers = Collections.synchronizedMap(new HashMap<Session, String>());

    private final Map<String, WebSocketUnit> unitMap = new HashMap<String, WebSocketUnit>();

    public WebSocketServerHandle(String endPoint) {
        registerEndPoint(endPoint, this);
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
        String sessionId = peers.remove(peer);
        if (sessionId != null) {
            BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(sessionId, true).invalidate();
            log.debug("token [{}] invalidate.", sessionId);
        }
        log.debug("session closed: {}, total sessions: {}", peer, peers.size());
    }

    @Override
    public <T> void broadcast(String subject, T content) {
        broadcast(subject, content, null, null);
    }

    @Override
    public <T> void broadcast(String subject, T content, Map<String, String> subjoin) {
        broadcast(subject, content, subjoin, null);
    }

    @Override
    public <T> void broadcast(String subject, T content, SessionFilter sessionFilter) {
        broadcast(subject, content, null, sessionFilter);
    }

    @Override
    public <T> void broadcast(String subject, T content, Map<String, String> subjoin, SessionFilter sessionFilter) {

        String text = JSONSerializerFactory.getInstance().toJson(buildPackage(subject, content, subjoin));
        for (Session session : peers.keySet()) {
            if (sessionFilter == null || sessionFilter.filter(session) != null) {
                broadcastText(text, session);
            }
        }
    }

    private void broadcastText(String text, Session session) {
        log.debug("broadcast, async send to {}:\n{}", session.getId(), text);
        session.getAsyncRemote().sendText(text);
    }

    private void sendText(String text, Session session) {
        log.debug("async send to {}:\n{}", session.getId(), text);
        session.getAsyncRemote().sendText(text);
    }

    private void sendError(String msgId, ConcreteException exception, Session session) {
        ResponsePackage<ErrorInfo> responsePackage = new ResponsePackage<ErrorInfo>();
        responsePackage.setOk(false);
        responsePackage.setMsgId(msgId);
        responsePackage.setContent(new ErrorInfo(exception));
        sendText(JSONSerializerFactory.getInstance().toJson(responsePackage), session);
    }

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
        if (packages == null || packages.length == 0) {
            packages = ConcreteHelper.getApiPackages();
        }
        List<WebSocketModule> modules = ConcreteHelper.loadModules(WebSocketModuleMaker.WEB_SOCKET_SUPPORT, packages);
        for (WebSocketModule module : modules) {
            appendUnits(module);
        }
    }

    public final void registerService(Class<?>... serviceClasses) {
        for (Class<?> clz : serviceClasses) {
            if (ConcreteHelper.isConcreteService(clz)) {
                appendUnits(new WebSocketModule(clz));
            }
        }
    }

    private void appendUnits(WebSocketModule module) {
        for (WebSocketUnit unit : module.getUnits()) {
            unitMap.put(unit.getKey(), unit);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {

        log.debug("message from {}:\n {}", session.getId(), message);
        // 1、解析
        RequestPackage<String> requestPackage = analysisRequest(message, session);
        if (requestPackage == null) return;

        // 2、调用服务
        try {
            invokeService(requestPackage, session);
        } catch (Throwable th) {
            sendError(requestPackage.getMsgId(), ConcreteHelper.getException(th), session);
        }
    }

    @Override
    public Token getToken(Session session) {
        return BeanProviderFacade.getBeanProvider().getBean(TokenManager.class).getToken(peers.get(session), true);
    }


    /**
     * serviceId定义：className#method.paramCount
     *
     * @param requestPackage
     * @param session
     */
    private void invokeService(final RequestPackage<String> requestPackage, final Session session) {
        //1 找到方法
        final WebSocketUnit unit = Assert.isNull(unitMap.get(requestPackage.getServiceId()),
                WebSocketErrorCodes.SERVICE_ID_NOT_EXISTS, requestPackage.getServiceId());

        //2 解析数据
        final Object[] objects = analysisParameters(requestPackage.getContent(), unit);

        //3 调用并返回结果
        final Token token = getToken(session);

        ConcreteHelper.getExecutor().execute(new PriorityRunnable(ConcreteHelper.getPriority(unit), new Runnable() {
            private Method method = unit.getMethod();

            @Override
            public void run() {

                try {
                    Object result = runWith(Constants.WEB_SOCKET_MODEL, getSubjoin(requestPackage.getSubjoin()), token,
                            ConcreteContext.run(
                                    CURRENT_UNIT, unit, new ConcreteClosure() {

                                        public Object concreteRun() throws Throwable {
                                            Object instance = BeanProviderFacade.getBeanProvider().getBean(unit.getDeclaringModule().getInterfaceClass());
                                            if (objects == null)
                                                return method.invoke(instance);
                                            else
                                                return method.invoke(instance, objects);

                                        }
                                    }
                            ));
                    ResponsePackage responsePackage = new ResponsePackage();
                    responsePackage.setMsgId(requestPackage.getMsgId());
                    responsePackage.setOk(true);
                    responsePackage.setContent(result);
                    sendText(JSONSerializerFactory.getInstance().toJson(responsePackage), session);
                } catch (Throwable th) {
                    sendError(requestPackage.getMsgId(), ConcreteHelper.getException(th), session);
                }

            }

            private Subjoin getSubjoin(Map<String, String> map) {
                return new WebSocketSubjoin(map);
            }
        }));
    }


    private ThreadLocal<Class> context = new ThreadLocal<>();

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


    private RequestPackage<String> analysisRequest(String message, Session session) {
        try {
            return JSONSerializerFactory.getInstance()
                    .parse(message, new GenericType<RequestPackage<String>>() {
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

    public String getHostId() {
        return ConcreteHelper.getProfile().getString("websocket.hostId", Common.getUUIDStr());
    }


}
