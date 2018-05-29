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

import org.coodex.concrete.common.Caller;
import org.coodex.concrete.common.ServerSideContext;
import org.coodex.concrete.common.Subjoin;

import java.util.Locale;

public class WebSocketServiceContext extends ServerSideContext {

//    private static final Courier webSocketCourier = new WebSocketCourier();

    private static Caller CALLER = new Caller() {
        @Override
        public String getAddress() {
            return "CONCRETE-TODO: JSR 356 do not support, use hack";
        }

        @Override
        public String getClientProvider() {
            return "CONCRETE-TODO: JSR 356 do not support, use hack";
        }
    };

    public WebSocketServiceContext(String tokenId, Subjoin subjoin, Caller caller, Locale locale) {
        super(caller == null ? CALLER : caller, subjoin, locale, tokenId);

//        this.token = token;
//        this.subjoin = subjoin;
//        this.currentUnit = unit;
//        this.model = WEB_SOCKET_MODEL;
//        this.caller = caller == null ? CALLER : caller;
//        this.side = SIDE_SERVER;
//        this.courier = webSocketCourier;
    }

//    public WebSocketServiceContext(Token token, Subjoin subjoin, AbstractUnit unit) {
//        this(token, subjoin, unit, CALLER);
//    }
}
