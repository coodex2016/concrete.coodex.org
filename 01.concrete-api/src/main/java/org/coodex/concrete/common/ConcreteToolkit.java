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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by davidoff shen on 2016-09-09.
 */
public class ConcreteToolkit {
    private final static Logger log = LoggerFactory.getLogger(ConcreteToolkit.class);

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

//    @SuppressWarnings("unchecked")
//    private static void loadServicesFrom(final Map<Class<ConcreteService>, ServiceDefinition> map,
//                                         String sPackage) {
//
//        foreachIntf(new ServiceIntfProcessor() {
//            @Override
//            public void process(Class serviceClass) {
//                if (!map.containsKey(serviceClass)) {
//                    Set<Method> methods = new LinkedHashSet<>();
//                    loadAllMethod(serviceClass, methods, null);
//                    map.put(serviceClass, new ServiceDefinition(serviceClass, methods));
//                }
//            }
//        }, sPackage);
//
//    }

//    private static Collection<ServiceDefinition> loadServices(String... packages) {
//        Map<Class<ConcreteService>, ServiceDefinition> map = new HashMap<>();
//        for (String s : packages) {
//            loadServicesFrom(map, s);
//        }
//        return map.values();
//    }


    public static String getServiceName(Class<?> clz) {
        if (clz == null) return null;
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

//    private static String getServiceAnnotation(ServiceDefinition definition) {
//        MicroService annotation = definition.getServiceClass().getAnnotation(MicroService.class);
//        return annotation == null ? "NULL" : "@MicroService(value = \"" + annotation.value() + "\")";
//    }

//    private static String getMethodAnnotation(Method method) {
//        MicroService annotation = method.getAnnotation(MicroService.class);
//        return annotation == null ? "NULL" : "@MicroService(value = \"" + annotation.value() + "\")";
//    }

//    private static ServiceDefinition methodRule(ServiceDefinition definition) {
//        Map<String, Method> methods = new HashMap<>();
//        for (Method method : definition.getMethods()) {
//            String methodName = getMethodName(method);
//            if (methods.containsKey(methodName)) {
//                Method method1 = methods.get(methodName);
//                log.warn("duplicate method [{}] in service [{}]: {}, {}, {} | {}, {}, {}",
//                        methodName, getServiceName(definition.getServiceClass()),
//                        method1.getDeclaringClass().getCanonicalName(),
//                        method1.getName(), getMethodAnnotation(method1),
//                        method.getDeclaringClass().getCanonicalName(),
//                        method.getName(), getMethodAnnotation(method));
//                continue;
//            }
//            methods.put(methodName, method);
//        }
//
//        return new ServiceDefinition(definition.getServiceClass(), methods.values());
//    }

//    public static Map<String, ServiceDefinition> loadDefinitions(String... packages) {
//        Map<String, ServiceDefinition> map = new HashMap<>();
//        for (ServiceDefinition definition : loadServices(packages)) {
//            String serviceName = getServiceName(definition.getServiceClass());
//            if (map.containsKey(serviceName)) { // 重名了
//                ServiceDefinition def1 = map.get(serviceName);
//
//                log.warn("duplicate service [{}]: {}, {} | {}, {}", serviceName,
//                        def1.getServiceClass().getName(), getServiceAnnotation(def1),
//                        definition.getServiceClass().getName(), getServiceAnnotation(definition));
//                continue;
//            }
//            map.put(serviceName, methodRule(definition));
//        }
//        return map;
//    }


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

//    private static final ClassFilter CONCRETE_ERROR_CODE = new ClassFilter() {
//        @Override
//        public boolean accept(Class<?> clazz) {
//            return clazz != null
//                    && AbstractErrorCode;
//        }
//    }


    public static void foreachIntf(ReflectHelper.Processer processor, String... packages) {
        ReflectHelper.foreachClass(processor, CONCRETE_SERVICE_INTERFACE_FILTER, packages);
    }


    private final static SPIFacade<ModuleMaker> MODULE_MAKERS = new ConcreteSPIFacade<ModuleMaker>() {
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

        final Map<String, MODULE> moduleMap = new HashMap<String, MODULE>();
        foreachIntf(new ReflectHelper.Processer() {
            @Override
            public void process(Class<?> serviceClass) {
                MODULE module = maker.make(serviceClass);

                String key = module.getName();
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

}
