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

import org.coodex.closure.CallableClosure;
import org.coodex.util.GenericTypeHelper;
import org.coodex.util.ServiceLoaderImpl;
import org.coodex.util.Singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Coodex默认的MockerProvider实现
 *
 * @author Davidoff
 */
public class CoodexMockerProvider implements MockerProvider {

    /**
     * 用来存放模拟检索泛型变量的上下文
     */
    private static ThreadLocal<Type> TYPE_CONTEXT = new ThreadLocal<Type>();

    /**
     * 所有的TypeMocker实例，使用单例缓存
     */
    private static Singleton<Collection<TypeMocker>> TYPE_MOCKERS = new Singleton<Collection<TypeMocker>>(
            new Singleton.Builder<Collection<TypeMocker>>() {
                @Override
                public Collection<TypeMocker> build() {
                    return new ServiceLoaderImpl<TypeMocker>() {
                    }.getAllInstances();
                }
            }
    );

    /**
     * 获取所有被{@link Mock}修饰过的注解
     * @param annotations 备选注解
     * @return 列表最后会有个null
     */
    private static List<Annotation> getMockers(Annotation... annotations) {
        List<Annotation> list = getAllDecoratedBy(Mock.class, annotations);
        list.add(null);// 当没有任何Mock修饰过的，按默认规则产生
        return list;
    }

    /**
     * 获得所有被指定注解装饰过的注解
     *
     * @param decorator   装饰器
     * @param annotations 备选
     * @return 所有被指定注解装饰过的注解，如果没有则返回size为0的List
     */
    private static List<Annotation> getAllDecoratedBy(Class<? extends Annotation> decorator, Annotation... annotations) {
        List<Annotation> list = new ArrayList<Annotation>();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getAnnotation(decorator) != null) {
                    list.add(annotation);
                }
            }
        }
        return list;
    }


    /**
     * 在备选内容中找到指定类型的注解
     *
     * @param annotationClass 需要找到的注解
     * @param annotations     备选范围
     * @param <A>             Annotation
     * @return 指定类型的注解的实例，如不存在则返回 {@code null}
     */
    private static <A extends Annotation> A getAnnotation(Class<A> annotationClass, Annotation... annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass))
                return (A) annotation;
        }
        return null;
    }


    /**
     * 根据所有模拟注解及其类型找到合适的TypeMocker实例
     * @param type 需要模拟的类型
     * @param annotations 所有被Mock修饰过的注解及{@code null}
     * @return TypeMocker实例，如果没有合适的则返回{@code null}
     */
    private static MockFacade getTypeMocker(Type type, List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            for (TypeMocker provider : TYPE_MOCKERS.getInstance()) {
                if (annotation == null || annotation.annotationType().equals(
                        GenericTypeHelper.solve(
                                TypeMocker.class.getTypeParameters()[0],
                                provider.getClass()
                        ))) {
                    if (provider.accept(annotation, type))
                        return new MockFacade(type, provider, annotation, getAnnotation(Mock.Nullable.class));
                }
            }
        }
        return null;
    }

    private Object mockClass(Class c, Annotation... annotations) {

        if (c.isArray()) {
            return mockArray(c.getComponentType(), 0, annotations);
        } else if (Collection.class.isAssignableFrom(c)) {

        } else if (Map.class.isAssignableFrom(c)) {

        }


        MockFacade param = getTypeMocker(c, getMockers(annotations));
        if (param != null) {
            return param.mock();
        }
        // todo
        return null;
    }


    @Override
    public <T> T mock(Class<T> type, Annotation... annotations) {
        TYPE_CONTEXT.set(type);
        try {
            //noinspection unchecked
            return (T) innerMock(type, annotations);
        } finally {
            TYPE_CONTEXT.remove();
        }
    }

    @Override
    public Object mock(Type type, Annotation... annotations) {
        TYPE_CONTEXT.set(type);
        try {
            return innerMock(type, annotations);
        } finally {
            TYPE_CONTEXT.remove();
        }
    }

    /**
     * @param componentType 数组元素类型
     * @param d             数组维度
     * @param annotations   注解
     * @return
     */
    private Object mockArray(Type componentType, int d, Annotation... annotations) {
        int arraySize = 0;

        return null;
    }

    private Object innerMock(final Type type, final Annotation... annotations) {

        CallableClosure closure = new CallableClosure() {
            @Override
            public Object call() {
                if (type == null) {
                    return null;
                } else if (type instanceof TypeVariable) {
                    throw new MockException("TypeVariable " + type + " not supported.");
                } else if (type instanceof Class) {
                    Class c = (Class) type;
                    if (void.class.equals(c) || Void.class.equals(c)) {
                        return null;
                    } else {
                        return mockClass(c, annotations);
                    }
                } else if (type instanceof ParameterizedType) {
                    // todo
                    return null;
                } else if (type instanceof GenericArrayType) {
                    return mockArray(((GenericArrayType) type).getGenericComponentType(), 0, annotations);
                } else {
                    throw new MockException("unsupported type : " + type);
                }
            }
        };


        try {
            return closure.call();
        } catch (Throwable throwable) {
            if (throwable instanceof MockException) {
                throw (MockException) throwable;
            } else {
                throw new MockException(throwable.getLocalizedMessage(), throwable);
            }
        }
    }

    private static class MockFacade {


        private final Type targetType;
        private final TypeMocker mocker;
        private final Annotation annotation;
        private final Mock.Nullable nullable;

        private MockFacade(Type targetType, TypeMocker mocker, Annotation annotation, Mock.Nullable nullable) {
            this.targetType = targetType;
            this.mocker = mocker;
            this.annotation = annotation;
            this.nullable = nullable;
        }

        Object mock() {
            return mocker.mock(annotation, nullable, targetType);
        }
    }
}
