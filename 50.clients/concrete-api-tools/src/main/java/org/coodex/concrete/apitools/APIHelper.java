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

import org.coodex.concrete.common.modules.AbstractModule;
import org.coodex.concrete.common.modules.ModuleMaker;
import org.coodex.util.AcceptableServiceLoader;

import java.util.*;

import static org.coodex.concrete.common.ConcreteHelper.foreachClassInPackages;
import static org.coodex.concrete.common.ConcreteHelper.isConcreteService;

public class APIHelper {

    private final static AcceptableServiceLoader<String, ModuleMaker<?>> MODULE_MAKERS = new AcceptableServiceLoader<>();

    private static ModuleMaker getInstance(String desc) {
//        if (MODULE_MAKERS.getAllInstances().size() == 0)
//            throw new RuntimeException("No service provider for " + ModuleMaker.class.getName());
//
//        for (ModuleMaker moduleMaker : MODULE_MAKERS.getAllInstances()) {
//            if (moduleMaker.isAccept(desc)) {
//                return moduleMaker;
//            }
//        }
        ModuleMaker moduleMaker = MODULE_MAKERS.getServiceInstance(desc);
        if (moduleMaker == null)
            throw new RuntimeException("No module maker supported '" + desc + "' ");
        else
            return moduleMaker;
    }

    @SuppressWarnings("unchecked")
    public final static <MODULE extends AbstractModule> List<MODULE> loadModules(
            String desc, String... packages) {

        return loadModules(getInstance(desc), packages);
    }

    @SuppressWarnings("unchecked")
    private static <MODULE extends AbstractModule> List<MODULE> loadModules(
            final ModuleMaker<MODULE> maker, String... packages) {

        final Map<Class, MODULE> moduleMap = new HashMap<>();
        foreachClassInPackages((serviceClass) ->{
                if(isConcreteService(serviceClass)) {
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
}
