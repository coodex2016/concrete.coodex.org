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

import org.coodex.concrete.common.Assert;
import org.coodex.concrete.websocket.WebSocket;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

public abstract class ConcreteWebSocketEndPoint/* implements ConcreteWebSocketEndPoint */ {

    private WebSocketServerHandle handle;

    public ConcreteWebSocketEndPoint() {
        ServerEndpoint serverEndpoint = Assert.isNull(getClass().getAnnotation(ServerEndpoint.class), "use ServerEndpoint plz.");
        String endPoint = serverEndpoint.value();
        synchronized (ConcreteWebSocketEndPoint.class) {
            try {
                handle = (WebSocketServerHandle) WebSocket.getEndPoint(endPoint);
            } catch (Throwable th) {
                handle = new WebSocketServerHandle(endPoint);
            }
        }
    }

    @OnOpen
    public void onOpen(Session peer) {
        handle.onOpen(peer);
    }

    @OnClose
    public void onClose(Session peer) {
        handle.onClose(peer);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        handle.onMessage(message, session);
    }

    public final void registerService(Class<?>... serviceClasses) {
        handle.registerService(serviceClasses);
    }

    public final void registerPackage(String... packages) {
        handle.registerPackage(packages);
    }
}
