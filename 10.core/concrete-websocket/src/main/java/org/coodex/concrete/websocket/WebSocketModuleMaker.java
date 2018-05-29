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

package org.coodex.concrete.websocket;

import org.coodex.concrete.common.ModuleMaker;

public class WebSocketModuleMaker implements ModuleMaker<WebSocketModule> {
    public final static String WEB_SOCKET_SUPPORT = "WebSocket.";

    @Override
    public boolean isAccept(String desc) {
        return accept(desc);
    }

    @Override
    public WebSocketModule make(Class<?> interfaceClass) {
        return new WebSocketModule(interfaceClass);
    }

    //    @Override
    public boolean accept(String desc) {
        return desc != null && desc.toLowerCase().startsWith(WEB_SOCKET_SUPPORT.toLowerCase());
    }
}
