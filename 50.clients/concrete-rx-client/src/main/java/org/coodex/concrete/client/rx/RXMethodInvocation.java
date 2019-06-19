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

package org.coodex.concrete.client.rx;

import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.core.intercept.ConcreteMethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RXMethodInvocation implements ConcreteMethodInvocation {

    private final DefinitionContext runtimeContext;
    private final Object[] arguments;
    private final Object instance;

    public RXMethodInvocation(DefinitionContext runtimeContext, Object[] arguments) throws InvocationTargetException, IllegalAccessException {
        this.runtimeContext = runtimeContext;
        this.arguments = arguments;
        this.instance = AbstractRxInvoker.buildSyncInstance(runtimeContext.getDeclaringClass());
    }

    @Override
    public Method getMethod() {
        return runtimeContext.getDeclaringMethod();
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() throws Throwable {
        throw new RuntimeException("Rx invocation can not Proceed.");
    }

    @Override
    public Object getThis() {
        return instance;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return null;
    }

    @Override
    public Class<?> getInterfaceClass() {
        return runtimeContext.getDeclaringClass();
    }
}
