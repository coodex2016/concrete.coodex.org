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
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.count.*;
import org.coodex.util.Clock;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteHelper.getScheduler;
import static org.coodex.util.TypeHelper.solve;

/**
 * Created by davidoff shen on 2017-04-18.
 */
public class CountFacadeProvider implements CountFacade {

//    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE
//            = ExecutorsHelper.newScheduledThreadPool(ConcreteHelper.getProfile().getInt("counter.thread.pool.size", 10));


    private final static Logger log = LoggerFactory.getLogger(CountFacadeProvider.class);
    private final static AtomicInteger atomicInteger = new AtomicInteger(0);
    private final static ServiceLoader<Counter> counterProvider = new ConcreteServiceLoader<Counter>() {
    };
    private Map<Class, CounterChain> chainMap;

    @SuppressWarnings("unchecked")
    private void buildMap() {
        TypeVariable t = Counter.class.getTypeParameters()[0];
        chainMap = new HashMap<Class, CounterChain>();

        for (Counter counter : counterProvider.getAllInstances()) {

            Type type = solve(t, counter.getClass());
            if (type instanceof Class) {
                try {
                    getCounterChain((Class) type).addCounter(counter);
                    if (counter instanceof SegmentedCounter)
                        schedule((SegmentedCounter) counter);
                } catch (Throwable th) {
                    log.warn("error occurred: {}", th.getLocalizedMessage(), th);
                }

            } else {
                log.warn("type nonsupport: {}", type);
            }
        }
    }

    private void schedule(final SegmentedCounter counter) {
        final Segmentation segmentation = counter.getSegmentation();
        if (segmentation != null) {
            long next = segmentation.next();
            if (next > Clock.currentTimeMillis()) {
                getScheduler("count").schedule(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            synchronized (counter) {
                                counter.slice();
                            }
                        } finally {
                            schedule(counter);
                        }
                    }
                }, next - Clock.currentTimeMillis(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private CounterChain getCounterChain(Class clz) throws IllegalAccessException, CannotCompileException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        synchronized (chainMap) {
            if (!chainMap.keySet().contains(clz)) {
                chainMap.put(clz, newCounterChain(clz));
            }
        }
        return chainMap.get(clz);
    }

    private CounterChain newCounterChain(Class clz) throws CannotCompileException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ClassPool classPool = ClassPool.getDefault();
        String className = String.format("CounterChain$%s$%08X", clz.getSimpleName(), atomicInteger.incrementAndGet());
        CtClass newClass = classPool.makeClass(className,
                classPool.getOrNull(CounterChain.class.getName()));

        newClass.getClassFile().setVersionToJava5();
        newClass.setGenericSignature(new SignatureAttribute.ClassSignature(null,
                new SignatureAttribute.ClassType(clz.getName()),
                null).encode());

        CtConstructor spiConstructor = new CtConstructor(null, newClass);
        spiConstructor.setBody("{super();}");

        CtMethod ctMethod = CtMethod.make(
                String.format("protected %s getThreadPool(){ return %s.SCHEDULED_EXECUTOR_SERVICE;}",
                        Executor.class.getName(),
                        CountFacadeProvider.class.getName()),
                newClass);
        newClass.addMethod(ctMethod);

        newClass.addConstructor(spiConstructor);

        return (CounterChain) newClass.toClass().getConstructor(new Class[0]).newInstance();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Countable> void count(final T value) {
        if (chainMap == null)
            synchronized (CountFacadeProvider.class) {
                if (chainMap == null)
                    buildMap();
            }
        if (value != null) {
            for (Class clz : chainMap.keySet()) {
                if (clz.isAssignableFrom(value.getClass())) {
                    chainMap.get(clz).count(value);
                }
            }
        }
    }
}
