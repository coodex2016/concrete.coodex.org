/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.coodex.concrete.common;

import org.coodex.concrete.common.conflictsolutions.ThrowException;

import java.util.Map;

/**
 * Created by davidoff shen on 2016-12-03.
 */
public abstract class AbstractBeanProvider implements BeanProvider {

    private static final ConflictSolution DEFAULT_CONFLICT_SOLUTION = new ThrowException();

    private static final ConcreteSPIFacade<ConflictSolution> SOLUTION_CONCRETE_SPI_FACADE =
            new ConcreteSPIFacade<ConflictSolution>() {};

    private static final ConflictSolution getSolution(Class<?> clz) {
        // 1 从BeanProvider里找
        try {
            Map<String, ConflictSolution> map = BeanProviderFacade.getBeanProvider().getBeansOfType(ConflictSolution.class);
            if (map != null) {
                for (ConflictSolution solution : map.values()) {
                    if (solution != null && solution.accepted(clz))
                        return solution;
                }
            }
        } catch (Throwable th) {
        }
        // 2 ServiceLoader
        try {
            for (ConflictSolution solution : SOLUTION_CONCRETE_SPI_FACADE.getAllInstances()) {
                if (solution != null && solution.accepted(clz))
                    return solution;
            }
        } catch (Throwable th) {
        }


        // 3 从配置文件中读取
        try {
            Class c = Class.forName(ConcreteHelper.getProfile().getString(ConflictSolution.class.getCanonicalName()));
            return (ConflictSolution) c.newInstance();
        } catch (Throwable th) {
        }

        return DEFAULT_CONFLICT_SOLUTION;
    }

    @Override
    public final  <T> T getBean(Class<T> type) {
        Map<String, T> instanceMap = getBeansOfType(type);
        switch (instanceMap.size()) {
            case 0:
                // no service instance found.
                throw new ConcreteException(ErrorCodes.NO_SERVICE_INSTANCE_FOUND, type.getName());
            case 1:
                return instanceMap.values().iterator().next();
            default:
                // conflict
                return getSolution(type).conflict(instanceMap, type);
        }
    }

//    @Override
//    public <T> T getBean(String name) {
//        return null;
//    }

//    @Override
//    public <T> T getBean(Class<T> type, String name) {
//        return null;
//    }

//    @Override
//    public <T> Map<String, T> getBeansOfType(Class<T> type) {
//        return null;
//    }
}
