/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.ClientHelper;
import org.coodex.concrete.api.rx.CompletableFutureBridge;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;
import org.coodex.concrete.client.Destination;
import org.coodex.concrete.client.RxInvoker;
import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ServiceContext;
import org.coodex.util.Common;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import static org.coodex.concrete.ClientHelper.getCompletableFutureBridge;
import static org.coodex.concrete.ClientHelper.getRxClientScheduler;

public abstract class AbstractRxInvoker extends AbstractInvoker implements RxInvoker {


    public AbstractRxInvoker(Destination destination) {
        super(destination);
    }

    public static Object buildSyncInstance(Class<?> targetClass) {
        return Proxy.newProxyInstance(targetClass.getClassLoader(), new Class<?>[]{targetClass}, (proxy1, method, args) -> {
            if (method.getDeclaringClass().equals(Object.class))
                return method.invoke(proxy1, args);
            else
                throw new RuntimeException("method " + method.getName() + " not implement.");
        });
    }

    /**
     * @param instance instance
     * @param clz      clz
     * @param method   method
     * @param args     args
     * @return 返回响应式接口对象
     */
    @Override
    public Object invoke(Object instance, Class<?> clz, Method method, Object... args) {
        CompletableFutureBridge bridge = getCompletableFutureBridge(method.getReturnType());
        if (bridge == null) {
            throw new RuntimeException("none completableFutureBridge found:" + method.getReturnType());
        }


        return bridge.bridging(invokeAsync(instance, clz, method, args));
    }

    @Override
    public CompletableFuture<?> invokeAsync(Object instance, Class<?> clz, Method method, Object... args) {
        final DefinitionContext runtimeContext = getDefinitionContext(clz, method);
        final ServiceContext serviceContext = buildContext(runtimeContext);
        final MethodInvocation invocation = new RXMethodInvocation(runtimeContext, args);

        Trace trace = APM.build();
        trace.start();
        try {
            // 1. before切片
            ConcreteContext.runWithContext(serviceContext, () -> {
                ClientHelper.getAsyncInterceptorChain().before(runtimeContext, invocation);
                return null;
            });
        } catch (Throwable th) {
            trace.error(th);
            trace.finish();
            throw Common.rte(th);
        }

        // 2. apm

        return wrap(serviceContext, invocation, runtimeContext,
                futureInvoke(runtimeContext, args), trace);
    }

    /**
     * @param serviceContext serviceContext
     * @param invocation     invocation
     * @param runtimeContext runtimeContext
     * @param future         future
     * @param trace          trace
     * @return 在future的基础上增加拦截器处理和apm处理
     */
    private CompletableFuture<?> wrap(ServiceContext serviceContext,
                                      MethodInvocation invocation,
                                      DefinitionContext runtimeContext,
                                      CompletableFuture<?> future, Trace trace) {
        return future.handleAsync((BiFunction<Object, Throwable, Object>) (o, throwable) -> {
            if (throwable != null) { // 异常
                trace.error(throwable);
                try {
                    throw Common.rte(throwable);
                } finally {
                    trace.finish();
                }
            } else {
                try {
                    return ConcreteContext.runWithContext(serviceContext,
                            () -> ClientHelper.getAsyncInterceptorChain().after(runtimeContext, invocation, o)
                    );
                } catch (Throwable th) {
                    trace.error(th);
                    throw Common.rte(th);
                } finally {
                    trace.finish();
                }
            }
        }, getRxClientScheduler());
    }

    protected abstract CompletableFuture<?> futureInvoke(DefinitionContext runtimeContext, Object[] args);
}
