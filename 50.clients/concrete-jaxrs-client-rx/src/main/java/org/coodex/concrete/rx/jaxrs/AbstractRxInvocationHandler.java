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

package org.coodex.concrete.rx.jaxrs;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import org.coodex.concurrent.ExecutorsHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public abstract class AbstractRxInvocationHandler implements InvocationHandler {

    private static ExecutorService threadPool = null;

    private static synchronized ExecutorService getThreadPool(){
        if (threadPool == null) {
            threadPool = ExecutorsHelper.newCachedThreadPool();
        }
        return threadPool;
    }

    protected final Class<?> serviceClass;


    public AbstractRxInvocationHandler(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    @SuppressWarnings("unchecked")
    protected Object sync(final Object instance, final Method method, final Object[] args) throws Throwable {
        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                Object result = method.invoke(instance, args);
                if (result != null)
                    e.onNext(result);
                e.onComplete();
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected Object async(final Object instance,  final Method method, final Object[] args) throws Throwable {
        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(final ObservableEmitter e) throws Exception {
                getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Object result = method.invoke(instance, args);
                            if (result != null)
                                e.onNext(result);
                            e.onComplete();
                        } catch (Throwable th) {
                            e.onError(th);
                        }
                    }
                });

            }
        });
    }

    protected Method findMethod(Method method) {
        for(Method m: serviceClass.getMethods()){
            if(m.getName().equals(method.getName())
                    && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())){
                return m;
            }
        }
        throw new RuntimeException("service method not found: " + method.getName());
    }


}
