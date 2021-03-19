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

package org.coodex.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by davidoff shen on 2017-05-12.
 */
public class PojoProperty {
    private final Method method;
    private final Field field;
    private final boolean readonly;
    private final Type type;
    private final String name;

    private Annotation[] annotations = null;

    protected PojoProperty(PojoProperty property, Type type) {
        this.method = property == null ? null : property.getMethod();
        this.field = property == null ? null : property.getField();
        this.readonly = property != null && property.isReadonly();
        this.type = type;
        this.name = property == null ? null : property.getName();
    }

    protected PojoProperty(Field field, Type type) {
        this(null, field, Modifier.isFinal(field.getModifiers()), type, field.getName());
    }

    protected PojoProperty(Method method, Field field, boolean readonly, Type type, String name) {
        this.method = method;
        this.field = field;
        this.readonly = readonly;
        this.type = type;
        this.name = name;
    }

    public Method getMethod() {
        return method;
    }

    public Field getField() {
        return field;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == null) return null;
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().equals(annotationClass))
                return Common.cast(annotation);
        }
        return null;
//        T annotation = method == null ? null : method.getAnnotation(annotationClass);
//        return field == null ? annotation : field.getAnnotation(annotationClass);
    }

    public Annotation findDecoratedBy(Class<? extends Annotation> decoratedClass) {
        if (decoratedClass == null) return null;
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().getAnnotation(decoratedClass) != null) {
                return annotation;
            }
        }
        return null;
    }

    public List<Annotation> findAllDecoratedBy(Class<? extends Annotation> decoratedClass) {
        if (decoratedClass == null) return null;
        List<Annotation> annotationList = new ArrayList<>();
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().getAnnotation(decoratedClass) != null) {
//                return annotation;
                annotationList.add(annotation);
            }
        }
        return annotationList.size() > 0 ? annotationList : null;
    }

    public Annotation[] getAnnotations() {
        synchronized (this) {
            if (annotations == null) {
                List<Annotation> annotationList = new ArrayList<>();
                if (method != null) {
                    annotationList.addAll(Arrays.asList(method.getAnnotations()));
                }
                if (field != null) {
                    annotationList.addAll(Arrays.asList(field.getAnnotations()));
                }
                annotations = annotationList.toArray(new Annotation[0]);
            }
        }
        return annotations;
    }

    @Override
    public String toString() {
        return "PojoProperty{" +
                "method=" + method +
                ", field=" + field +
                ", readonly=" + readonly +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", annotations=" + Arrays.toString(annotations) +
                '}';
    }
}
