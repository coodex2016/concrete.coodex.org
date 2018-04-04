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

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.api.Overlay;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Created by davidoff shen on 2016-09-06.
 */
public class RuntimeContext extends DefinitionContextImpl {

    private RuntimeContext() {
    }

    @SuppressWarnings("unchecked")
    public static final RuntimeContext getRuntimeContext(Method method, Class<?> clz) {
        DefinitionContext context = ConcreteHelper.getContext(method, clz);
        if (context == null) return null;
        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setDeclaringMethod(context.getDeclaringMethod());
        runtimeContext.setDeclaringClass(context.getDeclaringClass());
        runtimeContext.setActualClass((Class<? extends ConcreteService>) clz);
        // find actual method
        runtimeContext.setActualMethod(method);
        return runtimeContext;
    }

    private Class<? extends ConcreteService> actualClass;

    private Method actualMethod;

    public Class<? extends ConcreteService> getActualClass() {
        return actualClass;
    }


    public Method getActualMethod() {
        return actualMethod;
    }

    void setActualClass(Class<? extends ConcreteService> actualClass) {
        this.actualClass = actualClass;
    }

    void setActualMethod(Method actualMethod) {
        this.actualMethod = actualMethod;
    }


    /**
     * 获取运行期定义的Annotaion
     *
     * @param annotationClass
     * @param <T>
     * @return
     */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Overlay overlayAnnotation = annotationClass.getAnnotation(Overlay.class);
        boolean justDefinition = overlayAnnotation == null ? true : overlayAnnotation.definition();

        T annotation = justDefinition ?
                getDeclaringMethod().getAnnotation(annotationClass) :
                getAnnotationFrom(annotationClass, new AnnotatedElement[]{
                        actualMethod, getDeclaringMethod(), actualClass
                });


//        if (annotation == null) {
//            annotation = justDefinition ?
//                    super.getAnnotation(annotationClass) :
//                    getAnnotationFromImpl(annotationClass);
//        }
//        annotation = justDefinition ? annotation : getAnnotationFromImpl(annotationClass);
//        return annotation == null ? super.getAnnotation(annotationClass) : annotation;
        return annotation == null ? super.getAnnotation(annotationClass) : annotation;
    }



    private <T extends Annotation> T getAnnotationFromImpl(Class<T> annotationClass) {
        T annotation = actualMethod.getAnnotation(annotationClass);
        return annotation == null ? actualClass.getAnnotation(annotationClass) : annotation;
    }
}
