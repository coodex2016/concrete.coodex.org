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

import org.coodex.concrete.common.conflictsolutions.ThrowException;
import org.coodex.config.Config;
import org.coodex.util.LazySelectableServiceLoader;

import java.util.HashSet;
import java.util.Map;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

/**
 * Created by davidoff shen on 2016-12-03.
 */
public abstract class AbstractBeanProvider implements BeanProvider {

    public static final String CREATE_BY_CONCRETE = "cbc_";

    private static final ConflictSolution DEFAULT_CONFLICT_SOLUTION = new ThrowException();

    private static final LazySelectableServiceLoader<Class<?>, ConflictSolution> SOLUTION_CONCRETE_SPI_FACADE =
            new LazySelectableServiceLoader<Class<?>, ConflictSolution>() {
            };

    private static ConflictSolution getSolution(Class<?> clz) {
//        // 1 从BeanProvider里找
//        try {
//            Map<String, ConflictSolution> map = BeanServiceLoaderProvider.getBeanProvider().getBeansOfType(ConflictSolution.class);
//            if (map != null) {
//                for (ConflictSolution solution : map.values()) {
//                    if (solution != null && solution.accepted(clz))
//                        return solution;
//                }
//            }
//        } catch (Throwable th) {
//        }
//        // 2 ServiceLoader
//        try {
//            for (ConflictSolution solution : SOLUTION_CONCRETE_SPI_FACADE.getAllInstances()) {
//                if (solution != null && solution.accepted(clz))
//                    return solution;
//            }
//        } catch (Throwable th) {
//        }
        ConflictSolution conflictSolution = SOLUTION_CONCRETE_SPI_FACADE.select(clz);
        if (conflictSolution != null) return conflictSolution;

        // 从配置中心读取
        try {
            return (ConflictSolution) Class.forName(
                    Config.get(ConflictSolution.class.getCanonicalName(), getAppSet())
            ).getDeclaredConstructor().newInstance();
        } catch (Throwable ignored) {
        }

        return DEFAULT_CONFLICT_SOLUTION;
    }

    @Override
    public final <T> T getBean(Class<T> type) {
        Map<String, T> instanceMap = getBeansOfType(type);

        // remove create by concrete
        for (String name : new HashSet<>(instanceMap.keySet())) {
            if (name.startsWith(CREATE_BY_CONCRETE))
                instanceMap.remove(name);
        }
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
//    public <T> T getBean(String getName) {
//        return null;
//    }

//    @Override
//    public <T> T getBean(Class<T> type, String getName) {
//        return null;
//    }

//    @Override
//    public <T> Map<String, T> getBeansOfType(Class<T> type) {
//        return null;
//    }
}
