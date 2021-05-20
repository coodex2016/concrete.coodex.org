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

package org.coodex.concrete.common.count;

import javassist.*;
import javassist.bytecode.SignatureAttribute;
import org.coodex.concrete.common.bytecode.javassist.JavassistHelper;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.config.Config;
import org.coodex.count.*;
import org.coodex.util.Clock;
import org.coodex.util.ServiceLoader;
import org.coodex.util.LazyServiceLoader;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteHelper.getScheduler;
import static org.coodex.concrete.common.bytecode.javassist.JavassistHelper.IS_JAVA_9_AND_LAST;
import static org.coodex.util.Common.cast;
import static org.coodex.util.GenericTypeHelper.solveFromInstance;

/**
 * Created by davidoff shen on 2017-04-18.
 */
public class CountFacadeProvider implements CountFacade {

//    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE
//            = ExecutorsHelper.newScheduledThreadPool(ConcreteHelper.getProfile().getInt("counter.thread.pool.size", 10));

    // 为啥这么干???
    @SuppressWarnings("unused")
    public static final Singleton<ScheduledExecutorService> SCHEDULED_EXECUTOR_SERVICE = Singleton.with(
            () -> ExecutorsHelper.newScheduledThreadPool(
                    Config.getValue("counter.thread.pool.size", 10, "counter"),
                    "counter"
            )
    );

    private final static Logger log = LoggerFactory.getLogger(CountFacadeProvider.class);
    private final static AtomicInteger atomicInteger = new AtomicInteger(0);
    private final static ServiceLoader<Counter<Countable>> counterProvider = new LazyServiceLoader<Counter<Countable>>() {
    };

    private final Singleton<Map<Class<?>, CounterChain<Countable>>> chainMapSingleton
            = Singleton.with(
            () -> {
                Map<Class<?>, CounterChain<Countable>> chainMap = new HashMap<>();
                TypeVariable<?> t = Counter.class.getTypeParameters()[0];

                for (Counter<Countable> counter : counterProvider.getAll().values()) {
                    Type type = solveFromInstance(t, counter);
                    if (type instanceof Class) {
                        try {
                            Class<?> c = (Class<?>) type;
                            if (chainMap.containsKey(c)) {
                                chainMap.get(c).addCounter(counter);
                            } else {
                                CounterChain<Countable> counterChain = newCounterChain(c);
                                counterChain.addCounter(counter);
                                chainMap.put(c, counterChain);
                            }
                            if (counter instanceof SegmentedCounter)
                                schedule((SegmentedCounter<Countable>) counter);
                        } catch (Throwable th) {
                            log.warn("error occurred: {}", th.getLocalizedMessage(), th);
                        }
                    } else {
                        log.warn("type nonsupport: {}", type);
                    }
                }
                return chainMap;
            }
    );

    private void schedule(final SegmentedCounter<Countable> counter) {
        final Segmentation segmentation = counter.getSegmentation();
        if (segmentation != null) {
            long next = segmentation.next();
            if (next > Clock.currentTimeMillis()) {
                getScheduler("count").schedule(() -> {
                    try {
                        synchronized (counter) {
                            counter.slice();
                        }
                    } finally {
                        schedule(counter);
                    }
                }, next - Clock.currentTimeMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private CounterChain<Countable> newCounterChain(Class<?> clz) throws CannotCompileException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ClassPool classPool = ClassPool.getDefault();
//        String className = String.format("CounterChain$%s$%08X", clz.getSimpleName(), atomicInteger.incrementAndGet());
        String className = String.format("%s.CounterChain$%s$%08X", CounterChain.class.getPackage().getName(), clz.getSimpleName(), atomicInteger.incrementAndGet());
        CtClass newClass = classPool.makeClass(className,
//                classPool.getOrNull(CounterChain.class.getName())
                JavassistHelper.getCtClass(CounterChain.class, classPool)
        );

        newClass.getClassFile().setVersionToJava5();
        newClass.setGenericSignature(new SignatureAttribute.ClassSignature(null,
                new SignatureAttribute.ClassType(clz.getName()),
                null).encode());

        CtConstructor spiConstructor = new CtConstructor(null, newClass);
        spiConstructor.setBody("{super();}");

        CtMethod ctMethod = CtMethod.make(
                String.format("protected %s getThreadPool(){ return %s.SCHEDULED_EXECUTOR_SERVICE.get();}",
                        Executor.class.getName(),
                        CountFacadeProvider.class.getName()),
                newClass);
        newClass.addMethod(ctMethod);

        newClass.addConstructor(spiConstructor);

        CounterChain<Countable> counterChain = cast(
                (IS_JAVA_9_AND_LAST.get() ?
                        newClass.toClass(CounterChain.class) :
                        newClass.toClass())
                        .getConstructor(new Class<?>[0])
                        .newInstance()
        );
        log.info("CounterChain created: {}, {}", counterChain.getClass().getName(), clz.getName());
        return counterChain;
    }

    @Override
    public <T extends Countable> void count(final T value) {
        if (value != null) {
            for (Class<?> clz : chainMapSingleton.get().keySet()) {
                if (clz.isAssignableFrom(value.getClass())) {
                    chainMapSingleton.get().get(clz).count(value);
                }
            }
        }
    }
}
