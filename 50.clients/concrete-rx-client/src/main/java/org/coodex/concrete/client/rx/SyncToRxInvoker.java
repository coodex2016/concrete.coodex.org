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

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.coodex.closure.CallableClosure;
import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.ClientSideContext;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.util.Common;

import java.lang.reflect.Method;


public class SyncToRxInvoker extends AbstractRxInvoker {

    public SyncToRxInvoker(Destination destination) {
        super(destination);
    }

    @Override
    public ServiceContext buildContext(Class concreteClass, Method method) {
        return new ToAsyncClientContext(getDestination(), RuntimeContext.getRuntimeContext(method, concreteClass));
    }

    private Destination getSyncDestination() {
        try {
            Destination destination = Common.deepCopy(getDestination());
            destination.setAsync(false);
            return destination;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }


    @Override
    public Observable invoke(final RuntimeContext runtimeContext, final Object... args) {
        final ClientSideContext clientServiceContext = new ToAsyncClientContext(getDestination(), runtimeContext);

        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(final ObservableEmitter e) throws Exception {
                getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final Destination destination = getSyncDestination();
                            Object result = ConcreteContext.runWithContext(clientServiceContext, new CallableClosure() {
                                @Override
                                public Object call() throws Throwable {
                                    return ClientHelper.getInvokerFactoryProviders()
                                            .getServiceInstance(destination)
                                            .getInvoker(destination)
                                            .invoke(
                                                    buildSyncInstance(runtimeContext.getDeclaringClass()),
                                                    runtimeContext.getDeclaringClass(),
                                                    runtimeContext.getDeclaringMethod(),
                                                    args);
                                }
                            });
//                            Object result = ConcreteContext.runWithContext(clientServiceContext, new ConcreteClosure() {
//                                @Override
//                                public Object concreteRun() throws Throwable {
//                                    return ClientHelper.getInvokerFactoryProviders()
//                                            .getServiceInstance(destination)
//                                            .getInvoker(destination)
//                                            .invoke(
//                                                    buildSyncInstance(runtimeContext.getDeclaringClass()),
//                                                    runtimeContext.getDeclaringClass(),
//                                                    runtimeContext.getDeclaringMethod(),
//                                                    args);
//                                }
//                            });
                            e.onNext(result);
                        } catch (Throwable th) {
                            e.onError(th);
                        } finally {
                            e.onComplete();
                        }
                    }
                });
            }
        });
    }

}
