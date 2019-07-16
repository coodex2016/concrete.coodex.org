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

package org.coodex.concrete.spring.components;

import javassist.*;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.StringMemberValue;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.concrete.message.AbstractTopic;
import org.coodex.concrete.message.Queue;
import org.coodex.concrete.message.TopicKey;
import org.coodex.concrete.message.Topics;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.*;
import static org.coodex.util.GenericTypeHelper.toReference;
import static org.coodex.util.Common.runtimeException;


@Named
public class TopicBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    private final static Logger log = LoggerFactory.getLogger(TopicBeanPostProcessor.class);
    private static AtomicLong index = new AtomicLong(0);
    private Set<TopicKey> injected = new HashSet<TopicKey>();
    @Inject
    private DefaultListableBeanFactory defaultListableBeanFactory;
    private Class<? extends Annotation>[] injectableAnnotations = new Class[]{Inject.class};

    private boolean isInjectable(Field field) {
        for (Class<? extends Annotation> clz : injectableAnnotations) {
            if (field.getAnnotation(clz) != null) return true;
        }
        return false;
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        scan(bean, bean.getClass(), beanName);
        return super.postProcessAfterInstantiation(bean, beanName);
    }

    private boolean isTopic(Field field) {
        Class c = field.getType();
        return c.isInterface() && AbstractTopic.class.isAssignableFrom(c);
    }

    private void scan(Object bean, Class<?> beanClass, String beanName) {
        for (Field field : ReflectHelper.getAllDeclaredFields(beanClass)) {
            if (/*isInjectable(field) &&*/ isTopic(field)) {
                Queue queue = field.getAnnotation(Queue.class);
                String queueName = queue == null ? null : queue.value();
                Type topicType = toReference(field.getGenericType(), beanClass);
                if (isInjectable(field)) {
                    registerBean(topicType, queueName, beanClass);
                } else {
                    AbstractTopic topic = Topics.get(topicType, queueName);
                    try {
                        field.setAccessible(true);
                        field.set(bean, topic);
                        log.warn("{} {} {} injected. use @Inject plz.", beanName, topicType, field.getName());
                    } catch (Throwable e) {
                        throw runtimeException(e);
                    }
                }
//                if (!topicInstance.contains(topic)) {
//                    synchronized (this) {
//                        if (!topicInstance.contains(topic)) {
//
//                            registerBean(topicType, queueName, beanClass);
//                            topicInstance.add(topic);
//
//                        }
//                    }
//                }
            }
        }
    }

    private javassist.bytecode.annotation.Annotation queue(String queueName, ConstPool constPool) {
        javassist.bytecode.annotation.Annotation annotation =
                new javassist.bytecode.annotation.Annotation(Queue.class.getName(), constPool);
        annotation.addMemberValue("value", new StringMemberValue(queueName, constPool));
        return annotation;
    }

    //
//    private String messageType(Type messageType) {
//        StringBuilder builder = new StringBuilder();
//        if (messageType instanceof Class) {
//            builder.append(classType((Class) messageType));
//        } else if (messageType instanceof ParameterizedType) {
//            builder.append(parameterizedType((ParameterizedType) messageType));
//        } else if (messageType instanceof GenericArrayType) {
//            GenericArrayType genericArrayType = (GenericArrayType) messageType;
//            builder.append(messageType(genericArrayType.getGenericComponentType()))
//                    .append("[]");
//        } else {
//            builder.append(messageType.toString());
//        }
//        return builder.toString();
//    }
//
//    private String parameterizedType(ParameterizedType type) {
//        StringBuilder builder = new StringBuilder();
//        builder.append(messageType(type.getOwnerType()))
//                .append('<');
//        for (int i = 0; i < type.getActualTypeArguments().length; i++) {
//            if (i > 0)
//                builder.append(", ");
//            builder.append(messageType(type.getActualTypeArguments()[i]));
//        }
//        builder.append('>');
//        return builder.toString();
//    }
//
//    private String classType(Class type) {
//        if (type.isArray()) {
//            return classType(type.getComponentType()) + "[]";
//        } else {
//            return type.getName();
//        }
//    }
//


    private void registerBean(Type topicType, String queueName, Class<?> contextClass) {
        try {
            TopicKey topicKey = new TopicKey(queueName, topicType);
            if (!injected.contains(topicKey)) {
                synchronized (this) {
                    if(!injected.contains(topicKey)) {
                        String beanName = "topic_" + Common.getUUIDStr();
                        String cName = String.format("TopicBean$$CBC$$%08X", index.incrementAndGet());
                        String className = String.format("%s.%s", contextClass.getPackage().getName(), cName);


                        ParameterizedType pt = (ParameterizedType) topicType;

                        Class<?> clz = getBeanClass(topicType, queueName, className, pt, contextClass);
                        defaultListableBeanFactory.registerSingleton(beanName, clz.getConstructor().newInstance());
                        log.info("Topic Bean registered: {}, {}, {}, {}. ", beanName, queueName, topicType.toString(), clz.getName());
                        injected.add(topicKey);
                    }
                }
            }
        } catch (Throwable th) {
            throw runtimeException(th);
        }

    }

//    private Class<?> getBeanClassByCGLib(Type topicType, String queueName, String className, ParameterizedType pt) {
//        ClassWriter classWriter = new ClassWriter(1);
//        classWriter.visit(V1_6, ACC_PUBLIC, className,new SignatureAttribute.ClassSignature(
//                null,
//                classType(AbstractTopicFactoryBean.class.getName(), topicType),
//                null).encode(), "",null );
//        classWriter.visitEnd();
//        byte[] code = classWriter.toByteArray();
//
//        return null;
//    }


    private Class<?> getBeanClass(Type topicType, String queueName, String className, ParameterizedType pt, Class<?> contextClass) throws CannotCompileException {
//        Class topicClass = (Class) pt.getRawType();

        ClassPool classPool = JavassistHelper.getClassPool(TopicBeanPostProcessor.class);

        CtClass ctClass = classPool.makeClass(className);
        ctClass.setInterfaces(new CtClass[]{
                classPool.getOrNull(((Class) (pt.getRawType())).getName())
        });
        ClassFile classFile = ctClass.getClassFile();
        classFile.setVersionToJava5();

        ConstPool constPool = classFile.getConstPool();
        String sig = new SignatureAttribute.ClassSignature(
                null,
                null,
                new SignatureAttribute.ClassType[]{
                        classType(((Class) (pt.getRawType())).getName(), pt.getActualTypeArguments()[0]),
                }).encode();
        ctClass.setGenericSignature(sig);

        if (queueName != null) {
            classFile.addAttribute(aggregate(constPool, queue(queueName, constPool)));
        }

        // 增加获取队列名方法
        ctClass.addField(CtField.make("private boolean queueLoaded = false;", ctClass));
        ctClass.addField(CtField.make("private String queueName = null;", ctClass));

        ctClass.addMethod(CtMethod.make("private synchronized String getQueueName() {\n" +
                "    if (!queueLoaded) {\n" +
                "        org.coodex.concrete.message.Queue queue =\n" +
                "                getClass().getAnnotation(org.coodex.concrete.message.Queue.class);\n" +
                "        queueName = queue == null ? null : queue.value();\n" +
                "        queueLoaded = true;\n" +
                "    }\n" +
                "    return queueName;\n" +
                "}", ctClass));

        // 增加获取主题类型方法
        ctClass.addField(CtField.make("private java.lang.reflect.Type topicType = null;", ctClass));
        ctClass.addMethod(CtMethod.make("private synchronized java.lang.reflect.Type getTopicType(){\n" +
                "    if(topicType == null){\n" +
                "       topicType = getClass().getGenericInterfaces()[0];\n" +
                "    }\n" +
                "    return topicType;\n" +
                "}", ctClass));

        // methods
        for (Method method : ((Class) pt.getRawType()).getMethods()) {
            boolean voidReturn = method.getReturnType().equals(void.class);
            CtMethod ctMethod = new CtMethod(
                    voidReturn ? CtClass.voidType :
                            classPool.getOrNull(method.getReturnType().getName()),
                    method.getName(),
                    toCtClass(method.getParameterTypes(), classPool),
                    ctClass
            );
            List<SignatureAttribute.Type> parameters = new ArrayList<SignatureAttribute.Type>();

            for (int i = 0; i < method.getGenericParameterTypes().length; i++) {
                parameters.add(JavassistHelper.classType(
                        toReference(method.getGenericParameterTypes()[i], topicType),
                        contextClass));
            }

            ctMethod.setGenericSignature(new SignatureAttribute.MethodSignature(
                    null,
                    parameters.toArray(new SignatureAttribute.Type[0]),
                    voidReturn ? null : JavassistHelper.classType(
                            toReference(method.getGenericReturnType(), topicType),
                            contextClass),
                    null
            ).encode());
            StringBuilder body = new StringBuilder();
            body.append('{');
            if (!voidReturn) {
                body.append("return ");
            }

            body.append("((")
                    .append(((Class) pt.getRawType()).getName())
                    .append(")org.coodex.concrete.message.Topics.get(getTopicType(), getQueueName())).");

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

        return (Class<?>) ctClass.toClass();
    }

//    @Override
//    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
//        if (bean.getClass().getPackage().getName().startsWith("org.coodex.concrete.test")) {
//            log.debug(beanName);
//            for (PropertyValue value : pvs.getPropertyValues()) {
//                log.debug("{}: {}", value.getName(), value.getValue());
//            }
//        }
//
//        return super.postProcessPropertyValues(pvs, pds, bean, beanName);
//    }

    //

}
