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

package org.coodex.concrete.common;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.ErrorCode;
import org.coodex.concrete.api.Priority;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.config.Config;
import org.coodex.util.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.coodex.concrete.common.Token.CONCRETE_TOKEN_ID_KEY;
import static org.coodex.util.ReflectHelper.foreachClass;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class ConcreteHelper {

    public static final String VERSION = "0.5.1-SNAPSHOT";

    public static final String TAG_CLIENT = "client";
    public static final String KEY_DESTINATION = "destination";

    public static final String TOKEN_KEY = CONCRETE_TOKEN_ID_KEY;
    public static final String AGENT_KEY = "user-agent";
    public static final String LOCALE_KEY = "locale";


    public static final Integer DEFAULT_MAX_QUEUE_SIZE = 0x1988 + 0x0904;
    private static final Integer DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final Integer DEFAULT_MAX_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;

    private static final SelectableServiceLoader<Throwable, ThrowableToConcreteExceptionMapper<Throwable>> EXCEPTION_MAPPERS
            = new LazySelectableServiceLoader<Throwable, ThrowableToConcreteExceptionMapper<Throwable>>(
            new ThrowableToConcreteExceptionMapper<Throwable>() {
                @Override
                public ConcreteException toConcreteException(Throwable throwable) {
                    return new ConcreteException(ErrorCodes.UNKNOWN_ERROR, throwable.getLocalizedMessage(), throwable);
                }

                @Override
                public boolean accept(Throwable param) {
                    return true;
                }
            }
    ) {
    };

    private static final Class<?>[] PRIMITIVE_CLASSES = new Class<?>[]{
            String.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Void.class,
            boolean.class,
            char.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            void.class,
    };

    public static boolean isPrimitive(Class<?> c) {
        return Common.inArray(c, PRIMITIVE_CLASSES);
    }

    public static Integer getTokenMaxIdleInMinute() {
        return Config.getValue("token.maxIdleTime", 60, getAppSet());
    }

    public static Map<String, String> updatedMap(Subjoin subjoin) {
        Map<String, String> map = new ConcurrentHashMap<>();
        if (subjoin != null && subjoin.updatedKeySet().size() > 0) {
            for (String key : subjoin.updatedKeySet()) {
                map.put(key, subjoin.get(key));
            }
        }
        return map;
    }

    @Deprecated
    public static String getString(String tag, String module, String key) {
        return Config.get(key, tag, module);
    }

    public static ExecutorService getExecutor() {
        return getExecutor("service");
    }    private static final SingletonMap<String, ScheduledExecutorService> scheduledExecutorMap
            = SingletonMap.<String, ScheduledExecutorService>builder()
            .function(new Function<String, ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService apply(String key) {
                    String aliasTo = Config.get("scheduler", key, getAppSet());
                    if (Common.isBlank(aliasTo)) {
                        return ExecutorsHelper.newScheduledThreadPool(
                                Config.getValue("scheduler.executorSize", 1, key, getAppSet()),
                                key + ".scheduler"
                        );
                    } else {
                        return scheduledExecutorMap.get(aliasTo);
                    }
                }
            }).build();

    public static ScheduledExecutorService getScheduler() {
        return getScheduler("service");
    }

    public static ScheduledExecutorService getScheduler(String name) {
        return scheduledExecutorMap.get(name);
    }

    public static ExecutorService getExecutor(String executorName) {
        return executorServiceMap.get(executorName);
    }

    public static String getServiceName(Class<?> clz) {
        if (clz == null /*|| !ConcreteService.class.isAssignableFrom(clz)*/) return null;
        ConcreteService concreteService = clz.getAnnotation(ConcreteService.class);

        if (concreteService == null) return null;

        //clz.getAnnotation(Abstract.class) != null
        return Common.isBlank(concreteService.value()) ?
                (concreteService.nonspecific() ? "" : clz.getCanonicalName()) :
                Common.trim(concreteService.value(), "/\\.");
    }

    public static void foreachClassInPackages(Consumer<Class<?>> processor, String... packages) {
        String[] packageParrterns = packages;
        if (packageParrterns == null || packageParrterns.length == 0) {
            packageParrterns = getApiPackages();
        }
        if (packageParrterns == null) {
            packageParrterns = new String[0];
        }

        // 注册
        foreachClass((clazz) -> {
//            if (AbstractErrorCodes.class.isAssignableFrom(clazz)) {
                    ErrorMessageFacade.register(clazz);
//            }
                    processor.accept(clazz);

                }, (ConcreteClassFilter) clazz -> ConcreteHelper.isConcreteService(clazz) ||
                        clazz.getAnnotation(ErrorCode.class) != null,
                packageParrterns);
    }    private static final SingletonMap<String, ExecutorService> executorServiceMap
            = SingletonMap.<String, ExecutorService>builder()
            .function(new Function<String, ExecutorService>() {

                @Override
                public ExecutorService apply(String key) {
                    String aliasTo = Config.get("executor", key, getAppSet());
                    if (Common.isBlank(aliasTo)) {
                        return ExecutorsHelper.newPriorityThreadPool(
                                Config.getValue("executor.corePoolSize", DEFAULT_CORE_POOL_SIZE, key, getAppSet()),
                                Config.getValue("executor.maximumPoolSize", DEFAULT_MAX_POOL_SIZE, key, getAppSet()),
                                Config.getValue("executor.maxQueueSize", DEFAULT_MAX_QUEUE_SIZE, key, getAppSet()),
                                Config.getValue("executor.keepAliveTime", 60L, key, getAppSet()),
                                key + ".executor"
                        );
                    } else {
                        return executorServiceMap.get(aliasTo);
                    }
                }
            }).build();

    public static boolean isAbstract(Class<?> clz) {
        ConcreteService service = clz.getAnnotation(ConcreteService.class);
        return service != null && service.nonspecific();
    }

    public static boolean isConcreteService(Method method) {
        ConcreteService service = method.getAnnotation(ConcreteService.class);
        return service == null || !service.notService();
    }

    public static boolean isConcreteService(Class<?> clz) {
        return clz != null &&
                clz.isInterface() &&
                // ConcreteService.class.isAssignableFrom(clz) &&
                clz.getAnnotation(ConcreteService.class) != null &&
                !isAbstract(clz);
//                clz.getAnnotation(Abstract.class) == null;
    }

    public static int getPriority(Method method, Class<?> clz) {
        Priority priority = ConcreteHelper.getContext(method, clz).getAnnotation(Priority.class);
        return priority == null ?
                Thread.NORM_PRIORITY :
                Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, priority.value()));
    }

    public static int getPriority(AbstractUnit<?> unit) {
        return getPriority(unit.getMethod(), unit.getDeclaringModule().getInterfaceClass());
    }

    public static DefinitionContext getContext(Method method, Class<?> clz) throws ConcreteException {
        return IF.isNull(getContext(method, clz, new Stack<>()), ErrorCodes.MODULE_DEFINITION_NOT_FOUND);
    }

    private static boolean isRoot(Class<?> clz) {
        if (clz == null) return true;

        String className = clz.getName();
        return className.startsWith("java.") || className.startsWith("javax.");
    }

    private static DefinitionContext getContext(Method method, Class<?> clz, Stack<Class<?>> stack) {

//        if (clz == null) return null;

        // 如果找到根了，退出
        // ConcreteService.class.equals(clz)
        //                || !ConcreteService.class.isAssignableFrom(clz)
        if (isRoot(clz))
            return null;

        // 如果在栈内则表示检查过了
        if (stack.contains(clz)) {
            return null;
        } else {
            stack.add(clz);
        }

        // 查找服务定义
        //  clz.getAnnotation(ConcreteService.class) != null && clz.getAnnotation(Abstract.class) == null
        if (isConcreteService(clz)) {

            DefinitionContextImpl context = new DefinitionContextImpl();
            context.setDeclaringClass(clz);

            // 查找方法
            Method unitMethod = findMethod(method, clz);
            if (unitMethod == null)
                return null;
            else {
                context.setDeclaringMethod(unitMethod);
                return context;
            }
        }

        for (Class<?> clazz : clz.getInterfaces()) {
            DefinitionContext context = getContext(method, clazz, stack);
            if (context != null) {
                return context;
            }
        }

        return getContext(method, clz.getSuperclass(), stack);
    }

    private static Method findMethod(Method method, Class<?> clz) {
        return findMethod(method, clz, new Stack<>());
    }

    private static Method findMethod(Method method, Class<?> clz, Collection<Class<?>> stack) {
        if (stack.contains(clz))
            return null;
        else
            stack.add(clz);

        try {
            return clz.getDeclaredMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            for (Class<?> clazz : clz.getInterfaces()) {
                Method m = findMethod(method, clazz, stack);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public static ConcreteException findException(Throwable th) {
        if (th == null) return null;

        Throwable t = th;
        while (t != null) {
            if (t instanceof ConcreteException)
                return (ConcreteException) t;
            t = t.getCause();
        }
        return null;
    }

    ///////////////////////////////////////////////////////
    public static ConcreteException getException(Throwable th) {
        ConcreteException concreteException = findException(th);
        if (concreteException == null) {
            th = th instanceof ExceptionWrapperRuntimeException ? th.getCause() : th;
            concreteException = EXCEPTION_MAPPERS.select(th).toConcreteException(th);
//            concreteException = new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
        return concreteException;
    }

    public static ConcreteException getException(String message) {
        return new ConcreteException(ErrorCodes.UNKNOWN_ERROR, message);
    }

    public static List<Class<?>> inheritedChain(Class<?> root, Class<?> sub) {
        if (root == null || root.getAnnotation(ConcreteService.class) == null) return null;
        if (root.equals(sub)) {
            return Collections.emptyList();
        }
        for (Class<?> c : sub.getInterfaces()) {
            if (root.isAssignableFrom(c)) {
                List<Class<?>> subChain = inheritedChain(root, c);
                if (subChain != null) {
                    List<Class<?>> inheritedChain = new ArrayList<>();
                    inheritedChain.add(c);
                    inheritedChain.addAll(subChain);
                    return inheritedChain;
                }
            }
        }
        return null;
    }

    public static DefinitionContext getDefinitionContext(Class<?> cls, Method method) {
        DefinitionContextImpl definitionContext = new DefinitionContextImpl();
        definitionContext.setDeclaringClass(cls);
        definitionContext.setDeclaringMethod(method);
        return definitionContext;
    }

    public static String[] getApiPackages(String namespace) {
        String[] packages = Optional.ofNullable(Config.getArray("api.packages", "concrete", namespace, getAppSet()))
                .orElseGet(() -> new String[0]);
        return (packages.length == 0) ? getApiPackages() : packages;
    }

    public static String[] getApiPackages() {
        return Config.getArray("api.packages", new String[0], "concrete", getAppSet());
    }

    public static String getAppSet() {
//        return getProfile().getString("concrete.appSet");
        return System.getProperty("concrete.appSet");
//        return !Common.isBlank(appSet) ? appSet : Config.get("appSet", "concrete");
    }

    public static String devModelKey(String module) {
        return "org.coodex.concrete" + (
                Common.isBlank(module) ? "" : ("." + module)
        ) + ".devMode";
    }

    public static boolean isDevModel(String module) {
        return System.getProperty(devModelKey(module)) != null || System.getProperty(devModelKey(null)) != null;
    }






}
