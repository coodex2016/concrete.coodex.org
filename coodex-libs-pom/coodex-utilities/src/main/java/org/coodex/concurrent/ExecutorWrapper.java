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

package org.coodex.concurrent;

import org.coodex.util.Common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static org.coodex.util.ReflectHelper.getAllInterfaces;

/**
 * Created by davidoff shen on 2016-09-05.
 */
final class ExecutorWrapper {

    private static final Set<ExecutorService> executors = new HashSet<>();

    static <T extends ExecutorService> T wrap(T executorService) {
        // TODO 动态代理，当Executor shutdown或shutdownNow的时候脱离管理
        if (executorService instanceof ScheduledExecutorService) {
//            final ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) executorService;
//            final ScheduledExecutorService scheduledExecutorService = new ScheduledExecutorServiceImpl((ScheduledExecutorService) executorService);
            executorService = Common.cast(Proxy.newProxyInstance(
                    ScheduledExecutorService.class.getClassLoader(),
                    getAllInterfaces(executorService.getClass()),
                    getInvocationHandlerByType(ScheduledExecutorService.class,
                            executorService,
                            new ScheduledExecutorServiceImpl((ScheduledExecutorService) executorService))
            ));
        } else {
            executorService = Common.cast(Proxy.newProxyInstance(
                    ExecutorService.class.getClassLoader(),
                    getAllInterfaces(executorService.getClass()),
                    getInvocationHandlerByType(ExecutorService.class,
                            executorService,
                            new ExecutorServiceImpl(executorService))
            ));
        }
        executors.add(executorService);

        return executorService;
    }

    static void shutdown() {
        for (ExecutorService service : executors) {
            if (service != null && !service.isShutdown() && !service.isTerminated())
                service.shutdown();
        }
    }

    static List<Runnable> shutdownNow() {
        List<Runnable> list = new ArrayList<>();
        for (ExecutorService service : executors) {
            if (service != null && !service.isTerminated())
                list.addAll(service.shutdownNow());
        }
        return list;
    }

    private static InvocationHandler getInvocationHandlerByType(final Class<? extends ExecutorService> executorClass, final Object origin, final Object impl) {
        return (proxy, method, args) -> {
            Object object = method.getDeclaringClass().isAssignableFrom(executorClass) ?
                    impl : origin;

            if (args == null || args.length == 0)
                return method.invoke(object);
            else
                return method.invoke(object, args);
        };
    }
}
