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

package org.coodex.mock;

import org.coodex.util.Common;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbstractTypeMocker<A extends Annotation> implements TypeMocker<A> {

    private Class<?>[] SUPPORTED = null;

    private static Class<?> getClassFromType(Type targetType, boolean throwException) {
        if (targetType instanceof Class) {
            return (Class<?>) targetType;
        } else if (targetType instanceof ParameterizedType) {
            return getClassFromType(((ParameterizedType) targetType).getRawType(), throwException);
        } else {
            if (throwException) {
                throw new MockException("targetType is not Class: " + targetType);
            } else {
                return null;
            }
        }
    }

    protected static Class<?> getClassFromType(Type targetType) {
        return getClassFromType(targetType, true);
    }

    protected abstract Class<?>[] getSupportedClasses();

    protected abstract boolean accept(A annotation);

    @Override
    public boolean accept(A mockAnnotation, Type targetType) {
        return accept(mockAnnotation) && Common.inArray(getClassFromType(targetType, false), getSupported());
    }

    private Class<?>[] getSupported() {
        if (SUPPORTED == null) {
            SUPPORTED = getSupportedClasses();
        }
        return SUPPORTED;
    }

    @Override
    public Object mock(A mockAnnotation, Mock.Nullable nullable, Type targetType) {
        if (nullable != null) {
            Class<?> c = getClassFromType(targetType, false);
            if (c == null || !c.isPrimitive()) {
                if (Math.random() < nullable.probability()) return null;
            }
        }
        return mock(mockAnnotation, targetType);
    }


    public abstract Object mock(A mockAnnotation, Type targetType);
}
