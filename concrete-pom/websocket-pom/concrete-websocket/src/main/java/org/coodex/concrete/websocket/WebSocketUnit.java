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

import org.coodex.concrete.own.OwnServiceUnit;

import java.lang.reflect.Method;

public class WebSocketUnit extends OwnServiceUnit<WebSocketModule> /*extends AbstractUnit<AbstractParam, WebSocketModule> */ {
    public WebSocketUnit(Method method, WebSocketModule module) {
        super(method, module);
    }


    //    private String key = null;
//
//    private Class paramType = null;
//
//    public WebSocketUnit(Method method, WebSocketModule module) {
//        super(method, module);
//    }
//
//    @Override
//    public String getName() {
//        return getMethod().getName();
//    }
//
//    public synchronized String getKey() {
//        if (key == null) {
//            key = Common.sha1(String.format("%s:%s(%d)",
//                    getDeclaringModule().getInterfaceClass().getName(),
//                    getName(),
//                    getParameters().length));
//        }
//        return key;
//    }
//
//    @Override
//    protected void afterInit() {
////        super.afterInit();
//    }
//
//    @Override
//    protected AbstractParam buildParam(Method method, int index) {
//        return new AbstractParam(method, index) {
//        };
//    }
//
    @Override
    public String getInvokeType() {
        return "WebSocket";
    }
//
//    @Override
//    protected AbstractParam[] toArrays(List<AbstractParam> abstractParams) {
//        return abstractParams.toArray(new AbstractParam[0]);
//    }
//
//    @Override
//    protected DefinitionContext toContext() {
//        return ConcreteHelper.getContext(getMethod(), getDeclaringModule().getInterfaceClass());
//    }
//
//    @Override
//    public int compareTo(AbstractUnit o) {
//        return getName().compareTo(o.getName());
//    }
}
