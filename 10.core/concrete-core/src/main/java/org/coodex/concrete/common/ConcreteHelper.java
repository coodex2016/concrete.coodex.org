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
import org.coodex.concrete.api.Priority;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.config.Config;
import org.coodex.util.ClassNameFilter;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.coodex.util.SingletonMap;

import javax.annotation.CheckForNull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

import static org.coodex.util.ReflectHelper.foreachClass;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class ConcreteHelper {

    public static final String VERSION = "0.3.0-SNAPSHOT";

    public static final String TAG_CLIENT = "client";
    public static final String KEY_LOCATION = "location";

    private static final ClassNameFilter CONCRETE_SERVICE_INTERFACE_FILTER = new ConcreteClassFilter() {
        @Override
        protected boolean accept(Class<?> clazz) {
//            return clazz != null
//                    && clazz.isInterface() //是接口
////                    && ConcreteService.class.isAssignableFrom(clazz) //是ConcreteService
//                    && clazz.getAnnotation(ConcreteService.class) != null //定义了MicroService;
//                    && clazz.getAnnotation(Abstract.class) == null //非抽象
//                    ;
            return isConcreteService(clazz);
        }
    };


//    private final static ServiceLoader<ModuleMaker> MODULE_MAKERS = new ConcreteServiceLoader<ModuleMaker>() {
//
//    };
    //    private static ExecutorService executorService;
//    private static Singleton<ExecutorService> executorService = new Singleton<ExecutorService>(new Singleton.Builder<ExecutorService>() {
//        @Override
//        public ExecutorService build() {
//            return ExecutorsHelper.newPriorityThreadPool(
//                    getProfile().getInt("service.executor.corePoolSize", 0),
//                    getProfile().getInt("service.executor.maximumPoolSize", Integer.MAX_VALUE),
//                    getProfile().getInt("service.executor.keepAliveTime", 60)
//            );
//        }
//    });

    private static SingletonMap<String, ScheduledExecutorService> scheduledExecutorMap = new SingletonMap<String, ScheduledExecutorService>(
            new SingletonMap.Builder<String, ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build(String key) {
                    String aliasTo = Config.get("scheduler", key, getAppSet());
                    if (Common.isBlank(aliasTo)) {
                        return ExecutorsHelper.newScheduledThreadPool(
                                Config.getValue("scheduler.executorSize", 1, key, getAppSet()),
                                key + ".scheduler"
                        );
                    } else {
                        return scheduledExecutorMap.getInstance(aliasTo);
                    }
                }
            }
    );

    private static SingletonMap<String, ExecutorService> executorServiceMap = new SingletonMap<String, ExecutorService>(
            new SingletonMap.Builder<String, ExecutorService>() {

                @Override
                public ExecutorService build(String key) {
                    String aliasTo = Config.get("executor", key, getAppSet());
                    if (Common.isBlank(aliasTo)) {
                        return ExecutorsHelper.newPriorityThreadPool(
                                Config.getValue("executor.corePoolSize", 0, key, getAppSet()),
                                Config.getValue("executor.maximumPoolSize", Integer.MAX_VALUE, key, getAppSet()),
                                Config.getValue("executor.keepAliveTime", 60, key, getAppSet()),
                                key + ".executor"
                        );
                    } else {
                        return executorServiceMap.getInstance(aliasTo);
                    }
//                    return ExecutorsHelper.newP;
                }
            }
    );

//    @Deprecated
//    private static Profile_Deprecated getDefaultProfile(String tag) {
//        return Profile_Deprecated.getProfile(tag + ".properties");
//    }

    public static Integer getTokenMaxIdleInMinute() {
//        return getProfile().getInt("token.maxIdleTime", 60);
        return Config.getValue("token.maxIdleTime", 60, getAppSet());
    }

//    @Deprecated
//    public static Profile_Deprecated getProfile() {
//        return getProfile("concrete");
//    }
//
//    @Deprecated
//    public static Profile_Deprecated getProfile(String tag) {
//        return getProfile(tag, null);
//    }

//    @Deprecated
//    public static Profile_Deprecated getProfile(String tag, String sub) {
//        return Common.isBlank(sub) ?
//                getDefaultProfile(tag) :
//                Profile_Deprecated.getProfile(tag + "." + sub + ".properties");
//    }

    public static Map<String, String> updatedMap(Subjoin subjoin) {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        if (subjoin != null && subjoin.updatedKeySet().size() > 0) {
            for (String key : subjoin.updatedKeySet()) {
                map.put(key, subjoin.get(key));
            }
        }
        return map;
    }

    public static String getString(String tag, String module, String key) {
//        Profile_Deprecated profile = getProfile(tag, module);
//        String value = profile.getString(key);
//        if (value == null && !Common.isBlank(module)) {
//            profile = getProfile(tag);
//            value = profile.getString(String.format("%s.%s", module, key));
//            if (value == null) {
//                value = profile.getString(key);
//            }
//        }
//        if (value == null) {
//            profile = getProfile();
//            if (!Common.isBlank(module)) {
//                value = profile.getString(String.format("%s.%s.%s", tag, module, key));
//            }
//            if (value == null) {
//                value = profile.getString(String.format("%s.%s", tag, key));
//            }
//        }
//        return value;
        return Config.get(key, tag, module);
    }

    public static ExecutorService getExecutor() {
//        if (executorService == null) {
//            synchronized (ConcreteHelper.class) {
//                if (executorService == null) {
//                    executorService = ExecutorsHelper.newPriorityThreadPool(
//                            getProfile().getInt("service.executor.corePoolSize", 0),
//                            getProfile().getInt("service.executor.maximumPoolSize", Integer.MAX_VALUE),
//                            getProfile().getInt("service.executor.keepAliveTime", 60)
//                    );
//                }
//            }
//        }
//        return executorService;
//        return executorService.getInstance();
        return getExecutor("service");
    }

    public static ScheduledExecutorService getScheduler() {
        return getScheduler("service");
    }

    public static ScheduledExecutorService getScheduler(String name) {
        return scheduledExecutorMap.getInstance(name);
    }

    public static ExecutorService getExecutor(String executorName) {
        return executorServiceMap.getInstance(executorName);
    }

//    public static Method[] getAllMethod(Class<?> serviceClass) {
//        Set<Method> methods = new HashSet<Method>();
//        loadAllMethod(serviceClass, methods, null);
//        return methods.toArray(new Method[0]);
//    }

//    private static void loadAllMethod(Class<?> clz, Set<Method> methods, Set<Class> classes) {
//        if (clz == null) return;
//        if (methods == null) methods = new HashSet<Method>();
//        if (classes == null) classes = new HashSet<Class>();
//
//        if (classes.contains(clz)) return;
//        classes.add(clz);
//
//        for (Method method : clz.getMethods()) {
//            if (isConcreteService(method)) {
//                methods.add(method);
//            }
//        }
//
//    }

    public static String getServiceName(Class<?> clz) {
        if (clz == null /*|| !ConcreteService.class.isAssignableFrom(clz)*/) return null;
        ConcreteService concreteService = clz.getAnnotation(ConcreteService.class);

        if (concreteService == null) return null;

        //clz.getAnnotation(Abstract.class) != null
        return Common.isBlank(concreteService.value()) ?
                (concreteService.abstractive() ? clz.getSimpleName() : clz.getCanonicalName()) :
                concreteService.value();
    }

//    public static String getMethodName(Method method) {
//        if (method == null) return null;
//        ConcreteService concreteService = method.getAnnotation(ConcreteService.class);
//        if (concreteService == null) return method.getName();
//        if (concreteService.notService()) return null;
//        return Common.isBlank(concreteService.value()) ? method.getName() : concreteService.value();
//    }

//    @Deprecated
//    public static void foreachService(ReflectHelper.Processor processor, String... packages) {
//        ReflectHelper.foreachClass(processor, CONCRETE_SERVICE_INTERFACE_FILTER, packages);
//    }


    public static void foreachClassInPackages(ReflectHelper.Processor processor, String... packages) {
        String[] packageParrterns = packages;
        if (packageParrterns == null || packageParrterns.length == 0) {
            packageParrterns = getApiPackages();
        }
        if (packageParrterns == null) {
            packageParrterns = new String[0];
        }
        final String[] finalParrterns = packageParrterns;

        packages = toPackages(packageParrterns);
        // 排序
        for (int i = 0, l = packages.length; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                String p1 = packages[i];
                String p2 = packages[j];
                if (p1.length() > p2.length()) {
                    packages[i] = p2;
                    packages[j] = p1;
                }
            }
        }
        // 合并
        List<String> forSearch = new ArrayList<String>();

        packageCycle:
        for (String pkg : packages) {
            for (String searchPath : forSearch) {
                if (Common.isBlank(searchPath) || pkg.equals(searchPath) || pkg.startsWith(searchPath + ".")) {
                    continue packageCycle;
                }
            }
            forSearch.add(pkg);
        }
        // 注册
        foreachClass((clazz) -> {
            if (AbstractErrorCodes.class.isAssignableFrom(clazz)) {
                //noinspection unchecked
                ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) clazz);
            }
            processor.process(clazz);

        }, new ConcreteClassFilter() {
            @Override
            protected boolean accept(Class<?> clazz) {
                return isMatch(clazz, finalParrterns) &&
                        (ConcreteHelper.isConcreteService(clazz) ||
                                AbstractErrorCodes.class.isAssignableFrom(clazz));
            }
        }, forSearch.toArray(new String[0]));
    }

    private static String[] toPackages(String[] patterns) {
        String[] result = new String[patterns.length];
        for (int i = 0, l = patterns.length; i < l; i++) {
            result[i] = toPackage(patterns[i]);
        }
        return result;
    }

    private static String toPackage(String parttern) {
        int i = parttern.indexOf('*');
        return Common.trim(i > 0 ? parttern.substring(0, i) : parttern, '.');
    }

    private static boolean isMatch(Class<?> clazz, String... packagePartterns) {
        String className = clazz.getName();
        for (String s : packagePartterns) {
            if (s.indexOf('*') > 0) {
                if (Pattern.compile("^" +
                        s.replaceAll("\\.", "\\\\.")
                                .replaceAll("\\*\\*", ".+")
                                .replaceAll("\\*", "[\\\\w]+")).matcher(className).find())
                    return true;
            } else {
                if (className.startsWith(s))
                    return true;
            }
        }
        return false;
    }


    public static boolean isAbstract(Class<?> clz){
        ConcreteService service = clz.getAnnotation(ConcreteService.class);
        return service != null && service.abstractive();
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


//    public static <MODULE extends AbstractModule> MODULE loadModule(
//            String desc, Class<?> serviceClass) {
//        return (MODULE) loadModule(getInstance(desc), serviceClass);
//    }

//    public static <MODULE extends AbstractModule> MODULE loadModule(
//            ModuleMaker<MODULE> moduleMaker, Class<?> serviceClass) {
//
//        return moduleMaker.make(serviceClass);
//    }




    public static int getPriority(Method method, Class<?> clz) {
        Priority priority = ConcreteHelper.getContext(method, clz).getAnnotation(Priority.class);
        return priority == null ?
                Thread.NORM_PRIORITY :
                Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, priority.value()));
    }


    public static int getPriority(AbstractUnit unit) {
        return getPriority(unit.getMethod(), unit.getDeclaringModule().getInterfaceClass());
    }

    public static DefinitionContext getContext(Method method, Class<?> clz) throws ConcreteException{
        return IF.isNull(getContext(method, clz, new Stack<Class<?>>()), ErrorCodes.MODULE_DEFINITION_NOT_FOUND);
    }

    private static boolean isRoot(Class clz) {
        if (clz == null) return true;

        String className = clz.getName();
        if (className.startsWith("java.") || className.startsWith("javax.")) return true;

        return false;
    }

    @SuppressWarnings("unchecked")
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
        return findMethod(method, clz, new Stack<Class<?>>());
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

    public final static ConcreteException findException(Throwable th) {
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
    public final static ConcreteException getException(Throwable th) {
        ConcreteException concreteException = findException(th);
        if (concreteException == null) {
            concreteException = new ConcreteException(ErrorCodes.UNKNOWN_ERROR, th.getLocalizedMessage(), th);
        }
        return concreteException;
    }


//    public static <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass) {
//        T annotation = method.getAnnotation(annotationClass);
//        return annotation == null ? method.getDeclaringClass().getAnnotation(annotationClass) : annotation;
//    }

    @SuppressWarnings("unchecked")
    public static List<Class> inheritedChain(Class root, Class sub) {
        if (root == null || root.getAnnotation(ConcreteService.class) == null) return null;
//        Stack<Class<?>> inheritedChain = new Stack<Class<?>>();
        if (root.equals(sub)) {
            return Arrays.asList();
        }
        for (Class<?> c : sub.getInterfaces()) {
            if (root.isAssignableFrom(c)) {
                List<Class> subChain = inheritedChain(root, c);
                if (subChain != null) {
                    List<Class> inheritedChain = new ArrayList<Class>();
                    inheritedChain.add(c);
                    inheritedChain.addAll(subChain);
                    return inheritedChain;
                }
            }
        }
        return null;
    }

    public static DefinitionContext getDefinitionContext(Class<?> cls, Method method){
        DefinitionContextImpl definitionContext = new DefinitionContextImpl();
        definitionContext.setDeclaringClass(cls);
        definitionContext.setDeclaringMethod(method);
        return definitionContext;
    }


    public static String[] getApiPackages() {
        return Config.getArray("api.packages", ",", new String[0], "concrete", getAppSet());
    }

//    public static String[] getRemoteApiPackages() {
//        return Config.getArray("remoteapi.packages", ",", new String[0], "concrete", getAppSet());
//    }

    public static String getAppSet() {
//        return getProfile().getString("concrete.appSet");
        return System.getProperty("concrete.appSet");
//        return !Common.isBlank(appSet) ? appSet : Config.get("appSet", "concrete");
    }


    public static String devModelKey(String module) {
        String key = "org.coodex.concrete" + (
                Common.isBlank(module) ? "" : ("." + module)
        ) + ".devMode";
        return key;
    }

    public static boolean isDevModel(String module) {
        return System.getProperty(devModelKey(module)) != null || System.getProperty(devModelKey(null)) != null;
    }
}
