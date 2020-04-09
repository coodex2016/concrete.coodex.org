///*
// * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.coodex.concrete.spring.components;
//
//import javassist.CannotCompileException;
//import javassist.ClassPool;
//import javassist.CtClass;
//import javassist.bytecode.ClassFile;
//import javassist.bytecode.ConstPool;
//import javassist.bytecode.SignatureAttribute;
//import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
//import org.coodex.util.GenericTypeHelper;
//import org.coodex.util.SelectableService;
//import org.coodex.util.SelectableServiceLoader;
//import org.coodex.util.SelectableServiceLoaderImpl;
//
//import javax.inject.Named;
//import java.lang.reflect.Field;
//import java.lang.reflect.Proxy;
//import java.lang.reflect.Type;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.*;
//import static org.coodex.concrete.spring.components.ServiceLoaderBeanPostProcessor.getDefaultServiceClass;
//import static org.coodex.concrete.spring.components.ServiceLoaderBeanPostProcessor.spiAnnotation;
//import static org.coodex.util.Common.getUUIDStr;
//import static org.coodex.util.GenericTypeHelper.toReference;
//import static org.coodex.util.GenericTypeHelper.typeToClass;
//
//@Named
//public class SelectableServiceLoaderBeanPostProcessor extends AbstractInjectableBeanPostProcessor<SelectableServiceLoaderKey> {
//    @SuppressWarnings("rawtypes")
//    static Map<String, SelectableServiceLoader> cache = new HashMap<>();
//
//    @Override
//    protected boolean accept(Field field) {
//        return field.getType().equals(SelectableServiceLoader.class);
//    }
//
//    @Override
//    protected Class<?> getInjectClass(SelectableServiceLoaderKey key, Class<?> beanClass) {
//        try {
//            String className = ServiceLoaderBeanPostProcessor.class.getPackage().getName() + "."
//                    + String.format("SelectableServiceLoaderBean$$CBC$$%08X", getIndex());
////            Type serviceType = toReference(ServiceLoader.class.getTypeParameters()[0], key.getServiceType());
//
//            try {
//                final Object defaultService = key.getDefaultServiceClass() == null ? null :
//                        Throwable.class.isAssignableFrom(key.getDefaultServiceClass()) ?
//                                Proxy.newProxyInstance(
//                                        ServiceLoaderBeanPostProcessor.class.getClassLoader(),
//                                        new Class[]{
//                                                GenericTypeHelper.typeToClass(key.getServiceType())
//                                        },
//                                        (proxy, method, args) -> {
//                                            if (method.getDeclaringClass().equals(Object.class)) {
//                                                return method.invoke(this, args);
//                                            } else if (method.getDeclaringClass().equals(SelectableService.class)) {
//                                                return true;
//                                            } else
//                                                throw new RuntimeException("no default service found for: " + key.getServiceType().toString());
//                                        }
//                                )
//                                : key.getDefaultServiceClass().newInstance();
//
//                //noinspection rawtypes,unchecked
//                cache.put(className, new SelectableServiceLoaderImpl((SelectableService) defaultService) {
//                    @Override
//                    protected Type getServiceType() {
//                        return key.getServiceType();
//                    }
//
//                    @Override
//                    protected Type getParameterType() {
//                        return key.getParamType();
//                    }
//
//                    //                    @Override
////                    protected Class getParamType() {
////                        return typeToClass(key.getParamType());
////                    }
////
////                    @Override
////                    protected Class getInterfaceClass() {
////                        return typeToClass(key.getServiceType());
////                    }
//                });
//            } catch (Throwable e) {
//                throw new RuntimeException(e);
//            }
//
//            ClassPool classPool = JavassistHelper.getClassPool(SelectableServiceLoaderBeanPostProcessor.class);
//            CtClass ctClass = classPool.makeClass(className);
//            ctClass.setInterfaces(new CtClass[]{
//                    classPool.getOrNull(SelectableServiceLoader.class.getName())
//            });
//            ClassFile classFile = ctClass.getClassFile();
//            classFile.setVersionToJava5();
//            ConstPool constPool = classFile.getConstPool();
//            String sig = new SignatureAttribute.ClassSignature(
//                    null,
//                    null,
//                    new SignatureAttribute.ClassType[]{
//                            classType(
//                                    SelectableServiceLoader.class.getName(),
//                                    key.getParamType(),
//                                    key.getServiceType()
//                            ),
//                    }
//            ).encode();
//            ctClass.setGenericSignature(sig);
//
//            buildMethods(classPool, ctClass, SelectableServiceLoader.class,
//                    GenericTypeHelper.buildParameterizedType(SelectableServiceLoader.class, key.getParamType(), key.getServiceType()), beanClass,
//                    SelectableServiceLoaderBeanPostProcessor.class.getName() + ".cache.get(this.getClass().getName())");
//
//            if (key.getDefaultServiceClass() != null) {
//                classFile.addAttribute(aggregate(constPool, spiAnnotation(key.getDefaultServiceClass(), constPool)));
//            } else {
//                classFile.addAttribute(aggregate(constPool, primary(constPool)));
//            }
//
//            return IS_JAVA_9_AND_LAST.get() ? ctClass.toClass(SelectableServiceLoaderBeanPostProcessor.class) : ctClass.toClass();
//        } catch (CannotCompileException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected String newBeanName() {
//        return "SelectableServiceLoader_" + getUUIDStr();
//    }
//
//    @Override
//    protected SelectableServiceLoaderKey getKey(Class<?> beanClass, Field field) {
//        Type x = toReference(field.getGenericType(), beanClass);
//        return new SelectableServiceLoaderKey(
//                getDefaultServiceClass(field),
//                toReference(SelectableServiceLoader.class.getTypeParameters()[1], x),
//                toReference(SelectableServiceLoader.class.getTypeParameters()[0], x)
//        );
//    }
//}
