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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.*;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.util.ClassNameFilter;
import org.coodex.util.ReflectHelper;

import java.util.*;

import static org.coodex.util.ReflectHelper.foreachClass;

/**
 * Created by davidoff shen on 2016-12-07.
 */
public class JaxRSServiceHelper {

    private static final ConcreteServiceLoader<ClassGenerator> CLASS_GENERATORS = new ConcreteServiceLoader<ClassGenerator>() {
    };

    private static ClassGenerator getGenerator(String desc) {
        for (ClassGenerator classGenerator : CLASS_GENERATORS.getAllInstances()) {
            if (classGenerator.isAccept(desc))
                return classGenerator;
        }
        throw new RuntimeException("no class generator found for " + desc + ".");
    }


    public static Set<Class<?>> generate(String desc, String... packages) {
        if(packages == null){
            packages = ConcreteHelper.getApiPackages();
        }
        Set<Class<?>> classes = new HashSet<Class<?>>();
        ClassGenerator classGenerator = getGenerator(desc);

        registErrorCodes(packages);

        List<Module> modules = ConcreteHelper.loadModules(desc, packages);

        try {
            for (Module module : modules) {
                classes.add(classGenerator.generatesImplClass(module));
            }

            return classes;
        } catch (Throwable th) {
            throw new RuntimeException(th);
        }
    }

    private static final ClassNameFilter CONCRETE_ERROR = new ConcreteClassFilter() {
        @Override
        protected boolean accept(Class<?> clazz) {
            return clazz != null
                    && AbstractErrorCodes.class.isAssignableFrom(clazz);
        }
    };


    public static void foreachErrorClass(ReflectHelper.Processor processor, String... packages) {
        foreachClass(processor, CONCRETE_ERROR, packages);
    }

    @SuppressWarnings("unchecked")
    private static void registErrorCodes(String[] packages) {
//        ErrorMessageFacade.register(AbstractErrorCodes.class, ErrorCodes.class);
        ReflectHelper.Processor processor = new ReflectHelper.Processor() {
            @Override
            public void process(Class<?> serviceClass) {
                if (AbstractErrorCodes.class.isAssignableFrom(serviceClass))
                    ErrorMessageFacade.register((Class<? extends AbstractErrorCodes>) serviceClass);
            }
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
