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

package org.coodex.pojomocker;

import org.coodex.closure.CallableClosure;
import org.coodex.closure.MapClosureContext;
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
@SuppressWarnings("unchecked")
@Deprecated
public class MockerFacade {

    private final static Logger log = LoggerFactory.getLogger(MockerFacade.class);


    private static final PojoBuilder DEFAULT_BUILDER = new PojoBuilderImpl();

    private static final PojoBuilder POJO_BUILDER = new ServiceLoaderImpl<PojoBuilder>() {
        @Override
        public PojoBuilder getDefault() {
            return DEFAULT_BUILDER;
        }
    }.get();

    private static final DefaultMockers DEFAULT_MOCKER = new DefaultMockers();

    static final AcceptableServiceLoader<Annotation, Mocker<Annotation>> MOCKER_LOADER =
            new AcceptableServiceLoader<Annotation, Mocker<Annotation>>(
                    new ServiceLoaderImpl<Mocker<Annotation>>() {
                        @Override
                        public Mocker<Annotation> getDefault() {
                            return DEFAULT_MOCKER;
                        }
                    }
            ){};

    private static final AcceptableServiceLoader<String, RelationPolicy> RELATION_POLICY_LOADER =
            new AcceptableServiceLoader<String, RelationPolicy>(new ServiceLoaderImpl<RelationPolicy>() {
            }){};
//    private static final Map<String, PojoInfo> POJO_INFO_MAP = new HashMap<String, PojoInfo>();

    private static final SequenceContext SEQUENCE_CONTEXT = new SequenceContext();
    private static final MockerRefContext MOCKER_REF_CONTEXT = new MockerRefContext();

    private static SequenceGenerator buildSequenceGenerator(Sequence sequence) {
        if (sequence == null) return null;
        try {
            SequenceGenerator generator = sequence.sequenceType().newInstance();
            generator.setKey(sequence.key());
//            generator.reset();
            return generator;
        } catch (Throwable e) {
            throw Common.runtimeException(e);
        }
    }

    public static <T> T mock(GenericType<T> genericType) {
        return mock(genericType, null);
    }

    public static <T> T mock(GenericType<T> genericType, Class context) {
        return mock(genericType.genericType(context));
    }

    public static <T> T mock(Type type) {
        return mock(type, (Class[]) null);
    }

    public static <T> T mock(Method method) {
        return mock(method, (Class[]) null);
    }

    public static <T> T mock(final Method method, Class... context) {
        Type type = toTypeReference(method.getGenericReturnType(), context);
        if (type instanceof TypeVariable) {
            throw new RuntimeException(typeVariableInfo(type, context));
        } else {
            try {
                return $mock(type, new PojoProperty((PojoProperty)null, type) {
                    @Override
                    public Annotation[] getAnnotations() {
                        return method.getAnnotations();
                    }
                }, 0, null, context);
            } catch (MaxDeepException e) {
                return null;
            }
        }
    }

    public static <T> T mock(Type type, Class... context) {
        type = toTypeReference(type, context);
        if (type instanceof TypeVariable) {
            throw new RuntimeException(typeVariableInfo(type, context));
        } else {
            try {
                return $mock(type, null, 0, null, context);
            } catch (MaxDeepException e) {
                return null;
            }
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
        return method.getDeclaringClass().getName() +
                "." + method.getName() +
                '(' +
                parametersDeclaration(
                        method,
                        method.getGenericParameterTypes(),
                        context
                ) +
                ')';
    }

    private static String parametersDeclaration(Object executable, Type[] paramTyps, Class... context) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0, l = paramTyps.length; i < l; i++) {
            if (i > 0) builder.append(", ");
            builder.append(toTypeReference(paramTyps[i], context))
                    .append(' ').append(getParameterName(executable, i, "arg"));
        }
        return builder.toString();
    }

    private static String executableDeclaration(Constructor constructor, Class... context) {
        return constructor.getDeclaringClass().getName() +
                '(' +
                parametersDeclaration(
                        constructor,
                        constructor.getGenericParameterTypes(),
                        context
                ) +
                ')';
//        StringBuilder builder = new StringBuilder();
//        builder.append(constructor.getDeclaringClass().getName())
//                .append('(');
//        int i = 0;
//        for (Type t : constructor.getGenericParameterTypes()) {
//            if (i > 0) builder.append(", ");
//            builder.append(toTypeReference(t, context))
//                    .append(' ').append(getParameterName(constructor, i, "arg"));
//        }
//        return builder.append(')').toString();
    }

    private static List<Sequence> getSequence(PojoProperty pojoProperty) {
        if (pojoProperty == null) return null;

        List<Sequence> sequences = new ArrayList<Sequence>();

        Sequences sequencesAnnotation = pojoProperty.getAnnotation(Sequences.class);

        if (sequencesAnnotation != null) {
            sequences.addAll(Arrays.asList(sequencesAnnotation.value()));
        }

        Sequence sequence = pojoProperty.getAnnotation(Sequence.class);
        if (sequence != null) {
            sequences.add(sequence);
        }

        return sequences.size() > 0 ? sequences : null;
    }

    private static Object call(CallableClosure callableClosure) throws MaxDeepException {
        try {
            return callableClosure.call();
        } catch (MaxDeepException mde) {
            throw mde;
        } catch (Throwable th) {
            throw Common.runtimeException(th);
        }
    }

    private static <T> T $mock(
            final Type type, final PojoProperty property,
            final int dimension, final Stack<String> stack,
            final Type... context)
            throws MaxDeepException {

        if (type == null) return null;

        CallableClosure callableClosure = new CallableClosure() {
            @Override
            public Object call() throws Throwable {
                if (type instanceof ParameterizedType) {
                    return mockParameterizedType((ParameterizedType) type, property, dimension, stack, context);
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
        };

        callableClosure = defMockerRefContext(property, callableClosure);

        List<Sequence> sequenceList = getSequence(property);
        if (sequenceList != null) {
            Map<String, SequenceGenerator> generatorMap = new HashMap<String, SequenceGenerator>();
            for (Sequence sequence : sequenceList) {
                generatorMap.put(sequence.key(), buildSequenceGenerator(sequence));
            }
            return (T) SEQUENCE_CONTEXT.call(generatorMap, callableClosure);
        } else {
            return (T) call(callableClosure);
        }
    }

    private static CallableClosure defMockerRefContext(PojoProperty property, CallableClosure callableClosure) {
        Annotation annotation = property == null ? null : property.findDecoratedBy(MockerDef.class);
        if (annotation != null) {
            Class c = annotation.annotationType();
            final Map<String, Annotation> map = new HashMap<String, Annotation>();
            for (Method method : c.getDeclaredMethods()) {
                Annotation mocker = getAnnotationDecoratedBy(method.getDeclaredAnnotations(), Mock.class);
                if (mocker != null) {
                    map.put(method.getName(), mocker);
                }
            }
            if (map.size() > 0) {
                final CallableClosure finalCallableClosure = callableClosure;
                callableClosure = new CallableClosure() {
                    @Override
                    public Object call() throws Throwable {
                        return MOCKER_REF_CONTEXT.call(map, finalCallableClosure);
                    }
                };
            }
        }
        return callableClosure;
    }

    private static String getMockerName(Annotation annotation) {
        if (annotation == null) return null;
        Throwable th;
        try {
            Method m = annotation.annotationType().getDeclaredMethod("name");
            m.setAccessible(true);
            return (String) m.invoke(annotation);
        } catch (NoSuchMethodException e) {
            th = e;
        } catch (IllegalAccessException e) {
            th = e;
        } catch (InvocationTargetException e) {
            th = e;
        }
        if (th != null) {
            // do nothing
        }
        return null;

    }

    private static <T> T mockArray(
            Class clazz, final PojoProperty pojoProperty, final int dimension,
            final Stack<String> stack, final Type... context)
            throws MaxDeepException {
        final Class componentClass = clazz.getComponentType();
        int arraySize = getArraySize(pojoProperty, dimension);
        final Object array;
        if (componentClass.isArray()) {
            array = Array.newInstance(componentClass, arraySize);
            for (int i = 0; i < arraySize; i++) {
                Array.set(array, i, mockArray(componentClass, pojoProperty, dimension + 1, stack, context));
            }
        } else {
            SequenceGenerator generator = getGenerator(pojoProperty);
            if (generator != null) {
                generator.reset();
                arraySize = generator.size();
            }
            array = Array.newInstance(componentClass, arraySize);
            final int finalArraySize = arraySize;
            SEQUENCE_CONTEXT.call(generator, new CallableClosure() {
                @Override
                public Object call() throws Throwable {
                    for (int i = 0; i < finalArraySize; i++) {
                        Array.set(array, i, mockClass(componentClass, pojoProperty, dimension + 1, stack, context));
                    }
                    return null;
                }
            });
//            if (generator != null) {
//                SEQUENCE_CONTEXT.call(generator.getKey(), generator, callableClosure);
//            } else {
//                call(callableClosure);
//            }
        }
        return (T) array;
    }

    @SuppressWarnings("unchecked")
    private static <T> T mockParameterizedType(
            ParameterizedType type, PojoProperty property, int dimension, Stack<String> stack, Type... context)
            throws MaxDeepException {
        Class c = (Class) type.getRawType();
        if (Collection.class.isAssignableFrom(c)) {
            return (T) mockCollection(c, type.getActualTypeArguments()[0], property, dimension, stack, context);
        } else if (Map.class.isAssignableFrom(c)) {
            return (T) mockMap(c, type.getActualTypeArguments()[0], type.getActualTypeArguments()[1],
                    property, dimension, stack, context);
        }
        return mockPojo(new PojoInfo(type, context), property, stack, context);
    }

    @SuppressWarnings("unchecked")
    private static <T> T mockGenericArray(
            GenericArrayType type, final PojoProperty property,
            final int dimension, final Stack<String> stack, final Type... context)
            throws MaxDeepException {

        final Type componentType = toTypeReference(type.getGenericComponentType(), context);
        int size = getArraySize(property, dimension);
        final T array;
        if (componentType instanceof GenericArrayType) {
            array = (T) Array.newInstance(getComponentClass(componentType), size);
            for (int i = 0; i < size; i++) {
                Array.set(array, i, mockGenericArray(
                        (GenericArrayType) componentType, property,
                        dimension + 1, stack, context));
            }
        } else if (componentType instanceof ParameterizedType) {
            SequenceGenerator generator = getGenerator(property);
            if (generator != null) {
                generator.reset();
                size = generator.size();
            }
            array = (T) Array.newInstance(getComponentClass(componentType), size);
            final int finalSize = size;
            SEQUENCE_CONTEXT.call(generator, new CallableClosure() {
                @Override
                public Object call() throws Throwable {
                    for (int i = 0; i < finalSize; i++) {
                        Array.set(array, i, mockParameterizedType((ParameterizedType) componentType,
                                property, dimension + 1, stack, context));
                    }
                    return null;
                }
            });
//            if (generator == null)
//                call(callableClosure);
//            else
//                SEQUENCE_CONTEXT.call(generator.getKey(), generator, callableClosure);
        } else {
            throw new RuntimeException("unsupported component type: " + componentType.toString());
        }
        return array;
    }

    public static Class getComponentClass(Type componentType) {
        if (componentType == null || componentType instanceof TypeVariable) {
            return Object.class;
        } else if (componentType instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) componentType).getRawType();
        } else if (componentType instanceof Class) {
            return (Class) componentType;
        } else if (componentType instanceof GenericArrayType) {
            // ???
            return Array.newInstance(
                    getComponentClass(((GenericArrayType) componentType).getGenericComponentType()), 0)
                    .getClass();
        }
        return null;
    }

//    private static final Annotation getAnnotation(PojoProperty property) {
//        if (property == null) return null;
//        for (Annotation annotation : property.getAnnotations()) {
//            if (annotation != null &&
//                    annotation.annotationType().getAnnotation(Mock.class) != null) {
//                return annotation;
//            }
//        }
//        return null;
//    }

    @SuppressWarnings("unchecked")
    private static <T> T mockClass(
            Class clazz, PojoProperty property, int dimension, Stack<String> stack, Type... context)
            throws MaxDeepException {

        if (Collection.class.isAssignableFrom(clazz)) {
            return (T) mockCollection(clazz, null, property, dimension, stack, context);
        } else if (Map.class.isAssignableFrom(clazz)) {
            return (T) mockMap(clazz, null, null,
                    property, dimension, stack, context);
        } else {

            // find item
            Sequence.Item item = property == null ? null : property.getAnnotation(Sequence.Item.class);
            if (item != null) {
                SequenceGenerator generator = getSequenceGenerator(item.key(), item.notFound(), property);
                if (generator != null) {
                    return (T) generator.next();
                }
            }

            Annotation annotation = property == null ? null : property.findDecoratedBy(Mock.class);

            MockerRef ref = property == null ? null : property.getAnnotation(MockerRef.class);
            if (ref != null) {
                Annotation mocker = MOCKER_REF_CONTEXT.get(ref.name());
                if (mocker == null) {
                    log.info("mocker reference [{}] not found in context.", ref.name());
                } else {
                    annotation = mocker;
                }
            }

            if (TypeHelper.isPrimitive(clazz)) {
                return (T) MOCKER_LOADER.select(annotation).mock(annotation, clazz);
            } else {
                if (annotation != null) {
                    return (T) MOCKER_LOADER.select(annotation).mock(annotation, clazz);
                } else
                    return mockPojo(new PojoInfo(clazz, context), property, stack, context);
            }
        }
    }

    private static <T extends Map> T mockMap(
            Class<? extends Map> mapClass,
            Type keyType, Type valueType,
            PojoProperty property,
            int dimension,
            Stack<String> stack,
            Type... context) throws MaxDeepException {
        Map map;
        if (Map.class.equals(mapClass)) {
            map = new HashMap();
        } else {
            try {
                map = mapClass.newInstance();
            } catch (Throwable th) {
                throw new RuntimeException("unable init map: " + th.getLocalizedMessage(), th);
            }
        }
        MAP annotation = property.getAnnotation(MAP.class);
        int size = 5;
        SequenceGenerator keyGenerator = null;
        Annotation keyMocker = null;
        Annotation keyAnnotation = property.findDecoratedBy(MAP.Key.class);
        if (keyAnnotation != null) {
            size = keyAnnotation.annotationType().getAnnotation(MAP.Key.class).size();
            // 查找优先级：Sequence.Use -> Sequence
            Sequence.Use use = keyAnnotation.annotationType().getAnnotation(Sequence.Use.class);
            if (use != null) {
                keyGenerator = getSequenceGenerator(use.key(), use.notFound(), property);
            }

            if (keyGenerator == null) {
                Sequence sequence = keyAnnotation.annotationType().getAnnotation(Sequence.class);
                keyGenerator = buildSequenceGenerator(sequence);
            }

            if (keyGenerator == null) {
                for (Annotation anno : keyAnnotation.annotationType().getAnnotations()) {
                    if (anno.annotationType().getAnnotation(Mock.class) != null) {
                        keyMocker = anno;
                        break;
                    }
                }
            } else {
                keyGenerator.reset();
                size = keyGenerator.size();
            }
        }

        // 优先级 属性上Mock装饰的注解 -> 被MAP.Value装饰的注解里的Mock装饰的注解
        Annotation valueMocker = property.findDecoratedBy(Mock.class);
        if (valueMocker == null) {
            Annotation a = property.findDecoratedBy(MAP.Value.class);
            if (a != null) {
                for (Annotation anno : a.annotationType().getAnnotations()) {
                    if (anno.annotationType().getAnnotation(Mock.class) != null) {
                        valueMocker = anno;
                        break;
                    }
                }
            }
        }

        if (keyGenerator != null || keyMocker != null || valueMocker != null) {
            return mockMap(keyType, valueType, property, dimension, stack, map,
                    context, size, keyMocker, valueMocker, keyGenerator);
        } else {
            // deprecated
            return getMapUseMAPAnnotation(keyType, valueType, property,
                    dimension, stack, map, annotation, context);
        }
    }

    @SuppressWarnings("deprecation")
    private static <T extends Map> T getMapUseMAPAnnotation(
            Type keyType, Type valueType, final PojoProperty property,
            int dimension, Stack<String> stack, Map map,
            MAP annotation, Type[] context) throws MaxDeepException {
        int size = 5;
        Annotation keyMocker = null;
        Annotation valueMocker = null;
        String keySeq = null;
        Sequence.NotFound notFound = Sequence.NotFound.WARN;
        if (annotation != null) {
            size = Math.max(1, annotation.size());
            keyMocker = property.getAnnotation(annotation.keyMocker());
            valueMocker = property.getAnnotation(annotation.valueMocker());
            if (keyType == null) keyType = annotation.keyType();
            if (valueType == null) valueType = annotation.valueType();
            keySeq = annotation.keySeq();
            notFound = annotation.notFound();
        }

        if (keyType == null) keyType = String.class;
        if (valueType == null) valueType = String.class;

        SequenceGenerator generator = null;
        if (!Common.isBlank(keySeq)) {
            generator = getSequenceGenerator(keySeq, notFound, property);
        }

        if (generator != null) {
            generator.reset();
            size = generator.size();
        }

        return mockMap(keyType, valueType, property, dimension, stack, map,
                context, size, keyMocker, valueMocker, generator);
    }

    private static <T extends Map> T mockMap(final Type keyType, final Type valueType, final PojoProperty property, int dimension, Stack<String> stack, Map map, Type[] context, int size, Annotation keyMocker, Annotation valueMocker, SequenceGenerator generator) throws MaxDeepException {
        int maxRetry = size * 10;
        int retryTimes = 0;
        while (map.size() < size && retryTimes++ < maxRetry) {
            final Annotation finalKeyMocker = keyMocker;
            Object key = generator == null ?
                    $mock(keyType, keyMocker == null ? null :
                            new PojoProperty(property, keyType) {
                                @Override
                                public Annotation[] getAnnotations() {
                                    return new Annotation[]{finalKeyMocker};
                                }
                            }, dimension, stack, context) :
                    generator.next();

            final Annotation finalValueMocker = valueMocker;
            Object value = $mock(valueType, valueMocker == null ? null : new PojoProperty(property, valueType) {
                @Override
                public Annotation[] getAnnotations() {
                    return new Annotation[]{finalValueMocker};
                }
            }, dimension, stack, context);
            map.put(key, value);
        }

        return (T) map;
    }

//    private static final Type getComponentType(Type componentType, PojoProperty pojoProperty){
//        if(componentType == null) componentType = void.class;
//        if(pojoProperty != null){
//            COLLECTION collection = pojoProperty.getAnnotation(COLLECTION.class);
//            if
//        }
//    }


    private static boolean isCollection(Type t) {
        if (t instanceof ParameterizedType) {
            return isCollection(((ParameterizedType) t).getRawType());
        } else if (t instanceof Class) {
            return ((Class) t).isArray() || Collection.class.isAssignableFrom((Class<?>) t);
        } else
            return t instanceof GenericArrayType;
    }

    private static <T extends Collection> T mockCollection(
            Class<? extends Collection> collectionClass,
            Type componentType,
            final PojoProperty property,
            final int dimension,
            final Stack<String> stack,
            final Type... context) throws MaxDeepException {

        Collection collection;
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
        final Type componetTypeRef = toTypeReference(componentType, context);
        int size = getArraySize(property, dimension);

        if (isCollection(componetTypeRef)) {
            for (int i = 0; i < size; i++) {
                collection.add($mock(componetTypeRef, property, dimension + 1, stack, context));
            }
        } else {
            SequenceGenerator generator = getGenerator(property);
            if (generator != null) {
                generator.reset();
                size = generator.size();
            }
            final Collection finalCollection = collection;
            final int finalSize = size;
            SEQUENCE_CONTEXT.call(generator, new CallableClosure() {
                @Override
                public Object call() throws Throwable {
                    for (int i = 0; i < finalSize; i++) {
                        finalCollection.add($mock(componetTypeRef, property, dimension + 1, stack, context));
                    }
                    return null;
                }
            });
        }
        return (T) collection;
    }

//    private static PojoInfo getPojoInfo(Type type, Type... context) {
//        synchronized (POJO_INFO_MAP) {
//            type = toTypeReference(type, context);
//            if (!POJO_INFO_MAP.containsKey(type.toString())) {
//                POJO_INFO_MAP.put(type.toString(), new PojoInfo(type, context));
//            }
//        }
//        return POJO_INFO_MAP.get(type.toString());
//    }

    private static <T> T mockPojo(PojoInfo pojoInfo, PojoProperty property, Stack<String> stack, Type... context) throws MaxDeepException {
        if (stack == null)
            stack = new Stack<String>();
        stack.push(pojoInfo.getType().toString());
        try {
            // 达到指定层次时返回空值
            int deep = getDeep(property);
            String pojoType = pojoInfo.getType().toString();
            for (String s : stack) {
                if (pojoType.equals(s)) {
                    if (--deep < 0) throw new MaxDeepException();
                }
            }

            Map<String, List<String>> relations = new HashMap<String, List<String>>();
            for (PojoProperty pojoProperty : pojoInfo.getProperties()) {
                Relation relation = pojoProperty.getAnnotation(Relation.class);
                if (relations.containsKey(pojoProperty.getName())) continue;

                if (relation != null && relation.properties().length > 0) {
                    relations.put(pojoProperty.getName(), Arrays.asList(relation.properties()));
                    List<String> list = relations.get(pojoProperty.getName());
                    for (String dependency : list) {// field是否存在
                        if (pojoInfo.getProperty(dependency) == null)
                            throw new RuntimeException("property not exists: " + dependency);
                    }
                    checkCircular(relations, list, pojoProperty.getName());
                }
            }

            return (T) buildPojo(pojoInfo, stack, context);
        } finally {
            stack.pop();
        }
    }

    private static void checkCircular(
            Map<String, List<String>> relations, List<String> dependencies, String propertyName) {
        if (dependencies == null || dependencies.size() == 0) return;
        for (String s : dependencies) {
            if (propertyName.equals(s))
                throw new RuntimeException("circular relation: " + propertyName);
            checkCircular(relations, relations.get(s), propertyName);
        }
    }


    private static Object buildPojo(PojoInfo pojoInfo, Stack<String> stack, Type... context) {
        Object instance;
        try {
            instance = POJO_BUILDER.newInstance(pojoInfo);
        } catch (Throwable th) {
            throw new RuntimeException("Cannot instance type: " + pojoInfo.getType().toString()
                    + ", caused by: " + th.getLocalizedMessage(), th);
        }
        Set<String> over = new HashSet<String>();
        for (PojoProperty pojoProperty : pojoInfo.getProperties()) {
            try {
                buildProperty(instance, over, stack, pojoInfo, pojoProperty, context);

            } catch (Throwable th) {
                String message = "Cannot set property: " +
                        pojoProperty.getName() +
                        ", caused by: " +
                        th.getLocalizedMessage();

                if ("warn".equalsIgnoreCase(System.getProperty(Mock.POLICY_KEY))) {
                    log.warn("{}", message, th);
                } else {
                    throw new RuntimeException(message, th);
                }
            }
        }
        return instance;
    }

    private static void buildProperty(Object instance, Set<String> over, Stack<String> stack, PojoInfo pojoInfo, PojoProperty pojoProperty, Type[] context) throws Throwable {
        if (over.contains(pojoProperty.getName())) return;
        Relation relation = pojoProperty.getAnnotation(Relation.class);
        if (relation != null && relation.properties().length > 0) {
            List<Object> dependencies = new ArrayList<Object>();
            for (String property : relation.properties()) {
                PojoProperty p = pojoInfo.getProperty(property);
                buildProperty(instance, over, stack, pojoInfo, p, context);
                dependencies.add(POJO_BUILDER.get(instance, p));
            }
            POJO_BUILDER.set(instance, pojoProperty,
                    RELATION_POLICY_LOADER.select(relation.policy()).relate(relation.policy(), dependencies));
        } else {
            try {
                POJO_BUILDER.set(instance, pojoProperty,
                        $mock(pojoProperty.getType(), pojoProperty, 0, stack, context));
            } catch (MaxDeepException e) {
                POJO_BUILDER.set(instance, pojoProperty, null);
            }
        }
        over.add(pojoProperty.getName());
    }

    private static int getDeep(PojoProperty property) {
        int deepMin = 2, deepMax = 5;
        if (property != null) {
            Deep deep = property.getAnnotation(Deep.class);
            if (deep != null) {
                deepMin = Math.max(1, deep.min());
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


    private static SequenceGenerator getSequenceGenerator(String key, Sequence.NotFound notFound,
                                       PojoProperty pojoProperty) {
        SequenceGenerator generator = SEQUENCE_CONTEXT.get(key);
        if (generator == null) {
            switch (notFound) {
                case WARN:
                    log.warn("sequence [{}] not found. {}", key, pojoProperty);
                    break;
                case ERROR:
                    throw new RuntimeException("sequence [" + key + "] not found. " + pojoProperty);
                case IGNORE:
                default:
            }
        }
        return generator;
    }

    private static SequenceGenerator getGenerator(PojoProperty pojoProperty) {
        if (pojoProperty == null) return null;

        // use
        Sequence.Use use = pojoProperty.getAnnotation(Sequence.Use.class);
        SequenceGenerator generator = null;
        if (use != null) {
            generator = getSequenceGenerator(use.key(), use.notFound(), pojoProperty);
        }

        // sequence
        if (generator == null) {
            Sequence sequence = pojoProperty.getAnnotation(Sequence.class);
            if (sequence != null) {
                generator = buildSequenceGenerator(sequence);
            }
        }
        return generator;
    }

    private static Annotation getAnnotationDecoratedBy(Annotation[] annotations, Class<? extends Annotation> decorater) {
        if (annotations == null || annotations.length == 0) return null;
        for (Annotation a : annotations) {
            if (a.annotationType().getAnnotation(decorater) != null) {
                return a;
            }
        }
        return null;
    }

    private static class AbstractKeyValueContext<V> extends MapClosureContext<String, V> {
        @Override
        public Object call(String key, V v, CallableClosure callableClosure) throws MaxDeepException {
            try {
                return super.call(key, v, callableClosure);
            } catch (MaxDeepException mde) {
                throw mde;
            } catch (Throwable th) {
                throw Common.runtimeException(th);
            }
        }

        @Override
        public Object call(Map<String, V> map, CallableClosure callableClosure) throws MaxDeepException {
            try {
                return super.call(map, callableClosure);
            } catch (MaxDeepException mde) {
                throw mde;
            } catch (Throwable th) {
                throw Common.runtimeException(th);
            }
        }
    }

    private static class MockerRefContext extends AbstractKeyValueContext<Annotation> {

        public Object call(Annotation annotation, CallableClosure callableClosure) throws MaxDeepException {
            String name = getMockerName(annotation);
            if (Common.isBlank(name)) {
                return MockerFacade.call(callableClosure);
            } else {
                return call(name, annotation, callableClosure);
            }
        }
    }


    private static class SequenceContext extends AbstractKeyValueContext<SequenceGenerator> {

        public Object call(SequenceGenerator generator, CallableClosure callableClosure) throws MaxDeepException {
            if (generator == null) {
                return MockerFacade.call(callableClosure);
            } else {
                return call(generator.getKey(), generator, callableClosure);
            }
        }
    }

    private static class MaxDeepException extends Exception {
    }

//    public static void main(String [] args) throws ClassNotFoundException {
//        System.out.println(getComponentClass(new GenericType<Collection<String>[][][]>(){}.genericType()));
//    }

}
