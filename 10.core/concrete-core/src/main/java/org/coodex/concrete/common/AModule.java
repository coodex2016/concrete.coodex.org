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

import org.coodex.concrete.common.struct.AbstractModule;
import org.coodex.util.SingletonMap;

import java.lang.reflect.Method;
import java.util.List;

public class AModule extends AbstractModule<AUnit> {

    //    private static final Map<Class, AModule> modules = new ConcurrentHashMap<Class, AModule>();
    private static final SingletonMap<Class, AModule> modules = new SingletonMap<Class, AModule>(
            new SingletonMap.Builder<Class, AModule>() {
                @Override
                public AModule build(Class key) {
                    return new AModule(key);
                }
            }
    );

    private static final AModule getModule(Class clz) {
//        if (!modules.containsKey(clz)) {
//            synchronized (AModule.class) {
//                if (!modules.containsKey(clz)) {
//                    modules.put(clz, new AModule(clz));
//                }
//            }
//        }
//        return modules.get(clz);
        return modules.getInstance(clz);
    }

    public static AUnit getUnit(Class clz, Method method) {
        AModule localModule = getModule(clz);
        for (AUnit unit : localModule.getUnits()) {
            if (unit.getMethod().equals(method)) return unit;
        }
        throw new RuntimeException("no method found." + clz.getName() + "::" + method.getName());
    }

    public AModule(Class<?> interfaceClass) {
        super(interfaceClass);
    }

    @Override
    public String getName() {
        return getInterfaceClass().getName();
    }

    @Override
    protected AUnit[] toArrays(List<AUnit> localUnits) {
        return localUnits.toArray(new AUnit[0]);
    }

    @Override
    protected AUnit buildUnit(Method method) {
        return new AUnit(method, this);
    }

    @Override
    public int compareTo(AbstractModule o) {
        return 0;
    }
}
