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

import com.alibaba.fastjson.JSON;
import net.sf.cglib.proxy.Enhancer;
import org.coodex.closure.MapClosureContext;
import org.coodex.closure.StackClosureContext;
import org.coodex.util.ServiceLoader;
import org.coodex.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Supplier;

import static org.coodex.mock.Mock.Depth.DEFAULT_DEPTH;
import static org.coodex.mock.Mock.Dimension.*;
import static org.coodex.mock.Mock.Dimensions.SAME_DEFAULT;
import static org.coodex.util.Common.cast;
import static org.coodex.util.GenericTypeHelper.*;

/**
 * Coodex默认的MockerProvider实现
 *
 * @author Davidoff
 */
public class CoodexMockerProvider implements MockerProvider {


    private final static Logger log = LoggerFactory.getLogger(CoodexMockerProvider.class);
    /**
     * 用来存放模拟检索泛型变量的上下文
     */
    private static StackClosureContext<Type> TYPE_CONTEXT = new StackClosureContext<>();
    /**
     * 单值模拟定义的上下文
     */
    private static MapClosureContext<String, List<Annotation>> MOCKER_DEFINITION_CONTEXT = new MapClosureContext<String, List<Annotation>>() {
        @Override
        public Object call(Map<String, List<Annotation>> map, Supplier<?> supplier) {
            Map<String, List<Annotation>> contextMap = super.get();
            Map<String, List<Annotation>> copy = new HashMap<>(map);
            if (contextMap != null) {
                for (Map.Entry<String, List<Annotation>> entry : map.entrySet()) {
                    if (contextMap.containsKey(entry.getKey())) {
                        entry.getValue().addAll(contextMap.get(entry.getKey()));
                    }
                }
            }
            return super.call(copy, supplier);
        }

    };
    /**
     * 序列模拟器定义上下文
     */
    private static MapClosureContext<String, SequenceMockerFactory<?>> SEQUENCE_MOCKER_CONTEXT = new MapClosureContext<>();
    /**
     * 集合运行环境上下文
     */
    private static StackClosureContext<Map<String, SequenceMocker<?>>> COLLECTION_CONTEXT = new StackClosureContext<>();
    /**
     * 集合模拟的维度信息上下文
     */
    private static StackClosureContext<CollectionDimensions> DIMENSIONS_CONTEXT = new StackClosureContext<>();
    /**
     * 第三方类配置信息上下文
     */
    private static MapClosureContext<Class<?>, TypeAssignation> POJO_ASSIGNATION_CONTEXT = new MapClosureContext<>();
    /**
     * pojo深度信息上下文
     */
    private static PojoDeepStackContext POJO_DEEP_CONTEXT = new PojoDeepStackContext();
    /**
     *
     */
    private static ServiceLoader<SequenceMockerFactory<?>> SEQUENCE_MOCKER_FACTORIES = new LazyServiceLoader<SequenceMockerFactory<?>>() {
    };

    private static Object INJECT_UNENFORCED = new Object();
    private static Object STRATEGY_UNENFORCED = new Object();
    private static Object STRATEGY_RETRY = new Object();

    /**
     * 所有的TypeMocker实例，使用单例缓存
     */
    private static Singleton<Collection<TypeMocker<Annotation>>> TYPE_MOCKERS = Singleton.with(
            () -> new LazyServiceLoader<TypeMocker<Annotation>>() {
            }.getAll().values()
    );
    private static Object NOT_COLLECTION = new Object();
    private static Map<Class<?>, TypeAssignation> GLOBAL_ASSIGNATION = null;
    private static ServiceLoader<RelationStrategy> RELATION_STRATEGIES = new LazyServiceLoader<RelationStrategy>() {
    };

    public CoodexMockerProvider() {
        if (GLOBAL_ASSIGNATION == null) {
            synchronized (CoodexMockerProvider.class) {
                if (GLOBAL_ASSIGNATION == null) {
                    GLOBAL_ASSIGNATION = new HashMap<>();
                    ReflectHelper.foreachClass(serviceClass -> GLOBAL_ASSIGNATION.put(
                            serviceClass.getAnnotation(Mock.Assignation.class).value(),
                            new TypeAssignation(new PojoInfo(serviceClass))
                    ), className -> {
                        try {
                            Class<?> c = Class.forName(className);
                            return c.getAnnotation(Mock.Assignation.class) != null;
                        } catch (Throwable throwable) {
                            log.debug("class {} load failed. {}", className, throwable.getLocalizedMessage());
                        }
                        return false;
                    }, ASSIGNATIONS_PACKAGE);
                }
            }
        }
    }

    /**
     * 获得所有被指定注解装饰过的注解
     *
     * @param decorator   装饰器
     * @param annotations 备选
     * @return 所有被指定注解装饰过的注解，如果没有则返回size为0的List
     */
    private static List<Annotation> getAllDecoratedBy(Class<? extends Annotation> decorator, Annotation... annotations) {
        List<Annotation> list = new ArrayList<>();
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
     * 获取所有被{@link Mock}修饰过的注解
     *
     * @param annotations 备选注解
     * @return 列表最后会有个null
     */
    private static List<Annotation> getMockers(Annotation... annotations) {
        //        list.add(null);// 当没有任何Mock修饰过的，按默认规则产生
        return getAllDecoratedBy(Mock.class, annotations);
    }

//    private static String[] getNameSpaces() {
//        String string = NAMESPACE.get();
//        if (Common.isBlank(string) || DEFAULT_NAMESPACE.equalsIgnoreCase(string)) {
//            return new String[]{DEFAULT_NAMESPACE};
//        } else {
//            return new String[]{DEFAULT_NAMESPACE, string};
//        }
//    }

    private static Object runSupplier(Supplier<?> supplier) {
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            if (throwable instanceof MockException) {
                throw (MockException) throwable;
            } else {
                throw new MockException(throwable.getLocalizedMessage(), throwable);
            }
        }
    }

    /**
     * 在备选内容中找到指定类型的注解
     *
     * @param annotationClass 需要找到的注解
     * @param annotations     备选范围
     * @param <A>             Annotation
     * @return 指定类型的注解的实例，如不存在则返回 {@code null}
     */
    private static <A extends Annotation> A getAnnotation(Class<A> annotationClass, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                return cast(annotation);
            }
        }
        return null;
    }

    /**
     * 根据所有模拟注解及其类型找到合适的TypeMocker实例
     *
     * @param type        需要模拟的类型
     * @param annotations 所有被Mock修饰过的注解及{@code null}
     * @return TypeMocker实例，如果没有合适的则返回{@code null}
     */
    private static MockFacade<? extends Annotation> getTypeMocker(Type type, List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            if (annotation != null && annotation.annotationType().getAnnotation(Mock.class) == null) continue;
            for (TypeMocker<Annotation> provider : TYPE_MOCKERS.get()) {
                if (annotation == null || annotation.annotationType().equals(
                        solveFromType(TypeMocker.class.getTypeParameters()[0],
                                provider.getClass()
                        ))) {
                    if (provider.accept(annotation, type))
                        return new MockFacade<>(type, provider, annotation,
                                getAnnotation(Mock.Nullable.class, annotations.toArray(new Annotation[0])));
                }
            }
        }
        return null;
    }

    private static Map<Class<?>, TypeAssignation> getTypeAssignationsFromAnnotations(Annotation[] annotations)
            throws InvocationTargetException, IllegalAccessException {
        Map<Class<?>, TypeAssignation> map = new HashMap<>();
        for (Annotation annotation : getAllDecoratedBy(Mock.Assignation.class, annotations)) {
            map.put(
                    annotation.annotationType().getAnnotation(Mock.Assignation.class).value(),
                    new TypeAssignation(annotation)
            );
        }
        return map;
    }

    /**
     * 针对有参数类型进行模拟
     *
     * @param type        ParameterizedType
     * @param annotations 配置信息
     * @return 模拟值
     */
    private Object mockParameterizedType(ParameterizedType type, Annotation... annotations) throws InvocationTargetException, IllegalAccessException {
        Class<?> c = (Class<?>) type.getRawType();
        Object result = mockIfCollectionAndMap(c, type, annotations);
        if (result != NOT_COLLECTION) {
            return result;
        } else {
            return mockPojo(type, annotations);
        }
    }

    /**
     * 模拟一个明确的类型
     *
     * @param c           明确的类型
     * @param annotations 模拟定义
     * @return 模拟值
     */
    private Object mockClass(Class<?> c, Annotation... annotations) throws InvocationTargetException, IllegalAccessException {


        // Inject
        Object result = mockIfInject(c, InjectConfig.build(getAnnotation(Mock.Inject.class, annotations)), annotations);

        if (result != INJECT_UNENFORCED) {
            return result;
        }

        MockFacade<? extends Annotation> param = getTypeMocker(c, getMockers(annotations));
        if (param != null) {
            return param.mock();
        } else {
            if (String.class.equals(c)) {
                return StringTypeMocker.mock();
            } else if (Common.inArray(c, NumberTypeMocker.SUPPORTED)) {
                return NumberTypeMocker.mock(c);
            } else if (Common.inArray(c, CharTypeMocker.SUPPORTED_CLASSES)) {
                return CharTypeMocker.mock(c);
            } else if (Common.inArray(c, BooleanTypeMocker.SUPPORTED)) {
                return BooleanTypeMocker.mock(c);
            }
        }

        result = c.isArray() ?
                mockArray(c.getComponentType(), 0, annotations) :
                mockIfCollectionAndMap(c, c, annotations);

        if (result != NOT_COLLECTION) {
            return result;
        }


        return mockPojo(c, annotations);
    }

    private Object mockIfCollectionAndMap(Class<?> c, Type collectionsContext, Annotation[] annotations) {
        Object result;
        if (Collection.class.isAssignableFrom(c)) {
            Type t = solveFromType(Collection.class.getTypeParameters()[0], collectionsContext);
            if (t instanceof TypeVariable) {
                throw new MockException("Cannot mock collection: " + collectionsContext);
            }
            result = mockCollection(cast(c), t, 0, annotations);
        } else if (Map.class.isAssignableFrom(c)) {
            Type key = solveFromType(Map.class.getTypeParameters()[0], collectionsContext);
            Type value = solveFromType(Map.class.getTypeParameters()[1], collectionsContext);
            if (key instanceof TypeVariable || value instanceof TypeVariable) {
                throw new MockException("Cannot mock map: " + collectionsContext);
            }
            result = mockMap(cast(c), 0, key, value, annotations);
        } else {
            result = NOT_COLLECTION;
        }
        return result;
    }

    /**
     * @param type        只有两种可能，{@link Class}和{@link ParameterizedType}
     * @param annotations 模拟定义
     * @return 模拟值
     */
    private Object mockPojo(final Type type, final Annotation... annotations) throws InvocationTargetException, IllegalAccessException {
        Supplier<?> closure = new Supplier<Object>() {
            @Override
            public Object get() {
                return POJO_DEEP_CONTEXT.call(type, new Supplier<Object>() {
                    @Override
                    public Object get() {
                        PojoInfo pojoInfo = new PojoInfo(type);
                        TypeAssignation current = new TypeAssignation(pojoInfo);
                        TypeAssignation typeAssignation = POJO_ASSIGNATION_CONTEXT.get(typeToClass(type));
                        typeAssignation = typeAssignation == null ? current : typeAssignation.merge(current);

                        if (depthCheck(current, typeAssignation)) return null;

                        final Map<String, Object> mocked = new HashMap<>();
                        List<PojoProperty> properties = getPojoPropertiesAndSort(pojoInfo);

                        while (properties.size() > 0) {
                            List<PojoProperty> temp = new ArrayList<>();
                            for (PojoProperty property : properties) {
                                Annotation[] mockAnnotations = typeAssignation.get(property.getName());
                                Mock.Relation relation = getAnnotation(Mock.Relation.class, mockAnnotations);

                                if (relation == null) {
                                    mocked.put(property.getName(), mock(toReference(property.getType(), type), type,
                                            typeAssignation.get(property.getName())));
                                    temp.add(property);
                                } else {
                                    RelationStrategy toUse = null;
                                    for (RelationStrategy strategy : RELATION_STRATEGIES.getAll().values()) {
                                        if (strategy.accept(relation.strategy())) {
                                            toUse = strategy;
                                            break;
                                        }
                                    }

                                    if (toUse == null) {
                                        throw new MockException("none RelationStrategy accept [" + relation.strategy() + "]. " + relation.toString());
                                    }
                                    if (isAllMocked(relation, mocked, typeAssignation)) {
                                        Object result;
                                        try {
                                            result = relationCall(mocked, relation, toUse);
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            throw Common.rte(e);
                                        }
                                        if (relation == STRATEGY_UNENFORCED)
                                            throw new MockException("strategy unenforced. " + toUse);
                                        if (relation == STRATEGY_RETRY)
                                            continue;

                                        mocked.put(property.getName(), result);
                                        temp.add(property);
                                    }
                                }
                            }

                            if (temp.size() == 0) {
                                StringBuilder builder = new StringBuilder();
                                builder.append("invalid dependency. ");
                                for (PojoProperty property : properties) {
                                    builder.append("\n\t").append(property.getAnnotation(Mock.Relation.class));
                                }
                                throw new MockException(builder.toString());
                            }
                            properties.removeAll(temp);
                        }

                        MockInvocationHandler mockInvocationHandler = new MockInvocationHandler(mocked);

                        Object instance;
                        if (pojoInfo.getRowType().isInterface()) {
                            instance = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                                    new Class<?>[]{pojoInfo.getRowType()},
                                    mockInvocationHandler);
                        } else {
                            Enhancer enhancer = new Enhancer();
                            enhancer.setSuperclass(pojoInfo.getRowType());
                            enhancer.setCallback(mockInvocationHandler);
                            try {
                                instance = enhancer.create();
                            } catch (IllegalArgumentException e) {
                                throw new MockException("mock " + pojoInfo.getRowType().getName() + " failed: " + e.getLocalizedMessage(), e);
                            }
                            for (Field field : instance.getClass().getFields()) {
                                if (mocked.containsKey(field.getName())) {
                                    field.setAccessible(true);
                                    try {
                                        field.set(instance, mocked.get(field.getName()));
                                    } catch (IllegalAccessException e) {
                                        throw Common.rte(e);
                                    }
                                }
                            }
                            try {
                                instance = JSON.parseObject(JSON.toJSONString(instance), pojoInfo.getType());
                            } catch (Throwable th) {
                                log.warn("convert failed. {}, {}, {}",
                                        th.getLocalizedMessage(),
                                        pojoInfo.getType(),
                                        JSON.toJSONString(instance));
                            }
                        }
                        return instance;
                    }

                    private Object relationCall(Map<String, Object> mocked, Mock.Relation relation, RelationStrategy toUse)
                            throws IllegalAccessException, InvocationTargetException {

                        Object result = STRATEGY_UNENFORCED;
                        for (Method method : toUse.getClass().getDeclaredMethods()) {
                            RelationStrategy.Strategy strategy = method.getAnnotation(RelationStrategy.Strategy.class);
                            if (strategy != null && strategy.value().equals(relation.strategy())) {
                                Object[] args = new Object[relation.dependencies().length];
                                for (int i = 0, len = method.getParameterTypes().length; i < len; ) {
                                    String methodName = ReflectHelper.getParameterName(method, i);
                                    if (!Common.inArray(methodName, relation.dependencies()))
                                        throw new MockException("relation " + relation + " not included " + methodName + ". " + method.toGenericString());
                                    if (!mocked.containsKey(methodName)) {
                                        result = STRATEGY_RETRY;
                                        break;
                                    }
                                    args[i++] = mocked.get(methodName);
                                }
                                if (result != STRATEGY_RETRY) {
                                    result = method.invoke(toUse, args);
                                }
                                break;
                            }
                        }
                        return result;
                    }

                    private boolean isAllMocked(Mock.Relation relation, Map<String, Object> mocked, TypeAssignation assignation) {
                        for (String dependency : relation.dependencies()) {
                            if (assignation.properties.containsKey(dependency) && !mocked.containsKey(dependency))
                                return false;
                        }
                        return true;
                    }

                    private List<PojoProperty> getPojoPropertiesAndSort(PojoInfo pojoInfo) {
                        List<PojoProperty> properties = new ArrayList<>(pojoInfo.getProperties());
                        // 排序，有引用的在后
                        properties.sort(new Comparator<PojoProperty>() {
                            int[] range = {0, -1, 1, 0};

                            @Override
                            public int compare(PojoProperty o1, PojoProperty o2) {
                                int b1 = o1.getAnnotation(Mock.Relation.class) == null ? 0 : 2;
                                int b2 = o2.getAnnotation(Mock.Relation.class) == null ? 0 : 1;

                                return range[b1 | b2];
                            }
                        });
                        return properties;
                    }


                    private boolean depthCheck(TypeAssignation current, TypeAssignation typeAssignation) {
                        int maxDepth = DEFAULT_DEPTH;
                        Mock.Depth depth = getAnnotation(Mock.Depth.class, annotations);
                        if (depth == null) {
                            depth = current.depth;
                        }
                        if (depth == null) {
                            depth = typeAssignation.depth;
                        }
                        if (depth != null) {
                            maxDepth = Math.max(depth.value(), 1);
                        }
                        return maxDepth < POJO_DEEP_CONTEXT.depth(type);
                    }
                });
            }
        };

        Map<Class<?>, TypeAssignation> assignations = (POJO_ASSIGNATION_CONTEXT.get() == null) ?
                GLOBAL_ASSIGNATION : new HashMap<>();

        assignations.putAll(getTypeAssignationsFromAnnotations(annotations));

        if (assignations.size() > 0)
            return POJO_ASSIGNATION_CONTEXT.call(assignations, closure);
        else
            return runSupplier(closure);
    }

    private Object mockIfInject(Type type, InjectConfig inject, Annotation[] annotations) {
        if (inject != null) {
            if (COLLECTION_CONTEXT.get() != null) {
                // 集合上下文中
                SequenceMockerFactory<?> sequenceMockerFactory = SEQUENCE_MOCKER_CONTEXT.get(inject.getKey());
                if (sequenceMockerFactory != null && acceptType(sequenceMockerFactory, type)) {
                    // 使用序列模拟器
                    SequenceMocker<?> sequenceMocker = COLLECTION_CONTEXT.get().get(inject.getKey());
                    if (sequenceMocker == null) {
                        sequenceMocker = sequenceMockerFactory.newSequenceMocker(annotations);
                        COLLECTION_CONTEXT.get().put(inject.getKey(), sequenceMocker);
                    }
                    return sequenceMocker.next();
                }
            }

            List<Annotation> mockerDefinitions = MOCKER_DEFINITION_CONTEXT.get(inject.getKey());
            if (mockerDefinitions != null) {
                MockFacade<? extends Annotation> facade = getTypeMocker(type, mockerDefinitions);
                if (facade != null) {
                    return facade.mock();
                }
            }

            MockException mockException = new MockException("Mocker not found. key:" + inject.getKey() + "; context: " + TYPE_CONTEXT.get());
            switch (inject.getNotFound()) {
                case WARN:
                    log.warn(mockException.getLocalizedMessage());
                    break;
                case ERROR:
                    throw mockException;
                case IGNORE:
            }
        }

        return INJECT_UNENFORCED;
    }

    private boolean acceptType(SequenceMockerFactory<?> sequenceMockerFactory, Type type) {
        Class<?> c1 = typeToClass(solveFromInstance(SequenceMockerFactory.class.getTypeParameters()[0], sequenceMockerFactory));
        Class<?> c2 = typeToClass(type);
        return c1.isAssignableFrom(c2);
    }


    @Override
    public Object mock(final Type type, final Type context, Annotation... annotations) {
        if (annotations == null) {
            annotations = new Annotation[0];
        }
        final Annotation[] finalAnnotations = annotations;
        return TYPE_CONTEXT.call(context, () -> innerMock(toReference(type, context), finalAnnotations));
    }

    /**
     * @param componentType 数组元素类型
     * @param d             数组维度
     * @param annotations   注解
     * @return 模拟的数组
     */
    private Object mockArray(Type componentType, int d, Annotation... annotations) {
        // 使用List 转 数组方案
        List<?> list = mockCollection(List.class, componentType, d, annotations);
        if (list != null) {
            Object arrayInstance = Array.newInstance(typeToClass(componentType), list.size());
            int i = 0;
            for (Object o : list) {
                Array.set(arrayInstance, i++, o);
            }
            return arrayInstance;
        }
        return null;
    }

    private Map<?,?> buildMapInstance(Class<? extends Map<?,?>> mapClass, int d, Annotation... annotations) {
        if (Map.class.equals(mapClass)) {
            return DIMENSIONS_CONTEXT.get().ordered(d) ? new LinkedHashMap<>() : new HashMap<>();
        }

        // todo 根据定义指定实例

        try {
            if (mapClass.isInterface()) {
                return cast(getProxyObject(
                        DIMENSIONS_CONTEXT.get().ordered(d) ? new LinkedHashMap<>() : new HashMap<>(),
                        mapClass));
            } else {
                Constructor<?> constructor = mapClass.getConstructor();
                constructor.setAccessible(true);
                return (Map<?, ?>) constructor.newInstance();
            }
        } catch (Throwable throwable) {
            throw new MockException("map class " + mapClass.getName() + " not support yet.", throwable);
        }

    }

    private Collection<?> buildCollectionInstance(Class<? extends Collection<?>> collectionClass, int d, Annotation... annotations) {
        Collection<?> x = getJavaUtilCollection(collectionClass, d);
        if (x != null) return x;

        // todo 根据定义指定实例

        try {
            if (collectionClass.isInterface()) {
                return (Collection<?>) getProxyObject(getJavaUtilCollection(Collection.class, d), collectionClass);
            } else {
                Constructor<?> constructor = collectionClass.getConstructor();
                constructor.setAccessible(true);
                return (Collection<?>) constructor.newInstance();
            }
        } catch (Throwable throwable) {
            throw new MockException("collection class " + collectionClass.getName() + " not support yet. ", throwable);
        }

    }

    private Collection<?> getJavaUtilCollection(Class<?> collectionClass, int d) {
        if (List.class.equals(collectionClass)) {
            return new ArrayList<>();
        } else if (Set.class.equals(collectionClass)) {
            return DIMENSIONS_CONTEXT.get().ordered(d) ? new LinkedHashSet<>() : new HashSet<>();
        } else if (Collection.class.equals(collectionClass)) {
            return DIMENSIONS_CONTEXT.get().ordered(d) ? new ArrayList<>() : new HashSet<>();
        }
        return null;
    }

    private Object getProxyObject(final Object instance, Class<?> interfaceClass) {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    Method targetMethod = instance.getClass().getMethod(method.getName(), method.getParameterTypes());
                    targetMethod.setAccessible(true);
                    return targetMethod.invoke(instance, args);
                });
    }

    private <C extends Collection<?>> C mockCollection(
            final Class<C> collectionClass, final Type componentType,
            final int d, final Annotation... annotations) {

        Supplier<?> closure = new Supplier<Object>() {
            private Object mockElementOfCollection() {

                Object result = mockIfInject(componentType, InjectConfig.build(getAnnotation(Mock.Inject.class, annotations)), annotations);

                if (result != INJECT_UNENFORCED) {
                    return result;
                }
                MockFacade<? extends Annotation> param = getTypeMocker(componentType, getMockers(annotations));
                if (param != null) return param.mock();

                if (componentType instanceof GenericArrayType) {
                    return mockArray(((GenericArrayType) componentType).getGenericComponentType(), d + 1, annotations);
                }
                if (componentType instanceof Class) {
                    Class<?> c = (Class<?>) componentType;
                    if (c.isArray()) {
                        return mockArray(c.getComponentType(), d + 1, annotations);
                    }
                }
                if (componentType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) componentType;
                    Class<?> c = typeToClass((parameterizedType).getRawType());
                    if (Collection.class.isAssignableFrom(c)) {
                        return mockCollection(cast(c),
                                solveFromType(Collection.class.getTypeParameters()[0], parameterizedType),
                                d + 1, annotations);
                    } else if (Map.class.isAssignableFrom(c)) {
                        return mockMap(cast(c), d + 1,
                                solveFromType(Map.class.getTypeParameters()[0], componentType),
                                solveFromType(Map.class.getTypeParameters()[1], componentType),
                                annotations);
                    }
                }
                return innerMock(componentType, annotations);
            }

            @Override
            public Object get() {
                if (DIMENSIONS_CONTEXT.get().nullable(d))
                    return null;
                Collection<?> collection = buildCollectionInstance(collectionClass, d, annotations);
                CollectionDimensions dimensions = DIMENSIONS_CONTEXT.get();
                for (int i = 0, size = dimensions.getSize(d); i < size; i++) {
                    collection.add(cast(mockElementOfCollection()));
                }
                return collection;
            }
        };


        return cast(runSupplier(
                getDimensionsSupplier(d,
                        getCollectionSequenceSupplier(closure),
                        annotations)));
    }

    private Supplier<?> getCollectionSequenceSupplier(final Supplier<?> supplier) {
        return () -> COLLECTION_CONTEXT.call(new HashMap<>(), supplier);
    }

    private Supplier<?> getDimensionsSupplier(int d, Supplier<?> supplier, Annotation[] annotations) {
        if (d == 0) {
            final Supplier<?> innerSupplier = supplier;
            final CollectionDimensions collectionDimensions;
            Mock.Dimensions dimensionsAnnotation = getAnnotation(Mock.Dimensions.class, annotations);


            if (dimensionsAnnotation != null) {
                collectionDimensions = new CollectionDimensions(dimensionsAnnotation.value(), dimensionsAnnotation.same());
            } else {
                Mock.Dimension dimension = getAnnotation(Mock.Dimension.class, annotations);
                collectionDimensions = new CollectionDimensions(
                        dimension == null ? new Mock.Dimension[0] : new Mock.Dimension[]{dimension},
                        SAME_DEFAULT
                );
            }
            supplier = () -> DIMENSIONS_CONTEXT.call(collectionDimensions, innerSupplier);
        }
        return supplier;
    }

    private Object mockMap(
            final Class<? extends Map<?, ?>> mapClass, final int d,
            final Type keyType, final Type valueType, final Annotation... annotations) {


        Supplier<?> supplier = new Supplier<Object>() {

            private Object mockValue() {
                Object value = mockIfInject(valueType,
                        InjectConfig.build(getAnnotation(Mock.Value.class, annotations)),
                        annotations);
                return (value == INJECT_UNENFORCED) ?
                        innerMock(valueType, annotations) :
                        value;
            }

            private Object mockKey() {
                Object keyMock = mockIfInject(keyType,
                        InjectConfig.build(getAnnotation(Mock.Key.class, annotations)),
                        annotations);
                return (keyMock == INJECT_UNENFORCED) ?
                        innerMock(keyType, annotations) :
                        keyMock;
            }

            @Override
            public Object get() {
                if (DIMENSIONS_CONTEXT.get().nullable(d)) return null;
                Map<?,?> instance = buildMapInstance(mapClass, d, annotations);
                int size = DIMENSIONS_CONTEXT.get().getSize(d);
                int retry = size * 3;
                while (instance.size() < size && retry-- > 0) {
                    instance.put(cast(mockKey()), cast(mockValue()));
                }
                return instance;
            }
        };

        return runSupplier(
                getDimensionsSupplier(d,
                        getCollectionSequenceSupplier(supplier),
                        annotations));

    }

    private Object innerMock(final Type type, final Annotation... annotations) {
        if (type == null || void.class.equals(type) || Void.class.equals(type)) {
            return null;
        }

        Mock.Designated designated = getAnnotation(Mock.Designated.class, annotations);
        //noinspection StatementWithEmptyBody
        if (designated != null) {
            // todo 使用资源文件模拟
        }

        Supplier<?> supplier = () -> {
            Type toMock = type;
            if (toMock instanceof TypeVariable) {
                toMock = solveFromType((TypeVariable<?>) toMock, TYPE_CONTEXT.get());
            }

            if (toMock instanceof TypeVariable) {
                throw new MockException("TypeVariable " + toMock + " not supported.");
            } else if (toMock instanceof Class) {
                try {
                    return mockClass((Class<?>) toMock, annotations);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new MockException(e.getLocalizedMessage(), e);
                }
            } else if (toMock instanceof ParameterizedType) {
                try {
                    return mockParameterizedType((ParameterizedType) toMock, annotations);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new MockException(e.getLocalizedMessage(), e);
                }
            } else if (toMock instanceof GenericArrayType) {
                return mockArray(((GenericArrayType) toMock).getGenericComponentType(), 0, annotations);
            } else {
                throw new MockException("unsupported type : " + toMock);
            }
        };
        supplier = getDefinitionSupplier(supplier, annotations);
        supplier = getSequenceSupplier(supplier, annotations);
        return runSupplier(supplier);

    }

    /**
     * 根据注解设置序列模拟器配置上下文
     *
     * @param supplier    闭包执行体
     * @param annotations 可能包含@Mock.Sequence或@Mock.Sequences的注解
     * @return 带有序列模拟器信息的闭包执行体
     */
    private Supplier<?> getSequenceSupplier(Supplier<?> supplier, Annotation[] annotations) {
        final Map<String, SequenceMockerFactory<?>> sequenceMockerMap = new HashMap<>();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Mock.Sequence.class)) {
                Mock.Sequence sequence = (Mock.Sequence) annotation;
                Class<? extends SequenceMockerFactory<?>> factoryClass = sequence.factory();
                if (!factoryClass.isInterface()) {
                    log.warn("{} not interface.", sequence);
                }
                sequenceMockerMap.put(sequence.name(), getSequenceMockerFactory(factoryClass));
            }
        }
        if (sequenceMockerMap.size() > 0) {
            final Supplier<?> innerSupplier = supplier;
            supplier = () -> SEQUENCE_MOCKER_CONTEXT.call(sequenceMockerMap, innerSupplier);
        }
        return supplier;
    }

    private SequenceMockerFactory<?> getSequenceMockerFactory(Class<? extends SequenceMockerFactory<?>> factoryClass) {
        SequenceMockerFactory<?> factory = SEQUENCE_MOCKER_FACTORIES.get(factoryClass);
        if (factory == null && !factoryClass.isInterface()) {
            try {
                factory = factoryClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new MockException(e.getLocalizedMessage(), e);
            }
        }
        return factory;
    }

    /**
     * 根据注解设置模拟配置上下文
     *
     * @param supplier    闭包执行体
     * @param annotations 可能包含@Mock.Declaration修饰的注解
     * @return 带有配置信息的闭包执行体
     */
    private Supplier<?> getDefinitionSupplier(Supplier<?> supplier, Annotation[] annotations) {
        List<Annotation> defList = getAllDecoratedBy(Mock.Declaration.class, annotations);
        final Map<String, List<Annotation>> declarationMap = new HashMap<>();
        for (Annotation annotation : defList) {
            Class<?> c = annotation.annotationType();
            // 例外掉所有java和javax得注解
            if (c.getName().startsWith("java.") || c.getName().startsWith("javax.")) continue;

            for (Method method : c.getMethods()) {
                String key = method.getName();
                List<Annotation> mock = new ArrayList<>(
                        Arrays.asList(method.getDeclaredAnnotations())
                );//getAllDecoratedBy(Mock.class, method.getDeclaredAnnotations());

                if (Annotation.class.isAssignableFrom(method.getReturnType())
                    /* && method.getReturnType().getAnnotation(Mock.class) != null */) {
                    method.setAccessible(true);
                    try {
                        mock.add((Annotation) method.invoke(annotation));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new MockException("Load @Mock.Declaration failed. " + method.getDeclaringClass() + "." + method.getName(), e);
                    }
                }
                if (mock.size() > 0) {
                    List<Annotation> annotationList = declarationMap.get(key);
                    if (annotationList != null) {
                        annotationList.addAll(mock);
                    } else {
                        annotationList = mock;
                    }
                    declarationMap.put(key, annotationList);
                }
            }
        }

        if (declarationMap.size() > 0) {
            final Supplier<?> innerSupplier = supplier;
            supplier = () -> MOCKER_DEFINITION_CONTEXT.call(declarationMap, innerSupplier);
        }
        return supplier;
    }

    private static class MockFacade<A extends Annotation> {
        private final Type targetType;
        private final TypeMocker<A> mocker;
        private final A annotation;
        private final Mock.Nullable nullable;

        private MockFacade(Type targetType, TypeMocker<A> mocker, A annotation, Mock.Nullable nullable) {
            this.targetType = targetType;
            this.mocker = mocker;
            this.annotation = annotation;
            this.nullable = nullable;
        }

        Object mock() {
            return mocker.mock(annotation, nullable, targetType);
        }
    }

    private static class CollectionDimensions {
        private Mock.Dimension[] dimensions;
        private int[] corrected;
        private boolean same;

        CollectionDimensions(Mock.Dimension[] dimensions, boolean same) {
            this.dimensions = dimensions;
            this.same = same;
            corrected = new int[16];
            Arrays.fill(corrected, Integer.MIN_VALUE);

        }

        private int calc(int dimension) {
            int max = MAX_DEFAULT;
            int min = MIN_DEFAULT;
            int size = SIZE_DEFAULT;
            if (dimension < dimensions.length) {
                Mock.Dimension d = dimensions[dimension];
                max = Math.max(1, Math.max(d.max(), d.min()));
                min = Math.max(1, Math.min(d.min(), d.max()));
                size = d.size();
            }
            return size > 0 ? size : (new Random().nextInt(max - min + 1) + min);
        }

        int getSize(int dimension) {
            if (dimension >= 16)
                throw new MockException("too many dimensions.");
            if (same) {
                if (corrected[dimension] == Integer.MIN_VALUE) {
                    corrected[dimension] = calc(dimension);
                }
                return corrected[dimension];
            } else {
                return calc(dimension);
            }
        }

        boolean nullable(int dimension) {
            if (dimension >= 16)
                throw new MockException("too many dimensions.");
            if (dimension < dimensions.length) {
                return Math.random() < dimensions[dimension].nullProbability();
            } else {
                return false;
            }
        }

        boolean ordered(int dimension) {
            if (dimension >= 16)
                throw new MockException("too many dimensions.");
            if (dimension >= dimensions.length)
                return ORDERED_DEFAULT;
            else
                return dimensions[dimension].ordered();
        }
    }

    private static class InjectConfig {

        private String key;
        private Mock.NotFound notFound;

        InjectConfig(String key, Mock.NotFound notFound) {
            this.key = key;
            this.notFound = notFound;
        }

        static InjectConfig build(Mock.Inject inject) {
            if (inject == null) return null;
            return new InjectConfig(inject.value(), inject.notFound());
        }

        static InjectConfig build(Mock.Key inject) {
            if (inject == null) return null;
            return new InjectConfig(inject.value(), inject.notFound());
        }

        static InjectConfig build(Mock.Value inject) {
            if (inject == null) return null;
            return new InjectConfig(inject.value(), inject.notFound());
        }

        String getKey() {
            return key;
        }

        Mock.NotFound getNotFound() {
            return notFound;
        }
    }

    private static class PojoDeepStackContext extends StackClosureContext<Type> {

        int depth(Type type) {
            Stack<Type> pojoStack = super.getVariant();
            int count = 0;
            for (Type t : pojoStack) {
                if (t.equals(type)) count++;
            }
            return count;
        }
    }

    private static class TypeAssignation {
        private Map<String, Annotation[]> properties = new HashMap<>();
        private Mock.Depth depth;

        TypeAssignation(PojoInfo pojoInfo) {
            depth = pojoInfo.getRowType().getAnnotation(Mock.Depth.class);
            for (PojoProperty pojoProperty : pojoInfo.getProperties()) {
                properties.put(pojoProperty.getName(), pojoProperty.getAnnotations());
            }
        }

        TypeAssignation(Annotation annotation) throws InvocationTargetException, IllegalAccessException {
            depth = annotation.annotationType().getAnnotation(Mock.Depth.class);
            List<Annotation> annotationList = new ArrayList<>();
            for (Method method : annotation.annotationType().getMethods()) {
                method.setAccessible(true);
                if (method.getReturnType().isAnnotation()) {
                    annotationList.add((Annotation) method.invoke(annotation));
                }
                annotationList.addAll(Arrays.asList(method.getAnnotations()));
                properties.put(method.getName(), annotationList.toArray(new Annotation[0]));
            }
        }

        TypeAssignation merge(TypeAssignation typeAssignation) {
            if (typeAssignation != null) {
                for (Map.Entry<String, Annotation[]> entry : typeAssignation.properties.entrySet()) {
                    if (!properties.containsKey(entry.getKey())) {
                        properties.put(entry.getKey(), entry.getValue());
                    } else {
                        properties.put(entry.getKey(), merge(properties.get(entry.getKey()), entry.getValue()));
                    }
                }
            }
            return this;
        }

        private Annotation[] merge(Annotation[] array1, Annotation[] array2) {
            List<Annotation> result = new ArrayList<>(Arrays.asList(array1));
            for (Annotation annotation : array2) {
                if (!result.contains(annotation)) {
                    result.add(annotation);
                }
            }
            return result.toArray(new Annotation[0]);
        }

        int getDepth() {
            return Math.max(1, depth == null ? DEFAULT_DEPTH : depth.value());
        }

        Annotation[] get(String propertyName) {
            Annotation[] annotations = properties.get(propertyName);
            return annotations == null ? new Annotation[0] : annotations;
        }
    }

    static class MockInvocationHandler implements net.sf.cglib.proxy.InvocationHandler, InvocationHandler {

        private final Map<String, Object> values;

        MockInvocationHandler(Map<String, Object> values) {
            this.values = values;
        }

        private String getPropertyName(Method method) {
            String name = method.getName();
            if (method.getDeclaringClass().equals(Object.class)) return null;
            if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class))
                return null;
            if (method.getParameterTypes().length > 0) return null;
            if (name.length() > 3 && name.startsWith("get")) {
                return Common.lowerFirstChar(name.substring(3));
            }
            if (name.length() > 2 && name.startsWith("is") && method.getReturnType().equals(boolean.class)) {
                return Common.lowerFirstChar(name.substring(2));
            }
            return null;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass().equals(Object.class)) {
                return method.invoke(this, args);
            }
            String propertyName = getPropertyName(method);
            if (propertyName == null)
                throw new MockException("not property getter method." + method.toGenericString());
            return values.get(propertyName);
        }
    }

}
