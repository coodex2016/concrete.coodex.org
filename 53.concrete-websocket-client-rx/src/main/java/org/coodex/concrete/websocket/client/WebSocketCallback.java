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

package org.coodex.concrete.websocket.client;

import io.reactivex.ObservableEmitter;
import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.websocket.WebSocketUnit;

import java.util.concurrent.Future;

public class WebSocketCallback {
    private final Future future;

    private final String msgId;

    private final ObservableEmitter emitter;

    private final WebSocketUnit unit;

    private final RuntimeContext context;

    private final MethodInvocation invocation;

    public WebSocketCallback(String msgId, WebSocketUnit unit, Future future, ObservableEmitter emitter, RuntimeContext context, MethodInvocation invocation) {
        this.future = future;
        this.unit = unit;
        this.msgId = msgId;
        this.emitter = emitter;
        this.context = context;
        this.invocation = invocation;
    }

    public WebSocketUnit getUnit() {
        return unit;
    }

    public Future getFuture() {
        return future;
    }

    public String getMsgId() {
        return msgId;
    }

    public ObservableEmitter getEmitter() {
        return emitter;
    }

    public RuntimeContext getContext() {
        return context;
    }

    public MethodInvocation getInvocation() {
        return invocation;
    }
}
