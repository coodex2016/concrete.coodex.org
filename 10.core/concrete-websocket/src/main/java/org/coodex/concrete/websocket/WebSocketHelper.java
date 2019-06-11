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

import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.util.SingletonMap;

import java.lang.reflect.Method;
import java.util.Arrays;

public class WebSocketHelper {

    private static SingletonMap<Class, WebSocketModule> modules =
            new SingletonMap<Class, WebSocketModule>(
                    new SingletonMap.Builder<Class, WebSocketModule>() {
                        @Override
                        public WebSocketModule build(Class key) {
                            return new WebSocketModule(key);
                        }
                    }
            );


    @SuppressWarnings("unchecked")
    public static WebSocketUnit findUnit(RuntimeContext context) {
        WebSocketModule module = IF.isNull(modules.getInstance(context.getDeclaringClass()),
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


}
