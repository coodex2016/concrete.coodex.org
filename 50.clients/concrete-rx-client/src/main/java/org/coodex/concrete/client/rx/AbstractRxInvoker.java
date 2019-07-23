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
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.impl.AbstractInvoker;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.concrete.rx.ReactiveExtensionFor;
import org.coodex.mock.Mocker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public abstract class AbstractRxInvoker extends AbstractInvoker {


//    private static ExecutorService executorService;

    public AbstractRxInvoker(Destination destination) {
        super(destination);
    }


    private static Method findTargetMethod(Class targetClass, Method method) {
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

    private static DefinitionContext getDefinitionContext(Class rxClass, Method method) {
        final Class targetClass = ((ReactiveExtensionFor) rxClass.getAnnotation(ReactiveExtensionFor.class)).value();
        final Method targetMethod = findTargetMethod(targetClass, method);
        return ConcreteHelper.getDefinitionContext(targetClass, targetMethod);
    }

    static Object buildSyncInstance(Class targetClass) {
        return Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[]{targetClass}, (proxy1, method, args) -> {
            if (method.getDeclaringClass().equals(Object.class))
                return method.invoke(proxy1, args);
            else
                throw new RuntimeException("method " + method.getName() + " not implement.");
        });
    }


    static ExecutorService getExecutorService() {
//        if (executorService == null) {
//            synchronized (AbstractRxInvoker.class) {
//                if (executorService == null) {
//                    executorService = ExecutorsHelper.newLinkedThreadPool(
//                            getProfile().getInt("client.executor.corePoolSize", 0),
//                            getProfile().getInt("client.executor.maximumPoolSize", Integer.MAX_VALUE),
//                            getProfile().getInt("client.executor.keepAliveTime", 60)
//                    );
//                }
//            }
//        }
//        return executorService;
        return ConcreteHelper.getExecutor("client");
    }

    /**
     * 执行逻辑，不需要考虑切片
     */
    protected abstract Observable invoke(DefinitionContext context, Object... args);

    private Observable invokeP(final DefinitionContext context, Object... args) {
        if (isMock()) {
            //noinspection unchecked
            return Observable.create((ObservableOnSubscribe) e -> e.onNext(Mocker.mockMethod(
                    context.getDeclaringMethod(),
                    context.getDeclaringClass())));
        } else {
            return invoke(context, args);
        }
    }

    @Override
    public final Object invoke(Object instance, Class clz, Method method, final Object... args) throws Throwable {

        return invokerWithAop(getDefinitionContext(clz, method), args);
    }

    @SuppressWarnings({"unchecked", "unsafe"})
    Observable invokerWithAop(final DefinitionContext runtimeContext, final Object[] args) throws InvocationTargetException, IllegalAccessException {
        final ServiceContext serviceContext = buildContext(runtimeContext);
        final MethodInvocation invocation = new RXMethodInvocation(runtimeContext, args);
        // 调用前切片
        ConcreteContext.runWithContext(serviceContext, () -> {
            ClientHelper.getAsyncInterceptorChain().before(runtimeContext, invocation);
            return null;
        });

//        ConcreteContext.runWithContext(serviceContext, new ConcreteClosure() {
//            @Override
//            public Object concreteRun() throws Throwable {
//                ClientHelper.getAsyncInterceptorChain().before(runtimeContext, invocation);
//                return null;
//            }
//        });


        return Observable.create((ObservableOnSubscribe) emitter -> {
            // 请求
            ConcreteContext.runWithContext(serviceContext, () -> {
                invokeP(runtimeContext, args).subscribe(new Observer() {
                    private boolean notNull = false;
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(final Object o) {
                        //响应后切片
                        notNull = true;
                        ConcreteContext.runWithContext(serviceContext,
                                () -> {
                                ClientHelper.getAsyncInterceptorChain().after(runtimeContext, invocation, o);
                                emitter.onNext(o);
                                return null;
                        });
                    }

                    @Override
                    public void onError(Throwable e) {
                        ConcreteContext.runWithContext(serviceContext,
                                () -> {
                                    ClientHelper.getAsyncInterceptorChain().onError(runtimeContext, invocation, e);
                                    emitter.onError(e);
                                    return null;
                                });
                    }

                    @Override
                    public void onComplete() {
                        ConcreteContext.runWithContext(serviceContext,
                                () -> {
                                    if (!notNull)
                                        ClientHelper.getAsyncInterceptorChain().after(runtimeContext, invocation, null);
                                    emitter.onComplete();
                                    return null;
                                });
                    }
                });
                return null;
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
        });
    }
}
