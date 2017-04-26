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

import org.coodex.concrete.api.ConcreteService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-09-01.
 */
public class DefinitionContext {

    private Class<? extends ConcreteService> declaringClass;
    private Method declaringMethod;

    DefinitionContext() {
    }

    void setDeclaringClass(Class<? extends ConcreteService> declaringClass) {
        this.declaringClass = declaringClass;
    }

    void setDeclaringMethod(Method declaringMethod) {
        this.declaringMethod = declaringMethod;
    }

    /**
     * BizModule定义的class
     *
     * @return
     */
    public Class<? extends ConcreteService> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * 定义了BizUnit的method
     *
     * @return
     */
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    public String getModuleName() {
        return ConcreteHelper.getServiceName(getDeclaringClass());
    }

    public String getMethodName() {
        return ConcreteHelper.getMethodName(getDeclaringMethod());
    }


    public <T extends Annotation> T getDeclaringAnnotation(Class<T> annotaionClass) {
        T annotation = declaringMethod == null ? null : declaringMethod.getAnnotation(annotaionClass);
        if (annotation == null) {
            return declaringClass.getAnnotation(annotaionClass);
        }
        return annotation;
    }

}
