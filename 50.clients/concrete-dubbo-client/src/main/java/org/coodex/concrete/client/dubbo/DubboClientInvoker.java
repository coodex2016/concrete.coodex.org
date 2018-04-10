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

package org.coodex.concrete.client.dubbo;

import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.impl.AbstractSyncInvoker;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.ServiceContext;

import java.lang.reflect.Method;

public class DubboClientInvoker extends AbstractSyncInvoker {



    public DubboClientInvoker(Destination destination) {
        super(destination);
    }

    @Override
    protected Object execute(Class clz, Method method, Object[] args) throws Throwable {
        return null;
    }

    @Override
    public ServiceContext buildContext(Class concreteClass, Method method) {
        return new DubboClientContext(getDestination(), RuntimeContext.getRuntimeContext(method, concreteClass));
    }
}
