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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.coodex.util.Common.cast;

/**
 * @author Davidoff
 */
public class ReflectHelper {


    public static final ClassDecision NOT_NULL = new NotNullDecision();
    private static Logger log = LoggerFactory.getLogger(ReflectHelper.class);

    private ReflectHelper() {
    }

    public static <T extends Annotation> T getAnnotation(Class<T> annotationClass, AnnotatedElement element, Set<AnnotatedElement> checked) {
        if (checked.contains(element))
            return null;
        checked.add(element);
        T t = element.getAnnotation(annotationClass);
        if (t != null) return t;
        for (Annotation annotation : element.getAnnotations()) {
            t = getAnnotation(annotationClass, annotation.annotationType(), checked);
            if (t != null) return t;
        }
        return null;
    }

    public static <T extends Annotation> T getAnnotation(Class<T> annotationClass, AnnotatedElement... elements) {
        Set<AnnotatedElement> checked = new HashSet<>();
        for (AnnotatedElement element : elements) {
            T t = getAnnotation(annotationClass, element, checked);
            if (t != null) return t;
        }
        return null;
    }

    public static String getParameterName(Object executable, int index, String prefix) {
        if (executable instanceof Executable) {
            String parameterName = getParameterName((Executable) executable, index);
            return parameterName == null ? (prefix + index) : parameterName;
        } else {
            throw new IllegalArgumentException("none Executable object: " + executable);
        }
    }

//    private static String getMethodParameterName(Method method, int index, String prefix) {
//        String str = null;
//        try {
//            str = getParameterName(method, index);
//        } catch (Throwable th) {
////            str = prefix + index;
//        }
//        return Common.isBlank(str) ? (prefix + index) : str;
//    }

    public static String getParameterName(Method method, int index) {
        String s = getParameterNameByAnnotation(method.getParameterAnnotations(), index);
        return s == null ? getParameterNameByJava8(method, index) : s;
    }

//    private static String getParameterNameByJava8(Method method, int index) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
//        return getExecutableParameterNameByJava8(method, index);
////        Method getParameters = Method.class.getMethod("getParameters");
////        Object[] parameters = (Object[]) getParameters.invoke(method);
////        if (parameters != null) {
////            Class<?> methodParameterClass = Class.forName("java.lang.reflect.Parameter");
////            Method getName = methodParameterClass.getMethod("getName");
////            return (String) getName.invoke(parameters[index]);
////        }
////        return null;
//    }

//    private static String getConstructorParameterName(Constructor<?> constructor, int index, String prefix) {
//        try {
//            return getParameterName(constructor, index);
//        } catch (Throwable th) {
//            return prefix + index;
//        }
//    }

    private static String getParameterNameByAnnotation(Annotation[][] annotations, int index) {
        if (annotations == null || annotations.length < index) return null;

        for (Annotation annotation : annotations[index]) {
            if (annotation instanceof Parameter) {
                return ((Parameter) annotation).value();
            }
        }
        return null;
    }

    public static String getParameterName(Executable executable, int index) {
        String s = getParameterNameByAnnotation(executable.getParameterAnnotations(), index);

        return s == null ? getParameterNameByJava8(executable, index) : s;
    }

    private static String getParameterNameByJava8(Executable executable, int index) {
        return executable.getParameters()[index].getName();
    }

//    private static String getExecutableParameterNameByJava8(Executable executable, int index) {
//        return executable.getParameters()[index].getName();
////        Method getParameters = Method.class.getMethod("getParameters");
////        Object[] parameters = (Object[]) getParameters.invoke(executable);
////        if (parameters != null) {
////            Class<?> methodParameterClass = Class.forName("java.lang.reflect.Parameter");
////            Method getName = methodParameterClass.getMethod("getName");
////            return (String) getName.invoke(parameters[index]);
////        }
////        return null;
//    }

    public static Field[] getAllDeclaredFields(Class<?> clz) {
        return getAllDeclaredFields(clz, null);
    }

    public static Field[] getAllDeclaredFields(Class<?> clz,
                                               ClassDecision decision) {
        if (clz == null)
            throw new NullPointerException("class is NULL");
        if (decision == null) {
            decision = NOT_NULL;
        }
        Map<String, Field> fields = new HashMap<>();
        Class<?> clazz = clz;
        while (decision.determine(clazz)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields.values().toArray(new Field[0]);
    }

    public static Object invoke(Object obj, Method method, Object[] args)
            throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        if (obj == null)
            throw new NullPointerException("invoke target object is NULL.");

        if (method.getDeclaringClass().isAssignableFrom(obj.getClass())) {
            return method.invoke(obj, args);
        } else {
            return obj.getClass()
                    .getMethod(method.getName(), method.getParameterTypes())
                    .invoke(obj, args);
        }
    }

//    private static Collection<Class<?>> getClasses(String packageName,
//                                                  ClassNameFilter filter) {
//        ClassLoader classLoader = ReflectHelper.class.getClassLoader();
//
//        List<Class<?>> list = new ArrayList<Class<?>>();
////        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        // 枚举所有的符合的package
//        String path = packageName.replace('.', '/');
//        try {
//            Enumeration<URL> resources = classLoader.getResources(path);
//            while (resources.hasMoreElements()) {
//                URL url = resources.nextElement();
//
//                String resource = URLDecoder.decode(
//                        url.getFile().replace("+", "%2B")/* 缺陷？ */,
//                        System.getProperty("file.encoding"));
//
//                // 针对每一个匹配的包进行检索
//                int indexOfZipMarker = resource.indexOf('!');
//                if (indexOfZipMarker > 0) { // .zip, .jar
//
//                    list.addAll(loadFromZipFile(packageName, filter, new File(
//                            resource.substring(5 /* 去掉file协议头 */, indexOfZipMarker))));
//
//                } else {// 文件夹
//
//                    File dir;
//
//                    dir = new File(url.toURI());
//                    list.addAll(loadFromDirectory(packageName, filter, dir));
//
//                }
//            }
//        } catch (IOException e) {
//            log.warn("{}", e.getLocalizedMessage(), e);
//        } catch (URISyntaxException e) {
//            log.warn("{}", e.getLocalizedMessage(), e);
//        }
//        return list;
//    }

//    private static Collection<Class<?>> loadFromDirectory(
//            String packageName, ClassNameFilter filter, File dir) {
//
//        log.debug("Scan package[{}] in dir[{}]", packageName,
//                dir.getAbsolutePath());
//
//        List<Class<?>> result = new ArrayList<Class<?>>();
//        for (File f : dir.listFiles()) {
//            String fileName = f.getName();
//            if (f.isDirectory()) {
//                result.addAll(loadFromDirectory(packageName + "." + fileName,
//                        filter, f));
//            } else {
//
//                if (fileName.endsWith(".class")) {
//                    String className = packageName + "."
//                            + fileName.substring(0, fileName.length() - 6);
//                    try {
//                        if (filter == null || filter.accept(className))
//                            result.add(Class.forName(className));
//
//                    } catch (Throwable e) {
//                        log.debug("load Class {} fail. {}", className,
//                                e.getLocalizedMessage(), e);
//                    }
//                }
//            }
//        }
//        return result;
//    }
//
//    private static Collection<Class<?>> loadFromZipFile(
//            String packageName, ClassNameFilter filter, File zipFile) throws IOException {
//
//        List<Class<?>> list = new ArrayList<Class<?>>();
//        ZipFile zip = new ZipFile(zipFile);
//
//        log.debug("Scan package[{}] in [{}]", packageName,
//                zipFile.getAbsolutePath());
//
//        try {
//            Enumeration<? extends ZipEntry> entries = zip.entries();
//            while (entries.hasMoreElements()) {
//                ZipEntry entry = entries.nextElement();
//                String entryName = entry.getName().replace('/', '.');
//
//                // 此包中的class
//                if (entryName.startsWith(packageName)
//                        && entryName.endsWith(".class")) {
//                    String className = entryName
//                            .substring(0, entryName.length() - 6);
//                    try {
//                        if (filter == null || filter.accept(className))
//                            list.add(Class.forName(className));
//
//                    } catch (ClassNotFoundException e) {
//                        log.debug("load Class {} fail. {}", className,
//                                e.getLocalizedMessage(), e);
//                    }
//                }
//            }
//            return list;
//        } finally {
//            zip.close();
//        }
//    }

    private static String resourceToClassName(String resourceName) {
        if (resourceName.endsWith(".class")) {
            return resourceName.substring(0, resourceName.length() - 6).replace('/', '.');
        }
        return null;
    }

    private static String[] packageToPath(String[] packages) {
        if (packages == null || packages.length == 0) return new String[0];
        String[] paths = new String[packages.length];
        int i = 0;
        for (String p : packages) {
            paths[i++] = p == null ? "" : p.replace('.', '/');
        }
        return paths;
    }

    public static void foreachClass(final Processor processor, final ClassNameFilter filter, String... packages) {
        if (processor == null) return;
        Common.forEach((resource, resourceName) -> {
            String className = resourceToClassName(resourceName);
            try {
                processor.process(Class.forName(className));
            } catch (ClassNotFoundException e) {
                log.warn("load class fail. {}, {}", className, e.getLocalizedMessage());
            }
        }, (root, resourceName) -> {
            String className = resourceToClassName(resourceName);
            return className != null && filter.accept(className);
        }, packageToPath(packages));
    }

    @SuppressWarnings("unused")
    public static <T> T throwExceptionObject(Class<T> interfaceClass, final Supplier<Throwable> supplier) {
        return cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    throw supplier.get();
                }));
    }

    @SuppressWarnings("unused")
    public static <T> T throwExceptionObject(Class<T> interfaceClass, final Function<Method, Throwable> function) {
        return cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    throw function.apply(method);
                }));
    }

//    public static <T> T throwExceptionObject(Class<T> interfaceClass, final Throwable th) {
//        return cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
//                (proxy, method, args) -> {
//                    throw th;
//                }));
//    }

    public static String typeToCodeStr(Type type) {
        StringBuilder builder = new StringBuilder();
        if (type instanceof ParameterizedType) {
            builder.append(typeToCodeStr(((ParameterizedType) type).getRawType())).append("<");
            int l = ((ParameterizedType) type).getActualTypeArguments().length;
            for (int i = 0; i < l; i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(typeToCodeStr(((ParameterizedType) type).getActualTypeArguments()[i]));
            }
            builder.append(">");
        } else if (type instanceof Class) {
            if (((Class<?>) type).isArray()) {
                return typeToCodeStr(((Class<?>) type).getComponentType()) + "[]";
            } else {
                return ((Class<?>) type).getName();
            }
        } else if (type instanceof TypeVariable) {
            return ((TypeVariable<?>) type).getName();
        } else if (type instanceof GenericArrayType) {
            return typeToCodeStr(((GenericArrayType) type).getGenericComponentType()) + "[]";
        }
        return builder.toString();
    }

    private static Object invoke(Method method, Object first, Object[] objects, Object[] args) throws Throwable {
        if (first == null) throw new NullPointerException();
        if (method.getDeclaringClass().isAssignableFrom(first.getClass())) {
            return args == null || args.length == 0 ? method.invoke(first) : method.invoke(first, args);
        }
        for (Object o : objects) {
            if (o == null) throw new NullPointerException();
            if (method.getDeclaringClass().isAssignableFrom(o.getClass())) {
                return args == null || args.length == 0 ? method.invoke(o) : method.invoke(o, args);
            }
        }
        throw new RuntimeException("method not found in all objects: " + method.getName());
    }

    public static Class<?>[] getAllInterfaces(Class<?> clz) {
        Collection<Class<?>> coll = new HashSet<>();
        addInterfaceTo(clz, coll);
        return coll.toArray(new Class<?>[0]);
    }

    private static void addInterfaceTo(Class<?> clz, Collection<Class<?>> coll) {
        if (clz == null) return;
        if (coll.contains(clz)) return;
        if (clz.isInterface()) {
            coll.add(clz);
        }
        addInterfaceTo(clz.getSuperclass(), coll);
        for (Class<?> c : clz.getInterfaces()) {
            addInterfaceTo(c, coll);
        }
    }

//    @SuppressWarnings("rawtypes")
//    private static Collection<Class> getAllInterface(Class... aClass) {
//        if (aClass == null || aClass.length == 0) return Collections.emptySet();
//        Collection<Class> collection = new HashSet<Class>();
//        for (Class clz : aClass) {
//            addInterfaceTo(clz, collection);
//        }
//        return collection;
//    }

//    @Deprecated
//    public static <T> T extend(final T o, final Object... objects) {
//        return extendInterface(o, objects);
//    }

    public static boolean isAssignable(Class<?> from, Class<?> to) {
        if (from.isArray() && to.isArray())
            return Objects.equals(from, to);//isAssignable(from.getComponentType(), to.getComponentType());
        if (from.isArray() || to.isArray())
            return false;
        return to.isAssignableFrom(from);
    }

    public static boolean isMatch(Type instanceType, Type serviceType) {
        boolean match = isMatchForDebug(instanceType, serviceType);
        if (log.isDebugEnabled()) {
            log.debug("match: {}\ninstance: {}\nservice: {} ", match, instanceType, serviceType);
        }
        return match;
    }

//    private static String getTypeInfo(Type type){
//        StringBuilder builder = new StringBuilder();
//        builder.append(type.getTypeName())
//        return builder.toString();
//    }

    private static boolean isMatchForDebug(Type instanceType, Type serviceType) {
        if (Objects.equals(instanceType, serviceType)) return true;
//        if (serviceType instanceof TypeVariable) return true; // todo bound check.
        Class<?> instanceClass = GenericTypeHelper.typeToClass(instanceType);
        Class<?> serviceClass = GenericTypeHelper.typeToClass(serviceType);

        // 实例类型与服务类型的class不匹配
        if (serviceClass != null && instanceClass != null && !isAssignable(instanceClass, serviceClass)) return false;

        if (serviceClass != null && serviceType instanceof ParameterizedType) {
            ParameterizedType parameterizedServiceType = (ParameterizedType) serviceType;
            for (int i = 0; i < parameterizedServiceType.getActualTypeArguments().length; i++) {
                if (!isMatch(
                        GenericTypeHelper.solveFromType(serviceClass.getTypeParameters()[i], instanceType),
                        parameterizedServiceType.getActualTypeArguments()[i]))
                    return false;
            }
            return true;
        } else
            return !(serviceType instanceof GenericArrayType);// 均为泛型数组，则必须完全相同，在第一行已处理
    }

    /**
     * @param o       object
     * @param objects 需要扩展出来的对象，只扩展接口
     * @param <S>     必须是接口
     * @param <T>     extends S
     * @return 扩展后的对象
     */
    @SuppressWarnings("unused")
    public static <S, T extends S> S extendInterface(final T o, final Object... objects) {
        if (o == null) return null;
        if (objects == null || objects.length == 0) return o;
        Set<Class<?>> interfaces = new HashSet<>();
        addInterfaceTo(o.getClass(), interfaces);
        for (Object x : objects) {
            if (x != null) {
                addInterfaceTo(x.getClass(), interfaces);
            }
        }
        if (interfaces.size() == 0) return o;

        return cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces.toArray(new Class<?>[0]),
                (proxy, method, args) -> invoke(method, o, objects, args)));
    }

    public interface Processor {
        void process(Class<?> serviceClass);
    }

    public interface ClassDecision {
        boolean determine(Class<?> clz);
    }

    public static class MethodParameter {

        private final Method method;
        private final int index;
        private String name;
        private Annotation[] annotations;
        private Class<?> type;
        private Type genericType;


        public MethodParameter(Method method, int index) {
            this.method = method;
            this.index = index;

            annotations = method.getParameterAnnotations()[index];
            type = method.getParameterTypes()[index];
            genericType = method.getGenericParameterTypes()[index];

            name = getParameterName(method, index, "p");
//            try {
//                getName = getParameterName();
//            } catch (Throwable th) {
//            }
//            if (Common.isBlank(getName))
//                getName = "arg" + index;

        }

//        /**
//         * 使用java 8的getParameters来获取参数名，编译时，使用jdk的javac，-parameters。在java8环境中运行有效
//         *
//         * @return
//         * @throws NoSuchMethodException
//         * @throws InvocationTargetException
//         * @throws IllegalAccessException
//         * @throws ClassNotFoundException
//         */
//        private String getParameterName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
//            return ReflectHelper.getParameterName(method, index);
////
////            Method getParameters = Method.class.getMethod("getParameters");
////            Object[] parameters = (Object[]) getParameters.invoke(getMethod());
////            if (parameters != null) {
////                Class<?> methodParameterClass = Class.forName("java.lang.reflect.Parameter");
////                Method getName = methodParameterClass.getMethod("getName");
////                return (String) getName.invoke(parameters[index]);
////            }
////            return null;
//        }

        public Class<?> getType() {
            return type;
        }

        public Type getGenericType() {
            return genericType;
        }

        public Method getMethod() {
            return method;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public Annotation[] getAnnotations() {
            return annotations;
        }


        public <T> T getAnnotation(Class<T> annotationClass) {
            if (annotationClass == null) throw new IllegalArgumentException("annotationClass is NULL.");
            if (annotations == null) return null;
            for (Annotation annotation : annotations) {
                if (annotationClass.isAssignableFrom(annotation.getClass()))
                    return Common.cast(annotation);
            }
            return null;
        }
    }

    private static class NotNullDecision implements ClassDecision {
        //      @Override
        public boolean determine(Class<?> clz) {
            return clz != null;
        }
    }

    @SuppressWarnings("unused")
    private static class AllObjectDecision implements ClassDecision {
        //      @Override
        public boolean determine(Class<?> clz) {
            return clz != null && clz != Object.class;
        }

    }

    @SuppressWarnings("unused")
    private static class AllObjectExceptJavaSDK implements ClassDecision {
        //      @Override
        public boolean determine(Class<?> clz) {
            return clz != null && !clz.getPackage().getName().startsWith("java");
        }

    }

}
