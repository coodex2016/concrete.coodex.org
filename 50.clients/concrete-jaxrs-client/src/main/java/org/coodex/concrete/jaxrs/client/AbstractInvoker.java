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

package org.coodex.concrete.jaxrs.client;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.closure.CallableClosure;
import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.core.intercept.SyncInterceptorChain;
import org.coodex.concrete.jaxrs.struct.Unit;

import static org.coodex.concrete.common.ConcreteContext.runWithContext;

/**
 * Created by davidoff shen on 2017-03-09.
 */
@Deprecated
public abstract class AbstractInvoker implements Invoker {


    protected abstract MethodInvocation getInvocation(Unit unit, Object[] args, Object instance);

    @Override
    public final Object invoke(final Unit unit, final Object[] args, final Object instance) throws Throwable {
        return
                runWithContext(new JaxRSClientServiceContext(unit), new CallableClosure() {
                    @Override
                    public Object call() throws Throwable {
                        return getInterceptorChain().invoke(getInvocation(unit, args, instance));
                    }
                });
//                runWithContext(new JaxRSClientServiceContext(unit),new ConcreteClosure() {
//                    @Override
//                    public Object concreteRun() throws Throwable {
//                        return getInterceptorChain().invoke(getInvocation(unit, args, instance));
//                    }
//                });
//                run(CURRENT_UNIT, unit,
//                        run(MODEL, JaxRSHelper.JAXRS_MODEL,
//                                run(SUBJOIN, DEFAULT_SUBJOIN,
//                                        run(SIDE, SIDE_CLIENT, new ConcreteClosure() {
//                                            @Override
//                                            public Object concreteRun() throws Throwable {
//                                                return getInterceptorChain().invoke(getInvocation(unit, args, instance));
//                                            }
//                                        })
//                                )
//                        )
//                ).run();
//        return ConcreteContext.runWith(JaxRSHelper.JAXRS_MODEL, new JaxRSSubjoin(null), null, new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                return getInterceptorChain().invoke(getInvocation(unit, args, instance));
//            }
//        });
    }


//    private static SyncInterceptorChain interceptors;

    private static SyncInterceptorChain getInterceptorChain() {

        return ClientHelper.getSyncInterceptorChain();
//        if (interceptors == null) {
//            ServiceLoader<ConcreteInterceptor> spiFacade = new ConcreteServiceLoader<ConcreteInterceptor>() {
//            };
//            interceptors = new SyncInterceptorChain();
//            for (ConcreteInterceptor interceptor : spiFacade.getAllInstances()) {
//                interceptors.add(interceptor);
//            }
//        }
//        return interceptors;
    }


}
