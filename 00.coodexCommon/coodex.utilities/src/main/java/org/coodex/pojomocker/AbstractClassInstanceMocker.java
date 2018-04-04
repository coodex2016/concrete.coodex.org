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

/**
 *
 */
package org.coodex.pojomocker;

import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;

/**
 * 模拟指定class的实例，需要实现的类:<br>
 * List + Collection, Set, String, PrimitiveClass<br>
 * SimplePOJO
 *
 * @author davidoff
 */
@Deprecated
public abstract class AbstractClassInstanceMocker {

    private static Logger log = LoggerFactory
            .getLogger(AbstractClassInstanceMocker.class);

    /**
     * 当未指定mockInfo时的默认属性。可由各实现类重载以改变各自的默认属性
     *
     * @return
     */
    public POJOMockInfo getDefaultMockInfo() {
        return new POJOMockInfo();
    }

    /**
     * 是否接受指定的类型
     *
     * @param clz
     * @return
     */
    protected abstract boolean access(Class<?> clz);

    /**
     * 是否需要创建。为防止A.B.A的无限循环，MockInfo中定义了最大循环层数maxRecycledCount，需要结合此属性共同判定
     *
     * @param created 在属性从属关系栈中，当前class已创建过的数量
     * @param context
     * @return
     */
    protected abstract boolean needCreate(int created, MockContext context);

    /**
     * 是否需要mock该类新的属性？
     *
     * @param clz
     * @return
     */
    protected abstract boolean needMockFields(Class<?> clz);

    /**
     * 构建该类型的实例，但不包括给field设定值
     *
     * @param clz
     * @param context
     * @return
     * @throws UnsupportedTypeException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected abstract <T> T newInstance(Class<T> clz, MockContext context)
            throws UnsupportedTypeException, IllegalAccessException,
            IllegalArgumentException, UnableMockException;

    /**
     * 根据上下文创建指定类型的实例
     *
     * @param clz
     * @param context
     * @return
     * @throws IllegalAccessException
     * @throws UnsupportedTypeException
     * @throws IllegalArgumentException
     * @throws UnableMockException
     */
    public final <T> T mockInstance(Class<T> clz, MockContext context,
                                    Type[] param) throws IllegalAccessException, IllegalArgumentException,
            UnsupportedTypeException, UnableMockException {
        Integer created = context.getCreated().get(clz);

        if (created == null)
            created = Integer.valueOf(0);

        if (needCreate(created, context)) {
            T instance = newInstance(clz, context);
            if (instance != null) {
                MockContextHelper.enter();
                try {
                    MockContext current = MockContextHelper.currentContext();
                    current.addContextType(clz);
                    // current.setInstance(instance);
                    current.addCreatedCount(clz);

                    if (needMockFields(clz)) {
                        mockFields(clz, instance, param);
                    }

                    // Collection support
                    mockCollection(clz, instance);

                    // Map support
                    mockMap(clz, instance);

                } finally {
                    MockContextHelper.leave();
                }
            }

            return instance;
        } else
            return null;
    }

    @SuppressWarnings("unchecked")
    private <T> void mockMap(Class<T> clz, T instance)
            throws IllegalAccessException, IllegalArgumentException,
            UnsupportedTypeException, UnableMockException {

        if (!clz.isInterface() && (clz.getModifiers() & Modifier.ABSTRACT) == 0
                && Map.class.isAssignableFrom(clz)) {

            MockContext current = MockContextHelper.currentContext();
            TypeVariable<?> t0 = Map.class.getTypeParameters()[0];
            TypeVariable<?> t1 = Collection.class.getTypeParameters()[1];
            @SuppressWarnings("rawtypes")
            Map map = (Map) instance;
            Type keyType = TypeHelper.findActualClassFromInstanceClass(
                    (TypeVariable<Class<?>>) t0, clz);
            Type elementType = TypeHelper.findActualClassFromInstanceClass(
                    (TypeVariable<Class<?>>) t1, clz);

            POJOMockInfo pmi = current.getMockInfo();
            int randomSize = Common.random(Math.max(1, pmi.getMin()),
                    pmi.getMax());

            for (int i = 0; i < randomSize; i++) {
                MockContextHelper.enter();
                try {
                    Object key = POJOMocker.mock(keyType);
                    Object element = POJOMocker.mock(elementType);
                    log.debug("======= {}, {}", key, element);
                    map.put(key, element);
                } finally {
                    MockContextHelper.leave();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void mockCollection(Class<T> clz, T instance)
            throws IllegalAccessException, UnableMockException,
            UnsupportedTypeException {

        if (!clz.isInterface() && (clz.getModifiers() & Modifier.ABSTRACT) == 0
                && Collection.class.isAssignableFrom(clz)) {

            MockContext current = MockContextHelper.currentContext();
            TypeVariable<?> tv = Collection.class.getTypeParameters()[0];
            @SuppressWarnings("rawtypes")
            Collection collection = (Collection) instance;
            Type elementType = TypeHelper.findActualClassFromInstanceClass(
                    (TypeVariable<Class<?>>) tv, clz);

            POJOMockInfo pmi = current.getMockInfo();
            int randomSize = Common.random(Math.max(1, pmi.getMin()),
                    pmi.getMax());

            for (int i = 0; i < randomSize; i++) {
                MockContextHelper.enter();
                try {
                    Object element = POJOMocker.mock(elementType);
                    collection.add(element);
                } finally {
                    MockContextHelper.leave();
                }
            }
        }
    }

    private <T> void mockFields(Class<T> clz, T instance, Type[] param)
            throws IllegalAccessException, UnsupportedTypeException,
            UnableMockException {

        Field[] fields = ReflectHelper.getAllDeclaredFields(clz,
                ReflectHelper.ALL_OBJECT_EXCEPT_JDK);
        log.debug("instance created: [{}, {}]", clz.getName(), instance);

        for (Field field : fields) {
            if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0)
                continue;
            MockContextHelper.enter();
            try {
                MockContext current = MockContextHelper.currentContext();

                POJOMock pm = field.getAnnotation(POJOMock.class);
                POJOMockInfo pmi = getDefaultMockInfo();
                if (pm != null)
                    pmi = new POJOMockInfo(pm);
                current.setMockInfo(pmi);

                field.setAccessible(true);
                if (pmi.isForceMock() || field.get(instance) == null) {
                    Type t = field.getGenericType();

                    log.debug("mocking field: {} {}", field.getGenericType(),
                            field.getName());
                    Object fieldValue = null;
                    if (t instanceof TypeVariable && param != null) {
                        TypeVariable<?> $t = (TypeVariable<?>) t;
                        Type[] types = $t.getGenericDeclaration().getTypeParameters();
                        Type _t = null;
                        for (int i = 0; i < types.length; i++) {
                            if (types[i] == t)
                                _t = param[i];
                        }
                        if (_t == null)
                            throw new UnsupportedTypeException(t);

                        fieldValue = POJOMocker.$mock(_t);
//               } else if (toTypeReference instanceof ParameterizedType) {
//                  MockContextHelper.enter();
//                  try {
//                     MockContext mc = MockContextHelper.currentContext();
//                     ParameterizedType pt = (ParameterizedType) toTypeReference;
//                     Type[] types = pt.getActualTypeArguments();
//                     Class<?> declared = (Class<?>) pt.getRawType();
//                     for (int i = 0; i < types.length; i++) {
//                        mc.addReplace(declared, i, types[i]);
//                     }
//                     fieldValue = POJOMocker.$mock(toTypeReference);
//                  } finally {
//                     MockContextHelper.leave();
//                  }
                    } else
                        fieldValue = POJOMocker.$mock(t);
                    field.set(instance, fieldValue);
                }
            } finally {
                MockContextHelper.leave();
            }
        }
    }
}
