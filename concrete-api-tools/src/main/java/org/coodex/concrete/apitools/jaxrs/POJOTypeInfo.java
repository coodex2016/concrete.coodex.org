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

package org.coodex.concrete.apitools.jaxrs;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import static org.coodex.util.GenericTypeHelper.solveFromType;

/**
 * Created by davidoff shen on 2016-12-04.
 */
public class POJOTypeInfo {

    private static final Class<?> ARRAY_CLASS = (new Object[0]).getClass();
    private static final POJOTypeInfo OBJECT_POJO_INFO = new POJOTypeInfo(Object.class, Object.class);

    private final Class<?> contextType;

    private final Type genericType;

    private final Class<?> type;

    private POJOTypeInfo arrayElement = OBJECT_POJO_INFO;
    private List<POJOTypeInfo> genericParameters = new ArrayList<POJOTypeInfo>();

    public POJOTypeInfo(Class<?> contextType, Type genericType) {
        this.contextType = contextType;
        this.genericType = genericType;
        this.type = loadClass();
    }

    public Class<?> getContextType() {
        return contextType;
    }

    private Class<?> loadClass() {
        return $loadClass(genericType);
    }

    private Class<?> $loadClass(Type genericType) {
        if (genericType instanceof GenericArrayType) {
            arrayElement = new POJOTypeInfo(contextType, ((GenericArrayType) genericType).getGenericComponentType());
            return ARRAY_CLASS;
        } else if (genericType instanceof ParameterizedType) {
            Class<?> clz = (Class<?>) ((ParameterizedType) genericType).getRawType();
            for (Type t : ((ParameterizedType) genericType).getActualTypeArguments()) {
                genericParameters.add(new POJOTypeInfo(contextType, t));
            }
            return clz;
        } else if (genericType instanceof Class) {
            if (((Class) genericType).isArray()) {
                arrayElement = new POJOTypeInfo(contextType, ((Class) genericType).getComponentType());
                return ARRAY_CLASS;
            }
            return (Class<?>) genericType;
        } else if (genericType instanceof TypeVariable) {
            return $loadClass(solveFromType((TypeVariable) genericType, contextType));
        }
        throw new RuntimeException("unknown Type: " + genericType + ". genericType: " + this.genericType
                + ": instanceClass:" + contextType);
    }

    public Type getGenericType() {
        return genericType;
    }

    public Class<?> getType() {
        return type;
    }

    public POJOTypeInfo getArrayElement() {
        return arrayElement;
    }

    public List<POJOTypeInfo> getGenericParameters() {
        return genericParameters;
    }
}
