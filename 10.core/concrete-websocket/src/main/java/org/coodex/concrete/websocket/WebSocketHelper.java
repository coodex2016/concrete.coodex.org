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

import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.struct.AbstractParam;
import org.coodex.util.Common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class WebSocketHelper {
    private static String keyBase(Class serviceClass, Method method) {
        return String.format("%s:%s(%d)",
                serviceClass.getName(),
                method.getName(),
                method.getParameterTypes().length);
    }

    public static String buildKey(Class serviceClass, Method method) {
        return Common.sha1(keyBase(serviceClass, method));
    }

    private static String buildKey(RuntimeContext runtimeContext) {
        return buildKey(runtimeContext.getDeclaringClass(), runtimeContext.getDeclaringMethod());
    }


    public static RequestPackage buildRequest(String msgId, WebSocketUnit unit, Object[] args) {
        RequestPackage requestPackage = new RequestPackage();
        requestPackage.setMsgId(msgId);
        requestPackage.setServiceId(unit.getKey());

        AbstractParam[] parameters = unit.getParameters();
        switch (parameters.length) {
            case 0:
                break;
            case 1:
                requestPackage.setContent(args[0]);
                break;
            default:
                Map<String, Object> toSend = new HashMap<String, Object>();
                for (int i = 0; i < parameters.length; i++) {
                    toSend.put(parameters[i].getName(), args[i]);
                }
                requestPackage.setContent(toSend);
                break;
        }
        return requestPackage;
    }


}
