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

package org.coodex.concrete.message;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

import static org.coodex.concrete.message.CourierBuilder.getMessageType;
import static org.coodex.util.Common.runtimeException;

class TopicBuilder
        implements SingletonMap.Builder<TopicKey, AbstractTopic> {
    private final static Logger log = LoggerFactory.getLogger(TopicBuilder.class);

    private static AcceptableServiceLoader<Class<? extends AbstractTopic>, TopicPrototypeProvider> providers =
            new AcceptableServiceLoader<Class<? extends AbstractTopic>, TopicPrototypeProvider>(
                    new ConcreteServiceLoader<TopicPrototypeProvider>() {
                    }
            );
    private static SingletonMap<TopicKey, AbstractTopic> topics =
            new SingletonMap<TopicKey, AbstractTopic>(
                    new TopicBuilder()
            );
    private AtomicLong index = new AtomicLong(0);

    static AbstractTopic buildTopic(TopicKey key) {
        return topics.getInstance(key);
    }

    private Class<? extends AbstractTopic> getClass(Type topicType) {
        if (topicType instanceof ParameterizedType) {
            return (Class<? extends AbstractTopic>) ((ParameterizedType) topicType).getRawType();
        } else {
            throw new RuntimeException(topicType + " is NOT SUPPORTED.");
        }
    }

    public Annotation queue(ConstPool constPool, String queue) {
        Annotation annotation = new Annotation(Queue.class.getName(), constPool);
        annotation.addMemberValue("value",
                new StringMemberValue(queue == null ? "" : queue, constPool));
        return annotation;
    }

    @Override
    public AbstractTopic build(TopicKey key) {
        try {
            Class<? extends AbstractTopic> topicClass = getClass(key.topicType);
            TopicPrototypeProvider provider = providers.getServiceInstance(topicClass);
            IF.isNull(provider, "No provider for " + topicClass.getName());
            Class<? extends AbstractTopicPrototype> prototype = provider.getPrototype();

            Courier courier = CourierBuilder.buildCourier(key);

            ClassPool classPool = ClassPool.getDefault();
            String className = String.format("%s.Topic$$CBC$$%08X",
                    TopicBuilder.class.getPackage().getName(), index.incrementAndGet());
            CtClass ctClass = classPool.makeClass(className, classPool.getOrNull(
                    prototype.getName()
            ));
            ctClass.setInterfaces(new CtClass[]{classPool.getOrNull(topicClass.getName())});
            ClassFile classFile = ctClass.getClassFile();
            classFile.setVersionToJava5();
            ConstPool constPool = classFile.getConstPool();
            if (key.queue != null) {
                classFile.addAttribute(JavassistHelper.aggregate(constPool, queue(constPool, key.queue)));
            }
            ctClass.setGenericSignature(
                    new SignatureAttribute.ClassSignature(
                            null,
                            JavassistHelper.classType(prototype.getName(), getMessageType(key.topicType)),
                            new SignatureAttribute.ClassType[]{
                                    JavassistHelper.classType(topicClass.getName(), getMessageType(key.topicType))
                            }).encode());

//            log.debug(new SignatureAttribute.ClassSignature(null,
//                    JavassistHelper.classType(prototype.getName(), getMessageType(key.topicType)),
//                    new SignatureAttribute.ClassType[]{
//                            JavassistHelper.classType(topicClass.getName(), getMessageType(key.topicType))
//                    }).encode());

            CtConstructor ctConstructor = new CtConstructor(
                    new CtClass[]{classPool.getOrNull(Courier.class.getName())},
                    ctClass
            );
            ctConstructor.setBody("{super($$);}");
            ctClass.addConstructor(ctConstructor);

            Class<? extends AbstractTopic> newClass = ctClass.toClass();
            Constructor constructor = newClass.getConstructor(Courier.class);
            return (AbstractTopic) constructor.newInstance(courier);
        } catch (Throwable th) {
            throw runtimeException(th);
        }
    }
}
