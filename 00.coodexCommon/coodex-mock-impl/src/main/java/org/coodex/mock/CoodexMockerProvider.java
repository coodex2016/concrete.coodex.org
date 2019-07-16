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

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

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
     * 获取所有被{@link Mock}修饰过的注解
     * @param annotations 备选注解
     * @return 列表最后会有个null
     */
    private static List<Annotation> getMockers(Annotation... annotations) {
        List<Annotation> list = new ArrayList<Annotation>();
        if (annotations != null && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getAnnotation(Mock.class) != null) {
                    list.add(annotation);
                }
            }
        }
        list.add(null);// 当没有任何Mock修饰过的，按默认规则产生
        return list;
    }

    /**
     * 根据所有模拟注解及其类型找到合适的Mocker实例
     * @param type 需要模拟的类型
     * @param annotations 所有被Mock修饰过的注解及{@code null}
     * @return Mocker实例，如果没有合适的则返回{@code null}
     */
    private static Mocker getMocker(Type type, List<Annotation> annotations){
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

    private Object mockClass(Class c, Annotation... annotations) {
        // todo
        return null;
    }

    private Object innerMock(final Type type, final Annotation... annotations) {
        CallableClosure closure = new CallableClosure() {
            @Override
            public Object call() throws Throwable {
                if (type == null) {
                    return null;
                } else if (type instanceof TypeVariable) {
                    throw new MockException("TypeVariable " + type + " not supported.");
                } else if (type instanceof Class) {
                    Class c = (Class) type;
                    if (c.isArray()) {
                        return mockArray(c.getComponentType(), 0, annotations);
                    } else if (void.class.equals(c) || Void.class.equals(c)) {
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
}
