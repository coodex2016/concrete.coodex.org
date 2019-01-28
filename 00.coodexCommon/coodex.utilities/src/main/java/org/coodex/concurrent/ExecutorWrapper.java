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

import org.coodex.util.Clock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by davidoff shen on 2016-09-05.
 */
public final class ExecutorWrapper {

    private static final Set<ExecutorService> executors = new HashSet<ExecutorService>();

    public static final <T extends ExecutorService> T wrap(T executorService) {
        // TODO 动态代理，当Executor shutdown或shutdownNow的时候脱离管理
        if (executorService instanceof ScheduledExecutorService) {
            final ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) executorService;
            executorService = (T) Proxy.newProxyInstance(
                    ScheduledExecutorService.class.getClassLoader(),
                    scheduledExecutorService.getClass().getInterfaces(),
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            Object[] argsCopy = args;

                            if (method.getDeclaringClass().equals(ScheduledExecutorService.class)) {
                                if (args != null && args.length > 0) {
                                    argsCopy = new Object[args.length];
                                    System.arraycopy(args, 0, argsCopy, 0, args.length);
                                }
                                if ("schedule".equals(method.getName())) {
                                    argsCopy[1] = Clock.toMillis((Long) args[1], (TimeUnit) args[2]);
                                    argsCopy[2] = TimeUnit.MILLISECONDS;
                                } else if ("scheduleAtFixedRate".equals(method.getName()) ||
                                        "scheduleWithFixedDelay".equals(method.getName())) {
                                    argsCopy[1] = Clock.toMillis((Long) args[1], (TimeUnit) args[3]);
                                    argsCopy[2] = Clock.toMillis((Long) args[2], (TimeUnit) args[3]);
                                    argsCopy[3] = TimeUnit.MILLISECONDS;
                                }
                            }
                            if (argsCopy == null || argsCopy.length == 0)
                                return method.invoke(scheduledExecutorService);
                            else
                                return method.invoke(scheduledExecutorService, argsCopy);
                        }
                    });
        }
        executors.add(executorService);

        return executorService;
    }

    public static void shutdown() {
        for (ExecutorService service : executors) {
            if (service != null && !service.isShutdown() && !service.isTerminated())
                service.shutdown();
        }
    }

    public static List<Runnable> shutdownNow() {
        List<Runnable> list = new ArrayList<Runnable>();
        for (ExecutorService service : executors) {
            if (service != null && !service.isTerminated())
                list.addAll(service.shutdownNow());
        }
        return list;
    }
}
