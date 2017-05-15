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

package org.coodex.pojomocker;

import org.coodex.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static org.coodex.util.ReflectHelper.getParameterName;
import static org.coodex.util.TypeHelper.toTypeReference;


/**
 * Created by davidoff shen on 2017-05-13.
 */
public class MockerFacade {

    private final static Logger log = LoggerFactory.getLogger(MockerFacade.class);


    private static final PojoBuilder DEFAULT_BUILDER = new PojoBuilderImpl();

    private static final PojoBuilder POJO_BUILDER = new ServiceLoaderFacade<PojoBuilder>() {
        @Override
        public PojoBuilder getDefaultProvider() {
            return DEFAULT_BUILDER;
        }
    }.getInstance();

    private static final DefaultMockers DEFAULT_MOCKER = new DefaultMockers();

    static final AcceptableServiceLoader<Annotation, Mocker<Annotation>> MOCKER_LOADER =
            new AcceptableServiceLoader<Annotation, Mocker<Annotation>>(
                    new ServiceLoaderFacade<Mocker<Annotation>>() {
                        @Override
                        public Mocker<Annotation> getDefaultProvider() {
                            return DEFAULT_MOCKER;
                        }
                    }
            );


    public static <T> T mock(GenericType<T> genericType) {
        return mock(genericType, null);
    }

    public static <T> T mock(GenericType<T> genericType, Class context) {
        return mock(genericType.genericType(context));
    }

    public static <T> T mock(Type type) {
        return mock(type, null);
    }

    public static <T> T mock(Type type, Class... context) {
        type = toTypeReference(type, context);
        if (type instanceof TypeVariable) {
            throw new RuntimeException(typeVariableInfo(type, context));
        } else {
            return $mock(type, null, 0, null, context);
        }
    }

    private static String typeVariableInfo(Type type, Class... context) {
        StringBuilder builder = new StringBuilder("TypeVariable is NOT supported.[");
        builder.append(type).append(" declared in ");
        GenericDeclaration declaration = ((TypeVariable) type).getGenericDeclaration();
        if (declaration instanceof Class) {
            builder.append(((Class) declaration).getName());
        } else if (declaration instanceof Method) {
            builder.append(executableDeclaration((Method) declaration, context));

        } else if (declaration instanceof Constructor) {
            builder.append(executableDeclaration((Constructor) declaration, context));
        }
        return builder.append(']').toString();
    }

    private static String executableDeclaration(Method method, Class... context) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getName())
                .append(".").append(method.getName()).append('(');
        int i = 0;
        for (Type t : method.getGenericParameterTypes()) {
            if (i > 0) builder.append(", ");
            builder.append(toTypeReference(t, context))
                    .append(' ').append(getParameterName(method, i, "arg"));
        }
        return builder.append(')').toString();
    }

    private static String executableDeclaration(Constructor constructor, Class... context) {
        StringBuilder builder = new StringBuilder();
        builder.append(constructor.getDeclaringClass().getName()).append('(');
        int i = 0;
        for (Type t : constructor.getGenericParameterTypes()) {
            if (i > 0) builder.append(", ");
            builder.append(toTypeReference(t, context))
                    .append(' ').append(getParameterName(constructor, i, "arg"));
        }
        return builder.append(')').toString();
    }

    private static <T> T $mock(Type type, PojoProperty property, int dimension, Stack<String> stack, Type... context) {
        if (type == null) return null;

        if (type instanceof ParameterizedType) {
            return mockParameterizedType((ParameterizedType) type, property, dimension,stack, context);
//            return mockPojo(getPojoInfo(type, context), property, stack, context);
//            return mockClass((Class) ((ParameterizedType) type).getRawType(), property, dimension, stack, context);
        } else if (type instanceof GenericArrayType) {
            return mockGenericArray((GenericArrayType) type, property, dimension, stack, context);
        } else if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz.isArray()) {
                return mockArray(clazz, property, dimension, stack, context);
            } else {
                return mockClass(clazz, property, dimension, stack, context);
            }
        }
        return null;
    }

    private static final <T> T mockParameterizedType(
            ParameterizedType type, PojoProperty property, int dimension, Stack<String> stack, Type... context) {
        Class c = (Class) type.getRawType();
        if (Collection.class.isAssignableFrom(c)) {
            return (T) mockCollection(c, type.getActualTypeArguments()[0], property, dimension, stack, context);
        } else if (Map.class.isAssignableFrom(c)) {
            // TODO mock Map
        }
        return mockPojo(new PojoInfo(type, context), property, stack, context);
    }

    private static final <T> T mockGenericArray(
            GenericArrayType type, PojoProperty property, int dimension, Stack<String> stack, Type... context) {

        Type componentType = toTypeReference(type.getGenericComponentType(), context);
        int size = getArraySize(property, dimension);
        T array = (T) Array.newInstance(getComponentClass(componentType, context), size);
        for (int i = 0; i < size; i++) {
            if (componentType instanceof GenericArrayType) {
                Array.set(array, i, mockGenericArray(
                        (GenericArrayType) componentType, property,
                        dimension + 1, stack, context));
            } else if (componentType instanceof ParameterizedType) {
                Array.set(array, i, mockParameterizedType((ParameterizedType) componentType,
                        property, dimension + 1, stack, context));
            } else {
                throw new RuntimeException("unsupported component type: " + componentType.toString());
            }
        }
        return array;
    }

    public static final Class getComponentClass(Type componentType, Type... context) {
        if (componentType == null || componentType instanceof TypeVariable) {
            return Object.class;
        } else if (componentType instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) componentType).getRawType();
        } else if (componentType instanceof Class) {
            return (Class) componentType;
        } else if (componentType instanceof GenericArrayType) {
            return Array.newInstance(
                    getComponentClass(((GenericArrayType) componentType).getGenericComponentType(), context), 0)
                    .getClass();
        }
        return null;
    }


    private static final Annotation getAnnotation(PojoProperty property) {
        if (property == null) return null;
        for (Annotation annotation : property.getAnnotations()) {
            if (annotation != null &&
                    annotation.annotationType().getAnnotation(Mock.class) != null) {
                return annotation;
            }
        }
        return null;
    }

    private static final <T> T mockClass(
            Class clazz, PojoProperty property, int dimension, Stack<String> stack, Type... context) {


        if (Collection.class.isAssignableFrom(clazz)) {
            return (T) mockCollection(clazz, null, property, dimension, stack, context);
        } else if (Map.class.isAssignableFrom(clazz)) {
            // TODO map
            return null;
        } else if (TypeHelper.isPrimitive(clazz)) {
            Annotation annotation = getAnnotation(property);
            return (T) MOCKER_LOADER.getServiceInstance(annotation).mock(annotation, clazz);
        } else {
            Annotation annotation = getAnnotation(property);
            if (annotation != null) {
                return (T) MOCKER_LOADER.getServiceInstance(annotation).mock(annotation, clazz);
            } else
                return mockPojo(new PojoInfo(clazz, context), property, stack, context);
        }
    }

    private static final <T extends Collection> T mockCollection(
            Class<? extends Collection> collectionClass,
            Type componentType,
            PojoProperty property,
            int dimension,
            Stack<String> stack,
            Type... context) {

        Collection collection = null;
        if (List.class.equals(collectionClass)) {
            collection = new ArrayList();
        } else if (Set.class.equals(collectionClass)) {
            collection = new HashSet();
        } else {
            try {
                collection = collectionClass.newInstance();
            } catch (Throwable th) {
                throw new RuntimeException("unable init collection: " + th.getLocalizedMessage(), th);
            }
        }
        if (componentType == null) componentType = Object.class;

        for (int i = 0, size = getArraySize(property, dimension); i < size; i++) {
            collection.add($mock(toTypeReference(componentType, context), property, dimension + 1, stack, context));
        }

        return (T) collection;
    }

//    private static final Type getComponentType(Type componentType, PojoProperty pojoProperty){
//        if(componentType == null) componentType = void.class;
//        if(pojoProperty != null){
//            COLLECTION collection = pojoProperty.getAnnotation(COLLECTION.class);
//            if
//        }
//    }

    private static final Map<String, PojoInfo> POJO_INFO_MAP = new HashMap<String, PojoInfo>();

    private static PojoInfo getPojoInfo(Type type, Type... context) {
        synchronized (POJO_INFO_MAP) {
            type = toTypeReference(type, context);
            if (!POJO_INFO_MAP.containsKey(type.toString())) {
                POJO_INFO_MAP.put(type.toString(), new PojoInfo(type, context));
            }
        }
        return POJO_INFO_MAP.get(type.toString());
    }

    private static <T> T mockPojo(PojoInfo pojoInfo, PojoProperty property, Stack<String> stack, Type... context) {
        if (stack == null)
            stack = new Stack<String>();
        stack.push(pojoInfo.getType().toString());
        try {
            // 达到指定层次时返回空值
            int deep = getDeep(property);
            String pojoType = pojoInfo.getType().toString();
            for (String s : stack) {
                if (pojoType.equals(s)) {
                    if (--deep < 0) return null;
                }
            }

            Object instance = POJO_BUILDER.newInstance(pojoInfo);
            for (PojoProperty pojoProperty : pojoInfo.getProperties()) {
                try {
                    // TODO relation
                    POJO_BUILDER.set(instance, pojoProperty,
                            $mock(pojoProperty.getType(), pojoProperty, 0, stack, context));
                } catch (Throwable th) {
                    String message = new StringBuilder("Cannot set property: ")
                            .append(pojoProperty.getName())
                            .append(", caused by: ")
                            .append(th.getLocalizedMessage()).toString();

                    if ("warn".equalsIgnoreCase(System.getProperty(Mock.POLICY_KEY))) {
                        log.warn("{}", message, th);
                    } else {
                        throw new RuntimeException(message, th);
                    }
                }
            }
            return (T) instance;
        } finally {
            stack.pop();
        }
    }

    private static int getDeep(PojoProperty property) {
        int deepMin = 1, deepMax = 5;
        if (property != null) {
            Deep deep = property.getAnnotation(Deep.class);
            if (deep != null) {
                deepMin = Math.max(1, deepMin);
                deepMax = Math.max(deep.max(), deepMin);
            }
        }
        return Common.random(deepMin, deepMax);
    }


    private static int getArraySize(PojoProperty property, int dimension) {
        int[] arraySize = COLLECTION.DEFAULT_SIZE;
        if (property != null) {
            COLLECTION collectionAnnotation = property.getAnnotation(COLLECTION.class);
            if (collectionAnnotation != null)
                arraySize = collectionAnnotation.size();
        }
        int size = -1;
        if (dimension < arraySize.length) {
            size = arraySize[dimension];
        } else if (arraySize.length > 0) {
            size = arraySize[arraySize.length - 1];
        }
        return size < 0 ? Common.random(1, 10) : size;
    }

    private static <T> T mockArray(
            Class clazz, PojoProperty pojoProperty, int dimension,
            Stack<String> stack, Type... context) {
        Class componentClass = clazz.getComponentType();
        int arraySize = getArraySize(pojoProperty, dimension);
        Object array = Array.newInstance(componentClass, arraySize);
        for (int i = 0; i < arraySize; i++) {
            Array.set(array, i, componentClass.isArray() ?
                    mockArray(componentClass, pojoProperty, dimension + 1, stack, context) :
                    mockClass(componentClass, pojoProperty, dimension + 1, stack, context));
        }
        return (T) array;
    }

//    public static void main(String [] args) throws ClassNotFoundException {
//        System.out.println(getComponentClass(new GenericType<Collection<String>[][][]>(){}.genericType()));
//    }

}
