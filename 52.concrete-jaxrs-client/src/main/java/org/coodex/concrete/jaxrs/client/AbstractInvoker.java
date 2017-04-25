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

package org.coodex.concrete.jaxrs.client;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.ConcreteClosure;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.core.intercept.ConcreteInterceptor;
import org.coodex.concrete.core.intercept.InterceptorChain;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.SPIFacade;

import static org.coodex.concrete.common.SubjoinWrapper.DEFAULT_SUBJOIN;

/**
 * Created by davidoff shen on 2017-03-09.
 */
public abstract class AbstractInvoker implements Invoker {

    protected abstract MethodInvocation getInvocation(Unit unit, Object[] args, Object instance);

    @Override
    public final Object invoke(final Unit unit, final Object[] args, final Object instance) throws Throwable {
        return ConcreteContext.run(
                ConcreteContext.SUBJOIN, DEFAULT_SUBJOIN,
                ConcreteContext.run(ConcreteContext.SIDE, ConcreteContext.SIDE_CLIENT, new ConcreteClosure() {
            @Override
            public Object concreteRun() throws Throwable {
                return getInterceptorChain().invoke(getInvocation(unit, args, instance));
            }
        })).run();
    }


    private static InterceptorChain interceptors;

    private static synchronized InterceptorChain getInterceptorChain() {
        if (interceptors == null) {
            SPIFacade<ConcreteInterceptor> spiFacade = new SPIFacade<ConcreteInterceptor>() {
            };
            interceptors = new InterceptorChain();
            for (ConcreteInterceptor interceptor : spiFacade.getAllInstances()) {
                interceptors.add(interceptor);
            }
        }
        return interceptors;
    }


}
