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

import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;
import org.coodex.concrete.client.ClientMethodInvocation;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.mock.Mocker;
import org.coodex.util.Common;

import java.lang.reflect.Method;


public abstract class AbstractSyncInvoker extends AbstractInvoker {

    public AbstractSyncInvoker(Destination destination) {
        super(destination);
    }

    protected abstract Object execute(Class clz, Method method, Object[] args) throws Throwable;

    @Override
    public Object invoke(final Object instance, final Class clz, final Method method, final Object... args) {
        ServiceContext serviceContext = buildContext(ConcreteHelper.getDefinitionContext(clz, method));
        Trace trace = APM.build().tag("interface", clz.getName())
                .tag("method", method.getName())
                .start(String.format("client invoke: %s", getDestination().getLocation()));
        trace.hack(serviceContext.getSubjoin());
        try {
            return ConcreteContext.runWithContext(serviceContext, () -> ClientHelper.getSyncInterceptorChain().invoke(new ClientMethodInvocation(instance, clz, method, args) {
                @Override
                public Object proceed() throws Throwable {
                    if (isMock()) {
                        return Mocker.mockMethod(method, clz);
                    } else {
                        return execute(clz, method, args);
                    }
                }
            }));
        } catch (Throwable throwable) {
            trace.error(throwable);
            throw Common.runtimeException(throwable);
        } finally {
            trace.finish();
        }
//        return ConcreteContext.runWithContext(buildContext(clz, method), new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                return ClientHelper.getSyncInterceptorChain().invoke(new ClientMethodInvocation(instance, clz, method, args) {
//                    @Override
//                    public Object proceed() throws Throwable {
//                        return execute(clz, method, args);
//                    }
//                });
//            }
//        });
    }
}
