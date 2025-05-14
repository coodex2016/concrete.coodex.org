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

package org.coodex.concrete.apitools;

import org.coodex.concrete.common.ErrorMessageFacade;
import org.coodex.concrete.common.modules.AbstractModule;
import org.coodex.concrete.common.modules.ModuleMaker;
import org.coodex.util.Common;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SelectableServiceLoader;

import java.util.*;
import java.util.function.Consumer;

import static org.coodex.concrete.common.ConcreteHelper.foreachClassInPackages;
import static org.coodex.concrete.common.ConcreteHelper.isConcreteService;

public class APIHelper {

    private final static SelectableServiceLoader<String, ModuleMaker<?>> MODULE_MAKERS =
            new LazySelectableServiceLoader<String, ModuleMaker<?>>() {
            };

    private static ModuleMaker<?> getInstance(String desc) {
//        if (MODULE_MAKERS.getAllInstances().size() == 0)
//            throw new RuntimeException("No service provider for " + ModuleMaker.class.getName());
//
//        for (ModuleMaker moduleMaker : MODULE_MAKERS.getAllInstances()) {
//            if (moduleMaker.isAccept(desc)) {
//                return moduleMaker;
//            }
//        }
        ModuleMaker<?> moduleMaker = MODULE_MAKERS.select(desc);
        if (moduleMaker == null)
            throw new RuntimeException("No module maker supported '" + desc + "' ");
        else
            return moduleMaker;
    }


    public static <MODULE extends AbstractModule<?>> List<MODULE> loadModules(
            String desc, String... packages) {

        ModuleMaker<MODULE> maker = Common.cast(getInstance(desc));
        return loadModules(maker, packages);
    }

    public static <M extends AbstractModule<?>> List<M> loadModules(
            String desc, Class<?>... classes) {
        ModuleMaker<M> maker = Common.cast(getInstance(desc));
        return loadModules(maker, classes);
    }

    private static <MODULE extends AbstractModule<?>> List<MODULE> loadModules(
            final ModuleMaker<MODULE> maker, Class<?>... classes) {

//        final Map<Class<?>, MODULE> moduleMap = new HashMap<>();
        ForEachClassConsumer<MODULE> consumer = new ForEachClassConsumer<>(maker);
        Arrays.stream(classes).forEach(consumer.classConsumer);
        return consumer.toList();
//
//
//        List<MODULE> moduleList = new ArrayList<>(consumer.moduleMap.values());
//        Collections.sort(moduleList);
//        return moduleList;
    }

    private static <MODULE extends AbstractModule<?>> List<MODULE> loadModules(
            final ModuleMaker<MODULE> maker, String... packages) {

        ForEachClassConsumer<MODULE> consumer = new ForEachClassConsumer<>(maker);
        foreachClassInPackages(consumer.classConsumer, packages);
        return consumer.toList();
//        final Map<Class<?>, MODULE> moduleMap = new HashMap<>();
//        foreachClassInPackages((serviceClass) -> {
//            if (isConcreteService(serviceClass)) {
//                MODULE module = maker.make(serviceClass);
//
//                Class<?> key = module.getInterfaceClass();//.getName();
//                MODULE exists = moduleMap.get(key);
//
//                if (exists != null) {
//                    throw new RuntimeException(
//                            String.format("Module %s duplicated. %s & %s",
//                                    key,
//                                    exists.getInterfaceClass().getName(),
//                                    module.getInterfaceClass().getName()));
//                }
//                moduleMap.put(key, module);
//            } else {
//                ErrorMessageFacade.register(serviceClass);
//            }
//        }, packages);
//
//        List<MODULE> moduleList = new ArrayList<>(moduleMap.values());
//        Collections.sort(moduleList);
//
//        return moduleList;
    }

    private static class ForEachClassConsumer<MODULE extends AbstractModule<?>> {
        private final ModuleMaker<MODULE> maker;
        private final Map<Class<?>, MODULE> moduleMap = new HashMap<>();
        private final Consumer<Class<?>> classConsumer = (serviceClass) -> {
            if (isConcreteService(serviceClass)) {
                if (moduleMap.containsKey(serviceClass)) {
                    throw new DuplicatedModuleException(String.format("Module %s duplicated.", serviceClass));
                }
                moduleMap.put(serviceClass, getMaker().make(serviceClass));
            } else {
                ErrorMessageFacade.register(serviceClass);
            }
        };

        private ForEachClassConsumer(ModuleMaker<MODULE> maker) {
            this.maker = maker;
        }

        private ModuleMaker<MODULE> getMaker() {
            return this.maker;
        }

        private List<MODULE> toList() {
            List<MODULE> moduleList = new ArrayList<>(moduleMap.values());
            Collections.sort(moduleList);
            return moduleList;
        }
    }
}
