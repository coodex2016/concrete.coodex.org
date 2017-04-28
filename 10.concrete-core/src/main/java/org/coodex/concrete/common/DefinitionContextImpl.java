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
import org.coodex.concrete.api.Overlay;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by davidoff shen on 2016-09-01.
 */
public class DefinitionContextImpl implements DefinitionContext {

    private Class<? extends ConcreteService> declaringClass;
    private Method declaringMethod;

    DefinitionContextImpl() {
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
    @Override
    public Class<? extends ConcreteService> getDeclaringClass() {
        return declaringClass;
    }

    /**
     * 定义了BizUnit的method
     *
     * @return
     */
    @Override
    public Method getDeclaringMethod() {
        return declaringMethod;
    }

    @Override
    public String getModuleName() {
        return ConcreteHelper.getServiceName(getDeclaringClass());
    }


    /**
     * @param annotationClass
     * @param <T>
     * @return
     */
    @Override
    public final <T extends Annotation> T getDeclaringAnnotation(Class<T> annotationClass) {
        return getDeclaringMethod().getAnnotation(annotationClass);
    }

    /**
     * 优先级：
     * annotationClass不可覆盖时：
     * - method
     * - method.declaringClass
     * - moduleClass
     * <p>
     * annotationClass可覆盖时:
     * - method
     * - moduleClass
     * - method.declaringClass
     *
     * @param annotationClass
     * @param <T>
     * @return
     */
    @Override
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        boolean overlay = annotationClass.getAnnotation(Overlay.class) != null;
        T annotation = declaringMethod == null ? null : declaringMethod.getAnnotation(annotationClass);
        if (annotation == null) {
            for (Class<?> c : overlay ?
                    Arrays.asList(declaringClass, declaringMethod.getDeclaringClass()) :
                    Arrays.asList(declaringMethod.getDeclaringClass(), declaringClass)) {
                annotation = c.getAnnotation(annotationClass);
                if (annotation != null) break;
            }
        }
        return annotation;
    }


}
