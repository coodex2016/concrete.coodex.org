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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.*;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.util.ClassNameFilter;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;

import java.util.*;

import static org.coodex.util.ReflectHelper.foreachClass;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JaxRSServiceHelper {

    private static final ConcreteServiceLoader<ClassGenerator> CLASS_GENERATORS = new ConcreteServiceLoader<ClassGenerator>() {
    };


    private static final Set<Class> REGISTERED = new HashSet<>();
    private static final ClassNameFilter CONCRETE_ERROR = new ConcreteClassFilter() {
        @Override
        protected boolean accept(Class<?> clazz) {
            return clazz != null
                    && AbstractErrorCodes.class.isAssignableFrom(clazz);
        }
    };

    private static ClassGenerator getGenerator(String desc) {
        for (ClassGenerator classGenerator : CLASS_GENERATORS.getAllInstances()) {
            if (classGenerator.isAccept(desc))
                return classGenerator;
        }
        throw new RuntimeException("no class generator found for " + desc + ".");
    }

    private static String[] addDefaults(String[] packages) {
        Set<String> set = packages == null ? new HashSet<>() : Common.arrayToSet(packages);
        set.add(Polling.class.getPackage().getName());
        return set.toArray(new String[0]);
    }

    @Deprecated
    public synchronized static Set<Class<?>> generate(String desc, String... packages) {
        return generateByPackages(desc, packages);
    }

    private static Class<?> buildServiceImpl(String desc, Class<?> serviceClass) {
        try {
            return getGenerator(desc).generatesImplClass(ConcreteHelper.loadModule(desc, Polling.class));
        } catch (RuntimeException th) {
            throw th;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized static Set<Class<?>> generateByClasses(String desc, Class<?>... classes) {
        if (classes == null || classes.length == 0)
            return generateByPackages(desc);

        Set<Class<?>> result = new HashSet<Class<?>>();
        Set<Class> set = new HashSet<Class>();
        if (!REGISTERED.contains(Polling.class)) {
            result.add(buildServiceImpl(desc, Polling.class));
            set.add(Polling.class);
        }
        for (Class<?> clz : classes) {
            if (AbstractErrorCodes.class.isAssignableFrom(clz)) {
                ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) clz);
            } else if (ConcreteHelper.isConcreteService(clz)) {
                if (!REGISTERED.contains(clz) && !set.contains(clz)) {
                    result.add(buildServiceImpl(desc, clz));
                    set.add(clz);
                }
            } else {
                throw new RuntimeException("unable register class: " + clz.getName());
            }
        }
        REGISTERED.addAll(set);
        return result;
    }

    public synchronized static Set<Class<?>> generateByPackages(String desc, String... packages) {
        if (packages == null || packages.length == 0) {
            packages = ConcreteHelper.getApiPackages();
        }

        packages = addDefaults(packages);


        Set<Class<?>> classes = new HashSet<Class<?>>();
        ClassGenerator classGenerator = getGenerator(desc);

        registErrorCodes(packages);

        List<Module> modules = ConcreteHelper.loadModules(desc, packages);

        try {
            Set<Class> set = new HashSet<Class>();
            for (Module module : modules) {
                if (!REGISTERED.contains(module.getInterfaceClass()) && !set.contains(module.getInterfaceClass())) {
                    classes.add(classGenerator.generatesImplClass(module));
                    set.add(module.getInterfaceClass());
                }
            }
            REGISTERED.addAll(set);
            return classes;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    public static void foreachErrorClass(ReflectHelper.Processor processor, String... packages) {
        foreachClass(processor, CONCRETE_ERROR, packages);
    }

    @SuppressWarnings("unchecked")
    private static void registErrorCodes(String[] packages) {
        ReflectHelper.Processor processor = (Class<?> serviceClass) -> {
            if (AbstractErrorCodes.class.isAssignableFrom(serviceClass))
                ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) serviceClass);
        };

        foreachErrorClass(processor, AbstractErrorCodes.class.getPackage().getName());
        foreachErrorClass(processor, packages);
    }


    @SuppressWarnings("unchecked")
    public static List<ErrorDefinition> getAllErrorInfo(String... packages) {
        final List<ErrorDefinition> errorDefinitions = new ArrayList<ErrorDefinition>();
        registErrorCodes(packages);
        for (Integer i : ErrorMessageFacade.allRegisteredErrorCodes()) {
            errorDefinitions.add(new ErrorDefinition(i.intValue()));
        }
        Collections.sort(errorDefinitions);
        return errorDefinitions;
    }
}
