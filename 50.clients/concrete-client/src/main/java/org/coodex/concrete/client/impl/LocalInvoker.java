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

package org.coodex.concrete.client.impl;

import org.coodex.concrete.client.ClientMethodInvocation;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.Invoker;
import org.coodex.concrete.common.*;

import java.lang.reflect.Method;

import static org.coodex.concrete.common.AModule.getUnit;

public class LocalInvoker extends AbstractSyncInvoker {


    public LocalInvoker(Destination destination) {
        super(destination);
    }

    @Override
    protected Object execute(Class clz, Method method, Object[] args) throws Throwable {
        return method.invoke(BeanProviderFacade.getBeanProvider().getBean(clz), args);
    }


    @Override
    public ServiceContext buildContext(Class concreteClass, Method method) {
        return new LocalServiceContext(getDestination(),
                RuntimeContext.getRuntimeContext(method, concreteClass));
    }


    /**
     * 本地调用不走客户端切片
     *
     * @param instance
     * @param clz
     * @param method
     * @param args
     * @return
     */
    @Override
    public Object invoke(final Object instance, final Class clz, final Method method, final Object... args) {

        return ConcreteContext.runWithContext(
                buildContext(clz, method),
                new ConcreteClosure() {
                    @Override
                    public Object concreteRun() throws Throwable {
                        return execute(clz, method, args);
                    }
                }
        );
    }

}
