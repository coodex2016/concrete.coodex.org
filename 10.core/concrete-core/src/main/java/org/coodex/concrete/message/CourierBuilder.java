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
import javassist.bytecode.SignatureAttribute;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.util.AcceptableServiceLoader;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

import static org.coodex.concrete.message.GenericTypeHelper.solve;
import static org.coodex.concrete.message.Topics.TAG_QUEUE;


/**
 * 不同的队列、不同主题构建唯一的搬运工类
 * 类型定义：
 *
 * @Queue("queueName") public class ClassNameWithIndex
 * extends Prototype<MessageType>
 * implements Courier<MessageType>{}
 */
class CourierBuilder
        implements SingletonMap.Builder<TopicKey, Courier> {


    private final static Logger log = LoggerFactory.getLogger(CourierBuilder.class);

    private static SingletonMap<TopicKey, Courier> couriers =
            new SingletonMap<TopicKey, Courier>(
                    new CourierBuilder()
            );
    private static AcceptableServiceLoader<String, CourierPrototypeProvider> providers =
            new AcceptableServiceLoader<String, CourierPrototypeProvider>(
                    new ConcreteServiceLoader<CourierPrototypeProvider>() {
                    }
            );
    private AtomicLong index = new AtomicLong(0);

    static <M extends Serializable> Courier<M> buildCourier(TopicKey topicKey) {
        return couriers.getInstance(topicKey);
    }

    private static String getDestination(String queue) {
        if (queue == null) return null;

        // 根据queue获取队列的destination描述
        // 暂时考虑properties配置
        return ConcreteHelper.getString(TAG_QUEUE, queue, "destination");
    }


    static Type getMessageType(Type topicType) {
        return solve(AbstractTopic.class.getTypeParameters()[0], topicType);
    }

    private Class<? extends CourierPrototype> getLocalCourierPrototype(String queue, String destination) {
        if (destination != null && queue != null) {
            log.warn("CourierPrototype not found for queue[{}]: {}", queue, destination);
        }

        return LocalCourierPrototype.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Courier build(TopicKey key) {

        try {
            String destination = getDestination(key.queue);
            CourierPrototypeProvider provider = providers.getServiceInstance(destination);
            Class<? extends CourierPrototype> prototype =
                    provider == null ?
                            getLocalCourierPrototype(key.queue, destination) :
                            provider.getPrototype();
            String className = String.format("%s.Courier$$CBC$$%08X",
                    CourierBuilder.class.getPackage().getName(),
                    index.incrementAndGet());

            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.makeClass(className,
                    classPool.getOrNull(prototype.getName()));
            ClassFile classFile = ctClass.getClassFile();
            classFile.setVersionToJava5();

            ctClass.setGenericSignature(
                    new SignatureAttribute.ClassSignature(null,
                            JavassistHelper.classType(prototype.getName(),
                                    getMessageType(key.topicType)),
                            null).encode());


            CtConstructor ctConstructor = new CtConstructor(
                    new CtClass[]{
                            classPool.getOrNull(String.class.getName()),
                            classPool.getOrNull(String.class.getName()),
                            classPool.getOrNull(Type.class.getName())},
                    ctClass
            );
            ctConstructor.setBody("{super($$);}");
            ctClass.addConstructor(ctConstructor);

            Class<Courier> courierClass = ctClass.toClass();
            Constructor courierConstructor = courierClass.getConstructor(String.class, String.class, Type.class);
            return (Courier) courierConstructor.newInstance(key.queue, destination, key.topicType);
        } catch (Throwable th) {
            throw Common.runtimeException(th);
        }
    }


}
