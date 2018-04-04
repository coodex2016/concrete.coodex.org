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

package org.coodex.concrete.jaxrs.client.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.common.ConcreteClosure;
import org.coodex.concrete.jaxrs.client.*;
import org.coodex.concrete.jaxrs.struct.Unit;

import static org.coodex.concrete.common.ConcreteContext.SIDE_LOCAL_INVOKE;
import static org.coodex.concrete.common.ConcreteContext.runWithContext;

/**
 * Created by davidoff shen on 2016-12-13.
 */
@Deprecated
public class LocalInvokerFactory implements InvokerFactory {


    static class LocalInvoker extends AbstractInvoker {
        @Override
        protected MethodInvocation getInvocation(final Unit unit, final Object[] args, final Object instance) {
            return new ClientMethodInvocation(instance, unit, args) {
                @Override
                public Object proceed() throws Throwable {
                    return runWithContext(
                            new JaxRSClientServiceContext(unit, SIDE_LOCAL_INVOKE),
                            new ConcreteClosure() {
                                @Override
                                public Object concreteRun() throws Throwable {
                                    return unit.getMethod().invoke(
                                            BeanProviderFacade.getBeanProvider().getBean(
                                                    unit.getDeclaringModule().getInterfaceClass()),
                                            args);
                                }
                            });
//                    return ConcreteContext.run(SIDE, SIDE_LOCAL_INVOKE, new ConcreteClosure() {
//                        @Override
//                        public Object concreteRun() throws Throwable {
//                            return unit.getMethod().invoke(
//                                    BeanProviderFacade.getBeanProvider().getBean(
//                                            unit.getDeclaringModule().getInterfaceClass()),
//                                    args);
//                        }
//                    }).run();
                }
            };
        }
    }


    @Override
    public boolean accept(String domain) {
        return false;
    }

    @Override
    public Invoker getInvoker(String domain, String tokenManagerKey) {
        return new LocalInvoker();
    }
}
