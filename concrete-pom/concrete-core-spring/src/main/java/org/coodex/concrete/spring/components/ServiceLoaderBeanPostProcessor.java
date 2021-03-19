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
//import javassist.bytecode.annotation.ClassMemberValue;
//import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
//import org.coodex.util.DefaultService;
//import org.coodex.util.GenericTypeHelper;
//import org.coodex.util.ServiceLoader;
//import org.coodex.util.ServiceLoaderImpl;
//
//import javax.inject.Named;
//import java.lang.reflect.AnnotatedElement;
//import java.lang.reflect.Field;
//import java.lang.reflect.Proxy;
//import java.lang.reflect.Type;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.*;
//import static org.coodex.util.Common.getUUIDStr;
//import static org.coodex.util.GenericTypeHelper.toReference;
//import static org.coodex.util.GenericTypeHelper.typeToClass;
//
///**
// * 支持coodex spi注入：ServiceLoader&lt;T&gt;
// *
// * @see org.coodex.util.ServiceLoader
// */
//@Named
//public class ServiceLoaderBeanPostProcessor extends AbstractInjectableBeanPostProcessor<ServiceLoaderKey> {
//
//    @SuppressWarnings("rawtypes")
//    static Map<String, ServiceLoader> cache = new HashMap<>();
//
//    static javassist.bytecode.annotation.Annotation spiAnnotation(Class<?> defaultService, ConstPool constPool) {
//        javassist.bytecode.annotation.Annotation annotation =
//                new javassist.bytecode.annotation.Annotation(DefaultService.class.getName(), constPool);
//        annotation.addMemberValue("value", new ClassMemberValue(defaultService.getName(), constPool));
//        return annotation;
//    }
//
//    static Class<?> getDefaultServiceClass(AnnotatedElement annotatedElement) {
//        DefaultService defaultService = annotatedElement.getAnnotation(DefaultService.class);
//        return defaultService == null ? null : defaultService.value();
//    }
//
//    @Override
//    protected boolean accept(Field field) {
//        return ServiceLoader.class.equals(field.getType());
//    }
//
//    @Override
//    protected Class<?> getInjectClass(ServiceLoaderKey key, Class<?> beanClass) {
//        try {
//            String className = ServiceLoaderBeanPostProcessor.class.getPackage().getName() + "."
//                    + String.format("ServiceLoaderBean$$CBC$$%08X", getIndex());
//            Type serviceType = toReference(ServiceLoader.class.getTypeParameters()[0], key.getServiceType());
//
//            try {
//                final Object defaultService = key.getDefaultServiceClass() == null ? null :
//                        Throwable.class.isAssignableFrom(key.getDefaultServiceClass()) ?
//                                Proxy.newProxyInstance(
//                                        ServiceLoaderBeanPostProcessor.class.getClassLoader(),
//                                        new Class[]{
//                                                GenericTypeHelper.typeToClass(serviceType)
//                                        },
//                                        (proxy, method, args) -> {
//                                            throw new RuntimeException("no default service found for: " + serviceType.toString());
//                                        }
//                                )
//                                : key.getDefaultServiceClass().newInstance();
//
//                cache.put(className, new ServiceLoaderImpl() {
//                    @Override
//                    protected Type getServiceType() {
//                        return serviceType;
//                    }
//                    //                    @Override
////                    protected Class getInterfaceClass() {
////                        return typeToClass(serviceType);
////                    }
//
//                    @Override
//                    public Object getDefault() {
//                        return defaultService;
//                    }
//                });
//            } catch (Throwable e) {
//                throw new RuntimeException(e);
//            }
//
//            ClassPool classPool = JavassistHelper.getClassPool(ServiceLoaderBeanPostProcessor.class);
//            CtClass ctClass = classPool.makeClass(className);
//            ctClass.setInterfaces(new CtClass[]{
//                    classPool.getOrNull(ServiceLoader.class.getName())
//            });
//            ClassFile classFile = ctClass.getClassFile();
//            classFile.setVersionToJava5();
//            ConstPool constPool = classFile.getConstPool();
//            String sig = new SignatureAttribute.ClassSignature(
//                    null,
//                    null,
//                    new SignatureAttribute.ClassType[]{
//                            classType(
//                                    ServiceLoader.class.getName(),
//                                    serviceType
//                            ),
//                    }
//            ).encode();
//            ctClass.setGenericSignature(sig);
//
//            buildMethods(classPool, ctClass,
//                    ServiceLoader.class, key.getServiceType(), beanClass,
//                    ServiceLoaderBeanPostProcessor.class.getName() + ".cache.get(this.getClass().getName())");
//
//            if (key.getDefaultServiceClass() != null) {
//                classFile.addAttribute(aggregate(constPool, spiAnnotation(key.getDefaultServiceClass(), constPool)));
//            } else {
//                classFile.addAttribute(aggregate(constPool, primary(constPool)));
//            }
//
//            return IS_JAVA_9_AND_LAST.get() ? ctClass.toClass(ServiceLoaderBeanPostProcessor.class) : ctClass.toClass();
//        } catch (CannotCompileException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected String newBeanName() {
//        return "ServiceLoader_" + getUUIDStr();
//    }
//
//    @Override
//    protected ServiceLoaderKey getKey(Class<?> beanClass, Field field) {
//        return new ServiceLoaderKey(getDefaultServiceClass(field), toReference(field.getGenericType(), beanClass));
//    }
//}
