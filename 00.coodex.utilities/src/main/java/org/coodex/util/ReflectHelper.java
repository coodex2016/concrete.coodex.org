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

/**
 *
 */
package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author davidoff
 */
public class ReflectHelper {

    public interface Processor {
        void process(Class<?> serviceClass);
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
//                name = getParameterName();
//            } catch (Throwable th) {
//            }
//            if (Common.isBlank(name))
//                name = "arg" + index;

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


        @SuppressWarnings("unchecked")
        public <T> T getAnnotation(Class<T> annotationClass) {
            if (annotationClass == null) throw new IllegalArgumentException("annotationClass is NULL.");
            if (annotations == null) return null;
            for (Annotation annotation : annotations) {
                if (annotationClass.isAssignableFrom(annotation.getClass()))
                    return (T) annotation;
            }
            return null;
        }
    }

    public static String getParameterName(Method method, int index, String prefix){
        try {
            return getParameterName(method, index);
        } catch (Throwable th) {
            return prefix + index;
        }
    }

    public static String getParameterName(Method method, int index) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {

        Method getParameters = Method.class.getMethod("getParameters");
        Object[] parameters = (Object[]) getParameters.invoke(method);
        if (parameters != null) {
            Class<?> methodParameterClass = Class.forName("java.lang.reflect.Parameter");
            Method getName = methodParameterClass.getMethod("getName");
            return (String) getName.invoke(parameters[index]);
        }
        return null;
    }

    public static String getParameterName(Constructor constructor, int index, String prefix){
        try {
            return getParameterName(constructor, index);
        }catch (Throwable th){
            return prefix + index;
        }
    }

    public static String getParameterName(Constructor constructor, int index) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {

        Method getParameters = Method.class.getMethod("getParameters");
        Object[] parameters = (Object[]) getParameters.invoke(constructor);
        if (parameters != null) {
            Class<?> methodParameterClass = Class.forName("java.lang.reflect.Parameter");
            Method getName = methodParameterClass.getMethod("getName");
            return (String) getName.invoke(parameters[index]);
        }
        return null;
    }



    private ReflectHelper() {
    }

    private static Logger log = LoggerFactory.getLogger(ReflectHelper.class);

    public static final ClassDecision NOT_NULL = new NotNullDecision();
    public static final ClassDecision ALL_OBJECT = new AllObjectDecision();
    public static final ClassDecision ALL_OBJECT_EXCEPT_JDK = new AllObjectExceptJavaSDK();

    public interface ClassDecision {
        boolean determine(Class<?> clz);
    }

    private static class NotNullDecision implements ClassDecision {
        //      @Override
        public boolean determine(Class<?> clz) {
            return clz != null;
        }
    }

    private static class AllObjectDecision implements ClassDecision {
        //      @Override
        public boolean determine(Class<?> clz) {
            return clz != null && clz != Object.class;
        }

    }

    private static class AllObjectExceptJavaSDK implements ClassDecision {
        //      @Override
        public boolean determine(Class<?> clz) {
            return clz != null && !clz.getPackage().getName().startsWith("java");
        }

    }

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
        Map<String, Field> fields = new HashMap<String, Field>();
        Class<?> clazz = clz;
        while (decision.determine(clazz)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                if (!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields.values().toArray(new Field[0]);
    }

    public static boolean belong(Method method, Class<?> clz) {
        try {
            Method m = clz.getMethod(method.getName(), method.getParameterTypes());
            return m != null;
        } catch (SecurityException e) {
            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }
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

    public static Collection<Class<?>> getClasses(String packageName) {
        return getClasses(packageName, null);
    }

    public static Collection<Class<?>> getClasses(String packageName,
                                                  ClassNameFilter filter) {
        ClassLoader classLoader = ReflectHelper.class.getClassLoader();

        List<Class<?>> list = new ArrayList<Class<?>>();
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 枚举所有的符合的package
        String path = packageName.replace('.', '/');
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                String resource = URLDecoder.decode(
                        url.getFile().replace("+", "%2B")/* 缺陷？ */,
                        System.getProperty("file.encoding"));

                // 针对每一个匹配的包进行检索
                int indexOfZipMarker = resource.indexOf('!');
                if (indexOfZipMarker > 0) { // .zip, .jar

                    list.addAll(loadFromZipFile(packageName, filter, new File(
                            resource.substring(5 /* 去掉file协议头 */, indexOfZipMarker))));

                } else {// 文件夹

                    File dir;

                    dir = new File(url.toURI());
                    list.addAll(loadFromDirectory(packageName, filter, dir));

                }
            }
        } catch (IOException e) {
            log.warn("{}", e.getLocalizedMessage(), e);
        } catch (URISyntaxException e) {
            log.warn("{}", e.getLocalizedMessage(), e);
        }
        return list;
    }

    private static Collection<Class<?>> loadFromDirectory(
            String packageName, ClassNameFilter filter, File dir) {

        log.debug("Scan package[{}] in dir[{}]", packageName,
                dir.getAbsolutePath());

        List<Class<?>> result = new ArrayList<Class<?>>();
        for (File f : dir.listFiles()) {
            String fileName = f.getName();
            if (f.isDirectory()) {
                result.addAll(loadFromDirectory(packageName + "." + fileName,
                        filter, f));
            } else {

                if (fileName.endsWith(".class")) {
                    String className = packageName + "."
                            + fileName.substring(0, fileName.length() - 6);
                    try {
                        if (filter == null || filter.accept(className))
                            result.add(Class.forName(className));

                    } catch (Throwable e) {
                        log.debug("load Class {} fail. {}", className,
                                e.getLocalizedMessage(), e);
                    }
                }
            }
        }
        return result;
    }

    private static Collection<Class<?>> loadFromZipFile(
            String packageName, ClassNameFilter filter, File zipFile) throws IOException {

        List<Class<?>> list = new ArrayList<Class<?>>();
        ZipFile zip = new ZipFile(zipFile);

        log.debug("Scan package[{}] in [{}]", packageName,
                zipFile.getAbsolutePath());

        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName().replace('/', '.');

                // 此包中的class
                if (entryName.startsWith(packageName)
                        && entryName.endsWith(".class")) {
                    String className = entryName
                            .substring(0, entryName.length() - 6);
                    try {
                        if (filter == null || filter.accept(className))
                            list.add(Class.forName(className));

                    } catch (ClassNotFoundException e) {
                        log.debug("load Class {} fail. {}", className,
                                e.getLocalizedMessage(), e);
                    }
                }
            }
            return list;
        } finally {
            zip.close();
        }
    }

    public static void foreachClass(Processor processor, ClassNameFilter filter, String... packages) {
        if (processor == null) return;
        Set<Class> submittedClasses = new HashSet<Class>();
        try {
            for (String pkg : packages) {
                for (Class clazz : ReflectHelper.getClasses(pkg, filter)) {
                    if (!submittedClasses.contains(clazz)) {
                        processor.process(clazz);
                        submittedClasses.add(clazz);
                    }
                }
            }
        } finally {
            submittedClasses.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T throwExceptionObject(Class<T> interfaceClass, final Throwable th) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                throw th;
            }
        });
    }

}
