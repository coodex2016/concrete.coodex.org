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

import org.coodex.concrete.common.Assert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WebSocket {


    private final static Map<String, ConcreteWebSocketEndPoint> endPointMap = new HashMap<>();

    public static ConcreteWebSocketEndPoint getEndPoint(String endPoint) {
        return Assert.isNull(endPointMap.get(endPoint), "Web socket endpoint not found: " + endPoint);
    }

    protected static void registerEndPoint(String endPoint, ConcreteWebSocketEndPoint webSocketEndPoint) {
        endPointMap.put(endPoint, webSocketEndPoint);
    }

    private final static Set<BroadcastListener> listeners = new HashSet<>();

    public static void addBroadcastListener(BroadcastListener listener) {
        if (listener != null)
            listeners.add(listener);
    }

    protected static Set<BroadcastListener> getListeners(){
        return listeners;
    }
}
