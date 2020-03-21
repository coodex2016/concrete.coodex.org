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

import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.impl.OwnRXMessageListener;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.JSONSerializer;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.own.RequestPackage;
import org.coodex.util.Clock;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

//import org.coodex.util.GenericType;

@ClientEndpoint(configurator = SetUserAgentConfigurator.class)
public class WSClientHandle {

    private final static Logger log = LoggerFactory.getLogger(WSClientHandle.class);
    private Map<Destination, Session> sessionMap = new HashMap<>();
    private JSONSerializer serializer = JSONSerializerFactory.getInstance();
    private SingletonMap<WebsocketDestination, Object> locks = SingletonMap.<WebsocketDestination, Object>builder()
            .function(key -> new Object()).build();


    WSClientHandle() {
    }

    private Session getSession(WebsocketDestination destination) throws URISyntaxException, IOException, DeploymentException, InterruptedException {

        Session session = sessionMap.get(destination);
        if (session == null || !session.isOpen()) {
            Object lock = locks.get(destination);
            synchronized (lock) {
                session = sessionMap.get(destination);
                if (session == null || !session.isOpen()) {
                    // build session
                    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                    if (destination.isSsl()) {
                        // TODO how to support wss used self-signed ?
                    }
                    session = container.connectToServer(this, new URI(destination.getLocation()));
                    session.setMaxIdleTimeout(0);
                    sessionMap.put(destination, session);

                    int maxRetryTimes = 10, retried = 0;
                    while (!session.isOpen() && retried++ < maxRetryTimes) {
                        Clock.sleep(100);
                    }
                    if (!session.isOpen()) {
                        sessionMap.remove(destination);
                        throw new IOException("Cannot open websocket session: " + destination.getLocation());
                    }
                    log.debug("session opened. " + session.getId() + ". " + destination);
                }
            }
        }
        return session;
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        OwnRXMessageListener.getInstance().onMessage(message);
//        // parse to ResponsePackage
//        ResponsePackage<Object> responsePackage = serializer.parse(message,
//                new GenericTypeHelper.GenericType<ResponsePackage<Object>>() {
//                }.getType());
//        // broadcast ?
////        if(responsePackage.)
//
//        WSCallback callback = callbackMap.getAndRemove(responsePackage.getMsgId());
//        if (callback != null) {
//            // isOK
//            if (responsePackage.isOk()) {
//                callback.onReturn(responsePackage);
//            } else {
//                callback.onError(new WSClientException(
//                        serializer.<ErrorInfo>parse(responsePackage.getContent(), ErrorInfo.class)));
//            }
//
//        }
    }

    void send(final WebsocketDestination destination, final RequestPackage requestPackage) {
        try {
            Session session = getSession(destination);
            synchronized (session) {
//                callbackMap.put(requestPackage.getMsgId(), callback, new TimeLimitedMap.TimeoutCallback() {
//                    @Override
//                    public void timeout() {
//                        callback.onError(new TimeoutException("request timeout: "
//                                + requestPackage.getServiceId() + " "
//                                + destination.toString()));
//                    }
//                });

//                try {
                session.getBasicRemote().sendText(serializer.toJson(requestPackage));
//                } catch (Throwable th) {
//                    if (callbackMap.getAndRemove(requestPackage.getMsgId()) != null)
//                        callback.onError(ConcreteHelper.getException(th));
//                }
            }
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

//    public interface WSCallback {
//        void onReturn(ResponsePackage<Object> responsePackage);
//
//        void onError(Throwable th);
//    }
}
