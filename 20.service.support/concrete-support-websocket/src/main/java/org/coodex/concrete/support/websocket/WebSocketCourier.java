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

import org.coodex.concrete.common.Token;
import org.coodex.concrete.common.messages.Message;
import org.coodex.concrete.core.messages.Courier;
import org.coodex.concrete.message.ServerSideMessage;

@Deprecated
public class WebSocketCourier implements Courier {
    @Override
    public String getType() {
        return "WEB_SOCKET_COURIER";
    }

    @Override
    public <T> void pushTo(final Message<T> message, Token token) {
        WebSocketServerHandle.sendMessage(new ServerSideMessage<T>() {
            @Override
            public String getSubject() {
                return message.getSubject();
            }

            @Override
            public String getId() {
                return message.getId();
            }

            @Override
            public String getHost() {
                return message.getHost();
            }

            @Override
            public T getBody() {
                return message.getBody();
            }
        }, token.getTokenId());
    }
}
