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

package org.coodex.concrete.websocket;

import org.coodex.concrete.common.Token;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;

public interface ConcreteWebSocketEndPoint {

    @OnOpen
    void onOpen(Session peer);

    @OnClose
    void onClose(Session peer);

    @OnMessage
    void onMessage(String message, Session session) throws IOException;

//    @Deprecated
//    Token getToken(Session session);


    <T> void broadcast(String subject, T content);

    <T> void broadcast(String subject, T content, Map<String, String> subjoin);

    <T> void broadcast(String subject, T content, SessionFilter sessionFilter);

    <T> void broadcast(String subject, T content, Map<String, String> subjoin, SessionFilter sessionFilter);

}
