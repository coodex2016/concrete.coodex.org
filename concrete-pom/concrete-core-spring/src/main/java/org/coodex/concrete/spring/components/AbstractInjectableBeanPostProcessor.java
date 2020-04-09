/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.spring.components;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Primary;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.toCtClass;
import static org.coodex.util.Common.rte;
import static org.coodex.util.GenericTypeHelper.toReference;

public abstract class AbstractInjectableBeanPostProcessor<K extends InjectInfoKey> extends InstantiationAwareBeanPostProcessorAdapter {

    private final static Logger log = LoggerFactory.getLogger(AbstractInjectableBeanPostProcessor.class);
    private static AtomicLong index = new AtomicLong(0);

    //    @Inject
//    private ListableBeanFactory listableBeanFactory;
//    @Inject
//    private BeanDefinitionRegistry beanDefinitionRegistry;
    @Inject
    private DefaultListableBeanFactory defaultListableBeanFactory;
    @SuppressWarnings("unchecked")
    private Class<? extends Annotation>[] injectableAnnotations = new Class[]{Autowired.class, Inject.class};
    private Map<InjectInfoKey, Class<?>> injectedCache = new HashMap<>();

    protected boolean isInjectable(Field field) {
        for (Class<? extends Annotation> clz : injectableAnnotations) {
            if (field.getAnnotation(clz) != null) return true;
        }
        return false;
    }

    protected long getIndex() {
        return index.incrementAndGet();
    }

    protected javassist.bytecode.annotation.Annotation primary(ConstPool constPool) {
        return new javassist.bytecode.annotation.Annotation(Primary.class.getName(), constPool);
    }

    @Override
    public final boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        scan(bean, bean.getClass(), beanName, bean.getClass());
        return super.postProcessAfterInstantiation(bean, beanName);
    }


    //    @Override
//    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
//        scan(null, beanClass, beanName, beanClass);
//        return super.postProcessBeforeInstantiation(beanClass, beanName);
//    }

    //    protected abstract boolean isInjected(Class<?> beanClass, Field injectField);
//
//    protected abstract InjectInfo getInjectedBean(Class<?> beanClass, Field injectField);

    protected abstract boolean accept(Field field);

    protected abstract Class<?> getInjectClass(K key, Class<?> beanClass);

    protected abstract String newBeanName();

    protected abstract K getKey(Class<?> beanClass, Field field);

    protected boolean injectNoneAnnotation() {
        return false;
    }

    //    @Deprecated
//    protected abstract Object getBeanInstanceWithFieldSet(Class<?> beanClass, Field injectField);
    protected void buildMethods(
            ClassPool classPool, CtClass ctClass,
            Class<?> proxyClass, Type injectType,
            Class<?> contextClass, String instanceGetter) throws CannotCompileException {
        // methods
        for (Method method : proxyClass.getMethods()) {
            boolean voidReturn = method.getReturnType().equals(void.class);
            CtMethod ctMethod = new CtMethod(
                    voidReturn ? CtClass.voidType :
                            classPool.getOrNull(method.getReturnType().getName()),
                    method.getName(),
                    toCtClass(method.getParameterTypes(), classPool),
                    ctClass
            );
            List<SignatureAttribute.Type> parameters = new ArrayList<>();

            for (int i = 0; i < method.getGenericParameterTypes().length; i++) {
                parameters.add(JavassistHelper.classType(
                        method.getParameterTypes()[i].equals(Class.class) ? Class.class :
                                toReference(method.getGenericParameterTypes()[i], injectType),
                        contextClass));
            }

            ctMethod.setGenericSignature(new SignatureAttribute.MethodSignature(
                    null,
                    parameters.toArray(new SignatureAttribute.Type[0]),
                    voidReturn ? null : JavassistHelper.classType(
                            toReference(method.getGenericReturnType(), injectType),
                            contextClass),
                    null
            ).encode());
            StringBuilder body = new StringBuilder();
            body.append('{');
            if (!voidReturn) {
                body.append("return ");
            }

            body.append("((")
                    .append(proxyClass.getName())
                    .append(")").append(instanceGetter).append(")").append(".");

            body.append(method.getName())
                    .append('(');
            if (method.getParameterTypes().length > 0) {
                body.append("$$");
            }
            body.append(");");

            body.append("}");
            ctMethod.setBody(body.toString());

            ctClass.addMethod(ctMethod);
        }
    }

    private synchronized void scan(Object bean, Class<?> beanClass, String beanName, Class<?> scanClass) {
        if (Object.class.equals(scanClass)) return;
        for (Field field : scanClass.getDeclaredFields()) {
            if (accept(field)) {
                if (bean == null)
                    log.debug("process {} {}: {} extends {}",
                            toReference(field.getGenericType(), beanClass),
                            field.getName(), beanClass.getName(), scanClass.getName());
                K key = getKey(beanClass, field);
                if (!injectedCache.containsKey(key)) {
                    Class<?> injectClass = getInjectClass(key, beanClass);
                    String newBeanName = newBeanName();
                    BeanDefinition beanDefinition = new RootBeanDefinition(injectClass);
                    if (injectClass.getAnnotation(Primary.class) != null)
                        beanDefinition.setPrimary(true);

                    getDefaultListableBeanFactory().registerBeanDefinition(newBeanName, beanDefinition);
//                            getBeanFactory().registerSingleton(newBeanName, instance);
                    if (log.isInfoEnabled()) {
                        StringJoiner joiner = new StringJoiner(", ");
                        Arrays.stream(injectClass.getGenericInterfaces())
                                .map(type -> toReference(type, injectClass))
                                .forEach(type -> joiner.add(type.toString()));
                        String interfaces = joiner.length() > 0 ? (" implements " + joiner.toString()) : "";
                        log.info("new bean registered: {}, {} extends {}{}",
                                newBeanName, injectClass.getName(),
                                toReference(injectClass.getGenericSuperclass(), injectClass),
                                interfaces);
                    }
                    injectedCache.put(key, injectClass);
                }


                if (bean == null) continue;

                if (isInjectable(field)) {
                    log.info("{} {} {} injected.", beanName, toReference(field.getGenericType(), beanClass), field.getName());
                } else if (injectNoneAnnotation()) { // 向前兼容,0.4.1开始废弃
                    try {
                        field.setAccessible(true);
                        field.set(bean, injectedCache.get(key).newInstance());
                        log.warn("{} {} {} injected. use @Inject plz.", beanName, toReference(field.getGenericType(), beanClass), field.getName());
                    } catch (Throwable e) {
                        throw rte(e);
                    }
                }
            }
        }

        scan(bean, beanClass, beanName, scanClass.getSuperclass());
    }

//    private  void scan(Object bean, Class<?> beanClass, String beanName) {
////        for (Field field : ReflectHelper.getAllDeclaredFields(beanClass)) {
////            if (accept(field)) {
////                K key = getKey(beanClass, field);
////                if (!injectedCache.containsKey(key)) {
////                    Class<?> injectClass = getInjectClass(key, beanClass);
////                    String newBeanName = newBeanName();
////                    BeanDefinition beanDefinition = new RootBeanDefinition(injectClass);
////                    if (injectClass.getAnnotation(Primary.class) != null)
////                        beanDefinition.setPrimary(true);
////
////                    getDefaultListableBeanFactory().registerBeanDefinition(newBeanName, beanDefinition);
//////                            getBeanFactory().registerSingleton(newBeanName, instance);
////                    if (log.isInfoEnabled()) {
////                        StringJoiner joiner = new StringJoiner(", ");
////                        Arrays.stream(injectClass.getGenericInterfaces())
////                                .map(type -> toReference(type, injectClass))
////                                .forEach(type -> joiner.add(type.toString()));
////                        String interfaces = joiner.length() > 0 ? (" implements " + joiner.toString()) : "";
////                        log.info("new bean registered: {}, {} extends {}{}",
////                                newBeanName, injectClass.getName(),
////                                toReference(injectClass.getGenericSuperclass(), injectClass),
////                                interfaces);
////                    }
////                    injectedCache.put(key, injectClass);
////
////                }
////                if (bean == null) return;
////
////                if (isInjectable(field)) {
////                    log.info("{} {} {} injected.", beanName, toReference(field.getGenericType(), beanClass), field.getName());
////                } else if (injectNoneAnnotation()) { // 向前兼容,0.4.1开始废弃
////                    try {
////                        field.setAccessible(true);
////                        field.set(bean, injectedCache.get(key).newInstance());
////                        log.warn("{} {} {} injected. use @Inject plz.", beanName, toReference(field.getGenericType(), beanClass), field.getName());
////                    } catch (Throwable e) {
////                        throw runtimeException(e);
////                    }
////                }
////            }
////        }
//
////        for (Method method : beanClass.getMethods()) {
////
////        }
//    }

    protected DefaultListableBeanFactory getDefaultListableBeanFactory() {
        return defaultListableBeanFactory;
    }
}
