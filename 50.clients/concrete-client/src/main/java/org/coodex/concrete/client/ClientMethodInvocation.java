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

package org.coodex.concrete.client;

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public abstract class ClientMethodInvocation implements MethodInvocation {

    private final Class<?> clazz;
    private final Method method;
    private final Object[] arguments;
    private final Object instance;

    public ClientMethodInvocation(Object instance, Class<?> clazz, Method method, Object[] arguments) {
        this.clazz = clazz;
        this.method = method;
        this.arguments = arguments;
        this.instance = instance;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Object getThis() {
        return instance;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return null;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
