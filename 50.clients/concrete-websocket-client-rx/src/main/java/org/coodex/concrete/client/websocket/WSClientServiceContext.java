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

import org.coodex.concrete.client.ClientServiceContext;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.concrete.websocket.Constants;
import org.coodex.concrete.websocket.WebSocketModule;
import org.coodex.concrete.websocket.WebSocketUnit;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WSClientServiceContext extends ClientServiceContext {

    private static Map<Class, WebSocketModule> moduleMap = new ConcurrentHashMap<Class, WebSocketModule>();

    private static WebSocketModule getModule(Class concreteClass) {
        if (!moduleMap.containsKey(concreteClass)) {
            synchronized (moduleMap) {
                if (!moduleMap.containsKey(concreteClass)) {
                    moduleMap.put(concreteClass, new WebSocketModule(concreteClass));
                }
            }
        }
        return moduleMap.get(concreteClass);
    }

    static WebSocketUnit findUnit(RuntimeContext context) {
        WebSocketModule module = Assert.isNull(getModule(context.getDeclaringClass()),
                context.getDeclaringClass() + "is not a concrete service.");
        Method method = context.getDeclaringMethod();
        for (WebSocketUnit unit : module.getUnits()) {
            if (unit.getMethod().getName().equals(method.getName()) &&
                    Arrays.equals(unit.getMethod().getParameterTypes(), method.getParameterTypes())) {
                return unit;
            }
        }
        throw new RuntimeException("unable found websocket unit: "
                + context.getDeclaringClass().getName() + " "
                + context.getDeclaringMethod().getName());
    }


    public WSClientServiceContext(Destination destination, RuntimeContext context) {
        super(destination, context);
        this.model = Constants.WEB_SOCKET_MODEL;
    }

    @Override
    protected AbstractUnit getUnit(RuntimeContext context) {
        return findUnit(context);
    }
}
