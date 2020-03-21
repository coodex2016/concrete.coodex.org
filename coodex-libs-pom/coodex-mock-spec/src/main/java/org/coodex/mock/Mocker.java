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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


public final class Mocker {

    private static final String DEFAULT_PROVIDER_CLASS = Mocker.class.getPackage().getName() + ".CoodexMockerProvider";
    private static boolean instanceLoaded = false;
    private static MockerProvider mockerProviderInstance = null;
    private static Throwable th = null;

    private Mocker() {
    }

    private static MockerProvider getMockerProvider() {
        if (!instanceLoaded) {
            synchronized (Mocker.class) {
                if (!instanceLoaded) {
                    try {
                        Iterator<MockerProvider> serviceLoader = ServiceLoader.load(MockerProvider.class).iterator();
                        if (serviceLoader.hasNext()) {
                            mockerProviderInstance = serviceLoader.next();
                        } else {
                            mockerProviderInstance = (MockerProvider) Class.forName(DEFAULT_PROVIDER_CLASS).newInstance();
                        }
                    } catch (Throwable throwable) {
                        th = throwable;
                    } finally {
                        instanceLoaded = true;
                    }
                }
            }
        }

        if (mockerProviderInstance == null) {
            if (th == null)
                throw new MockException("none provider found.");
            else
                throw new MockException("none provider found. ", th);
        }
        return mockerProviderInstance;
    }

    public static <T> T mock(Class<T> type, Annotation... annotations) {
        return getMockerProvider().mock(type, annotations);
    }

    public static Object mock(Type type, Type context, Annotation... annotations) {
        return getMockerProvider().mock(type, context, annotations);
    }

    @SuppressWarnings("unused")
    public static Object mockMethod(Method method) {
        return mockMethod(method, method.getDeclaringClass());
    }

    public static Object mockMethod(Method method, Type instanceType) {
        return mock(
                method.getGenericReturnType(),
                instanceType,
                merge(method.getAnnotations(),
                        getTypeAnnotations(instanceType)));
    }

    public static Annotation[] getTypeAnnotations(Type instanceType) {
        Class<?> contextClass = null;
        if (instanceType instanceof Class) {
            contextClass = (Class<?>) instanceType;
        } else if (instanceType instanceof ParameterizedType) {
            contextClass = (Class<?>) ((ParameterizedType) instanceType).getRawType();
        }
        return contextClass == null ? null : contextClass.getAnnotations();
    }

    private static Annotation[] merge(Annotation[]... annotations) {
        List<Annotation> list = new ArrayList<>();
        if (annotations != null) {
            for (Annotation[] array : annotations) {
                if (array != null && array.length > 0) {
                    list.addAll(Arrays.asList(array));
                }
            }
        }
        return list.toArray(new Annotation[0]);
    }

    @SuppressWarnings("unused")
    public static Object mockParameter(Method method, int index) {
        return mockParameter(method, index, method.getDeclaringClass());
    }

    public static Object mockParameter(Method method, int index, Type instanceType) {
        return mock(
                method.getGenericParameterTypes()[index],
                instanceType,
                merge(
                        method.getParameterAnnotations()[index],
                        method.getAnnotations(),
                        getTypeAnnotations(instanceType))
        );
    }

}
