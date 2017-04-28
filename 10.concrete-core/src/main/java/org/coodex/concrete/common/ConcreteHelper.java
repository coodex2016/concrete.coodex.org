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

package org.coodex.concrete.common;

import org.coodex.concrete.api.Abstract;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.MicroService;
import org.coodex.concrete.api.NotService;
import org.coodex.concrete.common.struct.AbstractModule;
import org.coodex.util.*;
import org.coodex.util.ServiceLoader;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by davidoff shen on 2016-09-02.
 */
public class ConcreteHelper {

    public static Profile getProfile() {
        return Profile.getProfile("concrete.properties");
    }

    public static Method[] getAllMethod(Class<?> serviceClass) {
        Set<Method> methods = new HashSet<Method>();
        loadAllMethod(serviceClass, methods, null);
        return methods.toArray(new Method[0]);
    }

    private static void loadAllMethod(Class<?> clz, Set<Method> methods, Set<Class> classes) {
        if (clz == null) return;
        if (methods == null) methods = new HashSet<Method>();
        if (classes == null) classes = new HashSet<Class>();

        if (classes.contains(clz)) return;
        classes.add(clz);

        for (Method method : clz.getMethods()) {
            if (method.getAnnotation(NotService.class) == null) {
                methods.add(method);
            }
        }

    }


    public static String getServiceName(Class<?> clz) {
        if (clz == null || !ConcreteService.class.isAssignableFrom(clz)) return null;
        MicroService concreteService = clz.getAnnotation(MicroService.class);

        if (concreteService == null) return null;

        return Common.isBlank(concreteService.value()) ?
                (clz.getAnnotation(Abstract.class) != null ? clz.getSimpleName() : clz.getCanonicalName()) :
                concreteService.value();
    }

    public static String getMethodName(Method method) {
        if (method == null) return null;
        MicroService concreteService = method.getAnnotation(MicroService.class);
        if (concreteService == null) return method.getName();
        return Common.isBlank(concreteService.value()) ? method.getName() : concreteService.value();
    }


    private static final ClassFilter CONCRETE_SERVICE_INTERFACE_FILTER = new ClassFilter() {
        @Override
        public boolean accept(Class<?> clazz) {
            return clazz != null
                    && clazz.isInterface() //是接口
                    && ConcreteService.class.isAssignableFrom(clazz) //是ConcreteService
                    && clazz.getAnnotation(MicroService.class) != null //定义了MicroService;
                    && clazz.getAnnotation(Abstract.class) == null //非抽象
                    ;
        }
    };


    public static void foreachService(ReflectHelper.Processor processor, String... packages) {
        ReflectHelper.foreachClass(processor, CONCRETE_SERVICE_INTERFACE_FILTER, packages);
    }


    private final static ServiceLoader<ModuleMaker> MODULE_MAKERS = new ConcreteServiceLoader<ModuleMaker>() {
    };

    @SuppressWarnings("unchecked")
    public final static <MODULE extends AbstractModule> List<MODULE> loadModules(
            String desc, String... packages) {

        if (MODULE_MAKERS.getAllInstances().size() == 0)
            throw new RuntimeException("No service provider for " + ModuleMaker.class.getName());

        for (ModuleMaker moduleMaker : MODULE_MAKERS.getAllInstances()) {
            if (moduleMaker.isAccept(desc)) {
                return loadModules(moduleMaker, packages);
            }
        }

        throw new RuntimeException("No service provider supported '" + desc + "' ");
    }

    @SuppressWarnings("unchecked")
    private static <MODULE extends AbstractModule> List<MODULE> loadModules(
            final ModuleMaker<MODULE> maker, String... packages) {

        final Map<Class, MODULE> moduleMap = new HashMap<Class, MODULE>();
        foreachService(new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                MODULE module = maker.make(serviceClass);

                Class key = module.getInterfaceClass();//.getName();
                MODULE exists = moduleMap.get(key);

                if (exists != null) {
                    throw new RuntimeException(
                            String.format("Module %s duplicated. %s & %s",
                                    key,
                                    exists.getInterfaceClass().getName(),
                                    module.getInterfaceClass().getName()));
                }
                moduleMap.put(key, module);
            }
        }, packages);

        List<MODULE> moduleList = new ArrayList<MODULE>(moduleMap.values());
        Collections.sort(moduleList);

        return moduleList;
    }

//    public static final String DEFAULT_TOKEN_KEY_FOR_CURRENT_ACCOUNT_ID = "token.key.currentAccountId.default";

//    public static final String TOKEN_KEY_FOR_CURRENT_ACCOUNT_ID =
//            getProfile().getString("token.key.currentAccountId", DEFAULT_TOKEN_KEY_FOR_CURRENT_ACCOUNT_ID);

//    public static Profile getProfile() {
//        return ConcreteToolkit.getProfile();
//    }

//    public static String getServicesName(Class<?> clz) {
//        return ConcreteToolkit.getServiceName(clz);
//    }
//
//    public static String getMethodName(Method method) {
//        return ConcreteToolkit.getMethodName(method);
//    }


//    public static DefinitionContext getContextIfFound(Method method, Class<?> clz) {
//        DefinitionContext context = getContext(method, clz);
//
//        Assert.is(context == null, ErrorCodes.MODULE_DEFINITION_NOT_FOUND,
//                method.getName(), clz.getCanonicalName());
//        Assert.is(context.getDeclaringMethod() == null,
//                ErrorCodes.UNIT_DEFINITION_NOT_FOUND,
//                getServiceName(clz), method.getName());
//        return context;
//    }


    public static DefinitionContext getContext(Method method, Class<?> clz) {
        return getContext(method, clz, new Stack<Class<?>>());
    }

    @SuppressWarnings("unchecked")
    private static DefinitionContext getContext(Method method, Class<?> clz, Stack<Class<?>> stack) {

        if (clz == null) return null;

        // 如果找到根了，退出
        if (ConcreteService.class.equals(clz)
                || !ConcreteService.class.isAssignableFrom(clz))
            return null;

        // 如果在栈内则表示检查过了
        if (stack.contains(clz)) {
            return null;
        } else {
            stack.add(clz);
        }

        // 查找服务定义
        if (clz.getAnnotation(MicroService.class) != null &&
                clz.getAnnotation(Abstract.class) == null) {

            DefinitionContextImpl context = new DefinitionContextImpl();
            context.setDeclaringClass((Class<ConcreteService>) clz);

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

    public static List<Class> inheritedChain(Class root, Class sub) {
        if (!ConcreteService.class.isAssignableFrom(root)) return null;
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


}
