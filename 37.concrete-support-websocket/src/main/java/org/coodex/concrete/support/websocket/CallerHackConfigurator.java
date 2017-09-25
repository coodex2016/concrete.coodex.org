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

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

public class CallerHackConfigurator extends ServerEndpointConfig.Configurator {

    public static final String WEB_SOCKET_CALLER_INFO =
            CallerHackConfigurator.class.getPackage().getName()
                    + ".WEB_SOCKET_CALLER_INFO";

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, final HandshakeRequest request, HandshakeResponse response) {
        super.modifyHandshake(sec, request, response);

        Object httpSession = request.getHttpSession();
        if (httpSession instanceof HttpSession) {
            HttpSession session = (HttpSession) httpSession;
            Object caller = session.getAttribute(WEB_SOCKET_CALLER_INFO);
            sec.getUserProperties().put(WEB_SOCKET_CALLER_INFO, caller);
        }

    }
}
