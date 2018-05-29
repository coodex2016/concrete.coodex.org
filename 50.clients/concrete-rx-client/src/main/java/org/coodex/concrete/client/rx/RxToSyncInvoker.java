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

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.coodex.closure.CallableClosure;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.impl.AbstractInvoker;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.ServiceContext;

import java.lang.reflect.Method;

public abstract class RxToSyncInvoker extends AbstractInvoker {
    private final AbstractRxInvoker rxInvoker;

    public RxToSyncInvoker(Destination destination, AbstractRxInvoker rxInvoker) {
        super(destination);
        this.rxInvoker = rxInvoker;
    }

    @Override
    public Object invoke(Object instance, Class clz, Method method, final Object... args) throws Throwable {

        final RxResult rxResult = new RxResult();
        final RuntimeContext runtimeContext = RuntimeContext.getRuntimeContext(method, clz);
        ServiceContext context = rxInvoker.buildContext(clz, method);

        ConcreteContext.runWithContext(context, new CallableClosure() {
            @Override
            public Object call() throws Throwable {
                rxInvoker.invokerWithAop(runtimeContext, args).subscribe(new Observer() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Object o) {
                        rxResult.object = o;
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        rxResult.throwable = e;
                    }

                    @Override
                    public void onComplete() {
                        synchronized (rxResult) {
                            rxResult.completed = true;
                            if (rxResult.waiting)
                                rxResult.notify();
                        }

                    }
                });
                return null;
            }
        });
//        ConcreteContext.runWithContext(context, new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                rxInvoker.invokerWithAop(runtimeContext, args).subscribe(new Observer() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onNext(Object o) {
//                        rxResult.object = o;
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        e.printStackTrace();
//                        rxResult.throwable = e;
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        synchronized (rxResult) {
//                            rxResult.completed = true;
//                            if (rxResult.waiting)
//                                rxResult.notify();
//                        }
//
//                    }
//                });
//                return null;
//            }
//        });

        if (!rxResult.completed) {
            synchronized (rxResult) {
                if (!rxResult.completed) {
                    rxResult.waiting = true;
                    rxResult.wait();
                }
            }
        }

        if (rxResult.throwable != null) throw rxResult.throwable;
        return rxResult.object;
    }

    static class RxResult {
        private Object object;
        private Throwable throwable;
        private boolean waiting = false;
        private boolean completed = false;
    }
}
