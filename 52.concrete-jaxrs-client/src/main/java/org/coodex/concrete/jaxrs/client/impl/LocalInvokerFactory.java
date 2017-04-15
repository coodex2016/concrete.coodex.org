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

package org.coodex.concrete.jaxrs.client.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.BeanProviderFacade;
import org.coodex.concrete.jaxrs.client.AbstractInvoker;
import org.coodex.concrete.jaxrs.client.ClientMethodInvocation;
import org.coodex.concrete.jaxrs.client.Invoker;
import org.coodex.concrete.jaxrs.client.InvokerFactory;
import org.coodex.concrete.jaxrs.struct.Unit;

/**
 * Created by davidoff shen on 2016-12-13.
 */
public class LocalInvokerFactory implements InvokerFactory {


    static class LocalInvoker extends AbstractInvoker {
        @Override
        protected MethodInvocation getInvocation(final Unit unit, final Object[] args, final Object instance) {
            return new ClientMethodInvocation(instance, unit, args) {
                @Override
                public Object proceed() throws Throwable {
                    return unit.getMethod().invoke(
                            BeanProviderFacade.getBeanProvider().getBean(
                                    unit.getDeclaringModule().getInterfaceClass()),
                            args);
                }
            };
        }
    }


    @Override
    public boolean accept(String domain) {
        return false;
    }

    @Override
    public Invoker getInvoker(String domain) {
        return new LocalInvoker();
    }
}
