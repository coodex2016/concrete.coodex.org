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
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.SelectableServiceLoader;
import org.coodex.util.SingletonMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.*;
import static org.coodex.concrete.message.CourierBuilder.getMessageType;
import static org.coodex.util.Common.cast;
import static org.coodex.util.Common.rte;

@SuppressWarnings("rawtypes")
class TopicBuilder implements Function<TopicKey, AbstractTopic> {

    private final static Logger log = LoggerFactory.getLogger(TopicBuilder.class);

    private static final TopicPrototypeProvider defaultTopicPrototypeProvider = new DefaultTopicPrototypeProvider();

    private static final SelectableServiceLoader<Class<?>, TopicPrototypeProvider> topicPrototypeProviderLoader = new LazySelectableServiceLoader<Class<?>, TopicPrototypeProvider>(defaultTopicPrototypeProvider) {
    };
    //            new Singleton<>(
//                    () -> new SelectableServiceLoader<Class<? extends AbstractTopic>, TopicPrototypeProvider>(defaultTopicPrototypeProvider){}
//            );
//    private static AcceptableServiceLoader<Class<? extends AbstractTopic>, TopicPrototypeProvider> providers =
//            new AcceptableServiceLoader<Class<? extends AbstractTopic>, TopicPrototypeProvider>(defaultTopicPrototypeProvider){};

    private static final SingletonMap<TopicKey, AbstractTopic> topics = SingletonMap.<TopicKey, AbstractTopic>builder().function(new TopicBuilder()).build();

    private final AtomicLong index = new AtomicLong(0);

    static AbstractTopic buildTopic(TopicKey key) {
        return topics.get(TopicKey.copy(key));
    }

    private Class<? extends AbstractTopic> getClass(Type topicType) {
        if (topicType instanceof ParameterizedType) {
            return cast(((ParameterizedType) topicType).getRawType());
        } else if (topicType instanceof Class && AbstractTopic.class.isAssignableFrom((Class<?>) topicType)) {
            return cast(topicType);
        } else {
            throw new RuntimeException(topicType + " is NOT SUPPORTED.");
        }
    }

    public Annotation queue(ConstPool constPool, String queue) {
        Annotation annotation = new Annotation(Queue.class.getName(), constPool);
        annotation.addMemberValue("value", new StringMemberValue(queue == null ? "" : queue, constPool));
        return annotation;
    }

    @Override
    public AbstractTopic apply(TopicKey key) {
        try {
            Class<? extends AbstractTopic> topicClass = getClass(key.topicType);
            TopicPrototypeProvider provider = topicPrototypeProviderLoader.select(topicClass);
            if (provider == null) {
                if (defaultTopicPrototypeProvider.accept(topicClass)) {
                    provider = defaultTopicPrototypeProvider;
                }
            }

            Class<?> prototype = Optional.ofNullable(provider).orElseThrow(() -> ConcreteHelper.getException("No provider for " + topicClass.getName())).getPrototype();


            Courier courier = CourierBuilder.buildCourier(key);

            ClassPool classPool = ClassPool.getDefault();
            String className = String.format("%s.Topic$$CBC$$%08X", TopicBuilder.class.getPackage().getName(), index.incrementAndGet());
            CtClass ctClass = classPool.makeClass(className, getCtClass(prototype, classPool)
//                    classPool.getOrNull(
//                    prototype.getName()
//            )
            );
            ctClass.setInterfaces(new CtClass[]{getCtClass(topicClass, classPool)
//                    classPool.getOrNull(topicClass.getName())
            });
            ClassFile classFile = ctClass.getClassFile();
            classFile.setVersionToJava5();
            ConstPool constPool = classFile.getConstPool();
            if (key.queue != null) {
                classFile.addAttribute(aggregate(constPool, queue(constPool, key.queue)));
            }
            ctClass.setGenericSignature(new SignatureAttribute.ClassSignature(null, classType(prototype.getName(), getMessageType(key.topicType)), new SignatureAttribute.ClassType[]{classType(topicClass.getName(), getMessageType(key.topicType))}).encode());

//            log.debug(new SignatureAttribute.ClassSignature(null,
//                    JavassistHelper.classType(prototype.getName(), getMessageType(key.topicType)),
//                    new SignatureAttribute.ClassType[]{
//                            JavassistHelper.classType(topicClass.getName(), getMessageType(key.topicType))
//                    }).encode());

            CtConstructor ctConstructor = new CtConstructor(new CtClass[]{getCtClass(Courier.class, classPool)
//                            classPool.getOrNull(Courier.class.getName())
            }, ctClass);
            ctConstructor.setBody("{super($$);}");
            ctClass.addConstructor(ctConstructor);

            Class<?> newClass = ctClassToClass(ctClass, TopicBuilder.class);
//                    Common.isJava9AndLast() ?
//                    ctClass.toClass(TopicBuilder.class) :
//                    ctClass.toClass();
            Constructor constructor = newClass.getConstructor(Courier.class);

            AbstractTopic abstractTopic = cast(constructor.newInstance(courier));
            log.info("Topic build. {} for {}", abstractTopic.getClass().getName(), key);
            return abstractTopic;
        } catch (Throwable th) {
            throw rte(th);
        }
    }
}
