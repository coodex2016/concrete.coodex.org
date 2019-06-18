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

package org.coodex.concrete.common.modules;

import org.coodex.concrete.api.Description;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.coodex.concrete.common.ConcreteHelper.isConcreteService;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class AbstractModule<UNIT extends AbstractUnit> implements Annotated, Comparable<AbstractModule> {

    private Class<?> interfaceClass;

    private List<UNIT> units = new ArrayList<UNIT>();

    public AbstractModule(Class<?> interfaceClass) {
        //ConcreteService.class.isAssignableFrom(interfaceClass)
        //                && interfaceClass.getAnnotation(MicroService.class) != null
        //                && interfaceClass.getAnnotation(Abstract.class) == null
        if (isConcreteService(interfaceClass)) {
            this.interfaceClass = interfaceClass;
        } else {
            throw new RuntimeException(interfaceClass.getName() + " is NOT a concrete ConcreteService");
        }

        for (Method method : interfaceClass.getMethods()) {
            if (Object.class.equals(method.getDeclaringClass())) continue;

            //method.getAnnotation(NotService.class) == null
            if (isConcreteService(method)) {
                UNIT unit = buildUnit(method);
                if (unit != null)
                    units.add(unit);
            }
        }
    }

//    /**
//     * 业务模块继承链
//     *
//     * @return
//     */
//    public abstract List<Class<?>> getInheritedChain();

    /**
     * 服务模块所在的class
     *
     * @return
     */
    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

//    /**
//     * 服务模块的角色属主
//     *
//     * @return
//     */
//    public Domain getDomain() {
//        return getDeclaredAnnotation(Domain.class);
//    }

    private Description getDesc() {
        return getDeclaredAnnotation(Description.class);
    }

    /**
     * 服务模块名称
     *
     * @return
     */
    public abstract String getName();

    /**
     * 文档化的标题
     *
     * @return
     */
    public String getLabel() {
        return getDesc() == null ? getName() : getDesc().name();
    }

    /**
     * 服务模块说明
     *
     * @return
     */
    public String getDescription() {
        return getDesc() == null ? null : getDesc().description();
    }

    /**
     * 所有的服务原子
     *
     * @return
     */
    public final UNIT[] getUnits() {
        return toArrays(units);
    }


    protected abstract UNIT[] toArrays(List<UNIT> units);

    protected abstract UNIT buildUnit(Method method);

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        return interfaceClass.getAnnotation(annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return getDeclaredAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return interfaceClass.getAnnotations();
    }
}
