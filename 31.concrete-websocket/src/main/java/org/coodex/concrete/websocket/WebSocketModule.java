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

import org.coodex.concrete.common.struct.AbstractModule;

import java.lang.reflect.Method;
import java.util.List;

public class WebSocketModule extends AbstractModule<WebSocketUnit> {

//    private static Map<String,>
//    public static WebSocketModule buildModule(Class<?> interfaceClass){
//        WebSocketModule module = new WebSocketModule(interfaceClass);
//
//        return module;
//    }

    public WebSocketModule(Class<?> interfaceClass) {
        super(interfaceClass);
    }

    @Override
    public String getName() {
        return getInterfaceClass().getName();
    }

    @Override
    protected WebSocketUnit[] toArrays(List<WebSocketUnit> webSocketUnits) {
        return webSocketUnits.toArray(new WebSocketUnit[0]);
    }

    @Override
    protected WebSocketUnit buildUnit(Method method) {
        return new WebSocketUnit(method,this);
    }

    @Override
    public int compareTo(AbstractModule o) {
        return getName().compareTo(o.getName());
    }
}
