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
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.aopalliance.intercept.MethodInvocation;
import org.coodex.closure.CallableClosure;
import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.impl.AbstractInvoker;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.rx.ReactiveExtensionFor;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.pojomocker.MockerFacade;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import static org.coodex.concrete.common.ConcreteHelper.getProfile;

public abstract class AbstractRxInvoker extends AbstractInvoker {


    private static ExecutorService executorService;

    public AbstractRxInvoker(Destination destination) {
        super(destination);
    }

    protected static Method findTargetMethod(Class targetClass, Method method) {
        Method targetMethod = null;
        for (Method m : targetClass.getMethods()) {
            if (m.getName().equals(method.getName()) && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())) {
                targetMethod = m;
                break;
            }
        }

        if (targetMethod == null) {
            throw new RuntimeException("Reactive method not found for " + targetClass.getName() + " " + method.getName());
        }
        return targetMethod;
    }

    public static RuntimeContext getRuntimeContext(Class rxClass, Method method) {
        final Class targetClass = ((ReactiveExtensionFor) rxClass.getAnnotation(ReactiveExtensionFor.class)).value();
        final Method targetMethod = findTargetMethod(targetClass, method);
        return RuntimeContext.getRuntimeContext(targetMethod, targetClass);
    }

    protected static Object buildSyncInstance(Class targetClass) throws IllegalAccessException, InvocationTargetException {
        return Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[]{targetClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy1, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass().equals(Object.class))
                    return method.invoke(proxy1, args);
                else
                    throw new RuntimeException("method " + method.getName() + " not implement.");
            }
        });
    }

    protected static ExecutorService getExecutorService() {
        if (executorService == null) {
            synchronized (AbstractRxInvoker.class) {
                if (executorService == null) {
                    executorService = ExecutorsHelper.newLinkedThreadPool(
                            getProfile().getInt("client.executor.corePoolSize", 0),
                            getProfile().getInt("client.executor.maximumPoolSize", Integer.MAX_VALUE),
                            getProfile().getInt("client.executor.keepAliveTime", 60)
                    );
                }
            }
        }
        return executorService;
    }

    /**
     * 执行逻辑，不需要考虑切片
     *
     * @param context
     * @param args
     * @return
     */
    protected abstract Observable invoke(RuntimeContext context, Object... args);

    private Observable invokeP(final RuntimeContext context, Object... args) {
        if (isMock()) {
            return Observable.create(new ObservableOnSubscribe() {
                @Override
                public void subscribe(ObservableEmitter e) throws Exception {
                    e.onNext(MockerFacade.mock(context.getDeclaringMethod(), context.getDeclaringClass()));
                }
            });
        } else {
            return invoke(context, args);
        }
    }

    @Override
    public final Object invoke(Object instance, Class clz, Method method, final Object... args) throws Throwable {

        return invokerWithAop(getRuntimeContext(clz, method), args);
    }

    @SuppressWarnings({"unchecked", "unsafe"})
    Observable invokerWithAop(final RuntimeContext runtimeContext, final Object[] args) throws InvocationTargetException, IllegalAccessException {
        final ServiceContext serviceContext = buildContext(runtimeContext.getDeclaringClass(), runtimeContext.getDeclaringMethod());
        final MethodInvocation invocation = new RXMethodInvocation(runtimeContext, args);
        // 调用前切片
        ConcreteContext.runWithContext(serviceContext, new CallableClosure() {
            @Override
            public Object call() throws Throwable {
                ClientHelper.getAsyncInterceptorChain().before(runtimeContext, invocation);
                return null;
            }
        });

//        ConcreteContext.runWithContext(serviceContext, new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                ClientHelper.getAsyncInterceptorChain().before(runtimeContext, invocation);
//                return null;
//            }
//        });


        return Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(final ObservableEmitter emitter) throws Exception {
                // 请求
                ConcreteContext.runWithContext(serviceContext, new CallableClosure() {
                    @Override

                    public Object call() throws Throwable {
                        invokeP(runtimeContext, args).subscribe(new Observer() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onNext(final Object o) {
                                //响应后切片
                                ConcreteContext.runWithContext(serviceContext, new CallableClosure() {
                                    @Override
                                    public Object call() throws Throwable {
                                        ClientHelper.getAsyncInterceptorChain().after(runtimeContext, invocation, o);
                                        emitter.onNext(o);
                                        return null;
                                    }
                                });
                            }

                            @Override
                            public void onError(Throwable e) {
                                emitter.onError(e);
                            }

                            @Override
                            public void onComplete() {
                                emitter.onComplete();
                            }
                        });
                        return null;
                    }
                });
//                ConcreteContext.runWithContext(serviceContext, new ConcreteClosure() {
//                    @Override
//                    public Object concreteRun() throws Throwable {
//                        invoke(runtimeContext, args).subscribe(new Observer() {
//                            @Override
//                            public void onSubscribe(Disposable d) {
//                            }
//
//                            @Override
//                            public void onNext(final Object o) {
//                                //响应后切片
//                                ConcreteContext.runWithContext(serviceContext, new ConcreteClosure() {
//                                    @Override
//                                    public Object concreteRun() throws Throwable {
//                                        ClientHelper.getAsyncInterceptorChain().after(runtimeContext, invocation, o);
//                                        emitter.onNext(o);
//                                        return null;
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                emitter.onError(e);
//                            }
//
//                            @Override
//                            public void onComplete() {
//                                emitter.onComplete();
//                            }
//                        });
//                        return null;
//                    }
//                });
            }
        });
    }
}
