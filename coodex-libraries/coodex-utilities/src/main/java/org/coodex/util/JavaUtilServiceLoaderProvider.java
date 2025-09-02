/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import org.coodex.annotatoin.Remark;
import org.coodex.functional.BiConsumer;
import org.coodex.functional.Function;
import org.coodex.functional.Supplier;
import org.coodex.util.java8.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
//import java.util.function.BiConsumer;
//import java.util.function.Supplier;

public class JavaUtilServiceLoaderProvider extends AbstractServiceLoaderProvider {
    private final static Logger log = LoggerFactory.getLogger(JavaUtilServiceLoaderProvider.class);
    private static ClassLoader defaultClassLoader;

    @Remark("对外暴露设置默认的classLoader，主要为了适配Android低版本或者不完善的驱动造成无法使用SPI时无法找到服务实例的情况")
    public static void register(ClassLoader defaultClassLoader) {
        JavaUtilServiceLoaderProvider.defaultClassLoader = defaultClassLoader;
    }

    private static final boolean SINGLETON_ENABLED = true;//Common.toBool(System.getProperty(ServiceLoader.class.getName() + ".singleton.enable"), true);
    private static final boolean CACHE_ENABLED = true;//Common.toBool(System.getProperty(ServiceLoader.class.getName() + ".cache.enable"), true);
    //    private static final Singleton<Boolean> SINGLETON_ENABLED = new Singleton<>(() ->
//            Config.getValue(ServiceLoader.class.getName() + ".singleton.enable", true)
//    );
//    private static final Singleton<Boolean> CACHE_ENABLED = new Singleton<>(() ->
//            Config.getValue(ServiceLoader.class.getName() + ".cache.enable", true)
//    );
    private static final SingletonMap<Class<?>, Map<String, Object>> cache = SingletonMap
            .<Class<?>, Map<String, Object>>builder()
            .function(new Function<Class<?>, Map<String, Object>>() {
                @Override
                public Map<String, Object> apply(Class<?> aClass) {
                    return Collections.emptyMap();
                }
            })
            .build();

    @Override
    protected Map<String, Object> loadByRowType(final Class<?> rowType) {
        Supplier<Map<String, Object>> supplier = new Supplier<Map<String, Object>>() {
            @Override
            public Map<String, Object> get() {
                Class<?>[] interfaces = rowType.getInterfaces();
                if (interfaces.length == 0) {
                    return loadByInterface(rowType);
                } else {
                    final Map<String, Object> objectMap = new HashMap<>();
                    final Set<Class<?>> classes = new HashSet<>();
                    BiConsumer<String, Object> biConsumer = new BiConsumer<String, Object>() {
                        @Override
                        public void accept(String key, Object value) {
                            Class<?> instanceClass = value.getClass();
                            if (!classes.contains(instanceClass) && ReflectHelper.isMatch(instanceClass, rowType)) {
                                classes.add(instanceClass);
                                objectMap.put(key, value);
                            }
                        }
                    };

                    for (Class<?> interfaceClass : interfaces) {
                        if (SINGLETON_ENABLED) {
                            for (Map.Entry<String, Object> entry : loadByRowType(interfaceClass).entrySet()) {
                                biConsumer.accept(entry.getKey(), entry.getValue());
                            }
                        } else {
                            objectMap.putAll(loadByRowType(interfaceClass));
                        }
                    }
                    if (rowType.isInterface()) {
                        for (Map.Entry<String, Object> entry : loadByInterface(rowType).entrySet()) {
                            biConsumer.accept(entry.getKey(), entry.getValue());
                        }
//                        loadByInterface(rowType).forEach(biConsumer);
                    }
                    return objectMap;
                }
            }
        };

        Map<String, Object> objectMap = CACHE_ENABLED ?
                cache.get(rowType, supplier) :
                supplier.get();
        if (Common.isDebug() && log.isDebugEnabled()) {
            if (!objectMap.isEmpty()) {
                org.coodex.util.java8.StringJoiner joiner = new StringJoiner("\n\t");
                for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
                    joiner.add(entry.getKey() + ": " + entry.getValue().toString());
                }
//                objectMap.forEach((k, v) -> joiner.add(k + ": " + v.toString()));
                log.debug("{} JUS instances loaded for: {} instances: \n\t{}", objectMap.size(), rowType, joiner.toString());
            } else {
                log.debug("no JUS instance loaded for {}", rowType);
            }
        }
        return objectMap;
    }

    public static ClassLoader defaultLoader(Class<?> service) {
        ClassLoader cl = defaultClassLoader;
        if (cl == null) cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = service.getClassLoader();
        if (cl == null) cl = JavaUtilServiceLoaderProvider.class.getClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader(); // 兜底
        return cl;
    }

    private Map<String, Object> loadByInterface(Class<?> interfaceClass) {
        Map<String, Object> map = new HashMap<>();
        for (Object service : java.util.ServiceLoader.load(interfaceClass, defaultLoader(interfaceClass))) {
            map.put(service.getClass().getName(), service);
        }
        log.info("[SPI-java.util]load {} Service instances: {}, {}", map.size(), interfaceClass, defaultLoader(interfaceClass));
        return map;
    }
}
