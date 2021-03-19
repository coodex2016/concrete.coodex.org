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
import org.coodex.id.IDGenerator;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.*;
import static org.coodex.util.GenericTypeHelper.toReference;


@Named
public class TopicBeanPostProcessor extends AbstractInjectableBeanPostProcessor<TopicBeanPostProcessor.InjectTopicKey> {

    private final static Logger log = LoggerFactory.getLogger(TopicBeanPostProcessor.class);
//    private Set<TopicKey> injected = new HashSet<>();

    private boolean isTopic(Field field) {
        Class<?> c = field.getType();
        return c.isInterface() && AbstractTopic.class.isAssignableFrom(c);
    }


    @Override
    protected boolean accept(Field field) {
        return isTopic(field);
    }

    @Override
    protected Class<?> getInjectClass(InjectTopicKey key, Class<?> beanClass) {
        String cName = String.format("TopicBean$$CBC$$%08X", getIndex());
        String className = String.format("%s.%s", TopicBeanPostProcessor.class.getPackage().getName(), cName);

        ParameterizedType pt = (ParameterizedType) key.getTopicType();

        try {
            Class<?> clz = getBeanClass(key.getTopicType(), key.getThisQueue(), className, pt, beanClass);
            log.info("Topic Bean Class created: {}, {}, {}. ", key.getQueue(), key.getTopicType().toString(), clz.getName());
            return clz;
        } catch (Throwable th) {
            throw Common.rte(th);
        }
    }


    @Override
    protected String newBeanName() {
        return "topic_" + IDGenerator.newId();
    }

    private String getQueueName(Field field) {
        Queue queue = field.getAnnotation(Queue.class);
        return queue == null ? null : queue.value();
    }

    private Type getTopicType(Field field, Class<?> beanClass) {
        return toReference(field.getGenericType(), beanClass);
    }

    @Override
    @Deprecated
    protected boolean injectNoneAnnotation() {
        return true;
    }

    @Override
    protected InjectTopicKey getKey(Class<?> beanClass, Field field) {
        return new InjectTopicKey(getQueueName(field), getTopicType(field, beanClass));
    }

//    @SuppressWarnings("rawtypes")
//    private void scan(Object bean, Class<?> beanClass, String beanName) {
//        for (Field field : ReflectHelper.getAllDeclaredFields(beanClass)) {
//            if (/*isInjectable(field) &&*/ isTopic(field)) {
//                Queue queue = field.getAnnotation(Queue.class);
//                String queueName = queue == null ? null : queue.value();
//                Type topicType = toReference(field.getGenericType(), beanClass);
//                if (isInjectable(field)) {
//                    registerBean(topicType, queueName, beanClass);
//                } else {
//                    AbstractTopic topic = Topics.get(topicType, queueName);
//                    try {
//                        field.setAccessible(true);
//                        field.set(bean, topic);
//                        log.warn("{} {} {} injected. use @Inject plz.", beanName, topicType, field.getName());
//                    } catch (Throwable e) {
//                        throw runtimeException(e);
//                    }
//                }
//            }
//        }
//    }

    private javassist.bytecode.annotation.Annotation queue(String queueName, ConstPool constPool) {
        javassist.bytecode.annotation.Annotation annotation =
                new javassist.bytecode.annotation.Annotation(Queue.class.getName(), constPool);
        annotation.addMemberValue("value", new StringMemberValue(queueName, constPool));
        return annotation;
    }


//    private void registerBean(Type topicType, String queueName, Class<?> contextClass) {
//        try {
//            TopicKey topicKey = new TopicKey(queueName, topicType);
//            if (!injected.contains(topicKey)) {
//                synchronized (this) {
//                    if (!injected.contains(topicKey)) {
//                        String beanName = "topic_" + Common.getUUIDStr();
//                        String cName = String.format("TopicBean$$CBC$$%08X", getIndex());
//                        String className = String.format("%s.%s", contextClass.getPackage().getName(), cName);
//
//
//                        ParameterizedType pt = (ParameterizedType) topicType;
//
//                        Class<?> clz = getBeanClass(topicType, queueName, className, pt, contextClass);
//                        getBeanDefinitionRegistry().registerSingleton(beanName, clz.getConstructor().newInstance());
//                        log.info("Topic Bean registered: {}, {}, {}, {}. ", beanName, queueName, topicType.toString(), clz.getName());
//                        injected.add(topicKey);
//                    }
//                }
//            }
//        } catch (Throwable th) {
//            throw runtimeException(th);
//        }
//
//    }

//    @SuppressWarnings("rawtypes")
    private Class<?> getBeanClass(Type topicType, String queueName, String className,
                                  ParameterizedType pt, Class<?> contextClass) throws CannotCompileException {
//        Class topicClass = (Class) pt.getRawType();

        ClassPool classPool = JavassistHelper.getClassPool(TopicBeanPostProcessor.class);

        CtClass ctClass = classPool.makeClass(className);
        ctClass.setInterfaces(new CtClass[]{
                classPool.getOrNull(((Class<?>) (pt.getRawType())).getName())
        });
        ClassFile classFile = ctClass.getClassFile();
        classFile.setVersionToJava5();

        ConstPool constPool = classFile.getConstPool();
        String sig = new SignatureAttribute.ClassSignature(
                null,
                null,
                new SignatureAttribute.ClassType[]{
                        classType(((Class<?>) (pt.getRawType())).getName(), pt.getActualTypeArguments()[0]),
                }).encode();
        ctClass.setGenericSignature(sig);

        if (queueName != null) {
            classFile.addAttribute(aggregate(constPool, queue(queueName, constPool)));
        } else {
            classFile.addAttribute(aggregate(constPool, primary(constPool)));
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

        buildMethods(classPool, ctClass, (Class<?>) pt.getRawType(), topicType, contextClass,
                "org.coodex.concrete.message.Topics.get(getTopicType(), getQueueName())");

        //        return (Class<?>) ctClass.toClass();
        return IS_JAVA_9_AND_LAST.get() ? ctClass.toClass(TopicBeanPostProcessor.class) : ctClass.toClass();
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

    public static class InjectTopicKey extends TopicKey implements InjectInfoKey {

        private final String thisQueue;

        public InjectTopicKey(String queue, Type topicType) {
            super(queue, topicType);
            this.thisQueue = queue;
        }

        public String getThisQueue() {
            return thisQueue;
        }
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
