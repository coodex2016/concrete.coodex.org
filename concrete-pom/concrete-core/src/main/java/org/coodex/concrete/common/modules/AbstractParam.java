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
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static org.coodex.util.Common.camelCase;
import static org.coodex.util.Common.cast;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public abstract class AbstractParam implements Annotated, Documentable {

    private final ReflectHelper.MethodParameter parameter;
    private final Description description;

    public AbstractParam(Method method, int index) {
        this.parameter = new ReflectHelper.MethodParameter(method, index);
        this.description = getDeclaredAnnotation(Description.class);
    }


    @Override
    public String getLabel() {
        return description == null ? "" : description.name();
//        return Common.isBlank(s) ? "　" : s;
    }

    @Override
    public String getDescription() {
        return description == null ? "" : description.description();
//        return Common.isBlank(s) ? "　" : s;
    }

    /**
     * @return 参数类型
     */
    public Class<?> getType() {
        return parameter.getType();
    }


    @Override
    public Annotation[] getAnnotations() {
        return parameter.getAnnotations();
    }

    /**
     * @return 参数泛型类型
     */
    public Type getGenericType() {
        return parameter.getGenericType();
    }


    /**
     * @return 参数名
     */
    public String getName() {
        return camelCase(parameter.getName());
    }


    /**
     * @return 参数索引号
     */
    public int getIndex() {
        return parameter.getIndex();
    }

    @Override
    public <T extends Annotation> T getDeclaredAnnotation(Class<T> annotationClass) {
        Target target = annotationClass.getAnnotation(Target.class);
        if (target != null && Common.inArray(ElementType.ANNOTATION_TYPE, target.value())) {
            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotation.annotationType().equals(annotationClass))
                    return cast(annotation);
                Annotation x = annotation.annotationType().getAnnotation(annotationClass);
                if (x != null) return cast(x);

            }

            return null;
        } else {
            return parameter.getAnnotation(annotationClass);
        }
    }

}
