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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class JavaUtilServiceLoaderProvider extends AbstractServiceLoaderProvider {
    private final static Logger log = LoggerFactory.getLogger(JavaUtilServiceLoaderProvider.class);

    private static final boolean SINGLETON_ENABLED = Common.toBool(System.getProperty(ServiceLoader.class.getName() + ".singleton.enable"), true);
    private static final boolean CACHE_ENABLED = Common.toBool(System.getProperty(ServiceLoader.class.getName() + ".cache.enable"), true);
    //    private static final Singleton<Boolean> SINGLETON_ENABLED = new Singleton<>(() ->
//            Config.getValue(ServiceLoader.class.getName() + ".singleton.enable", true)
//    );
//    private static final Singleton<Boolean> CACHE_ENABLED = new Singleton<>(() ->
//            Config.getValue(ServiceLoader.class.getName() + ".cache.enable", true)
//    );
    private static final SingletonMap<Class<?>, Map<String, Object>> cache = SingletonMap.<Class<?>, Map<String, Object>>builder().build();

    @Override
    protected Map<String, Object> loadByRowType(Class<?> rowType) {
        Supplier<Map<String, Object>> supplier = () -> {
            Class<?>[] interfaces = rowType.getInterfaces();
            if (interfaces.length == 0) {
                return loadByInterface(rowType);
            } else {
                Map<String, Object> objectMap = new HashMap<>();
                Set<Class<?>> classes = new HashSet<>();
                BiConsumer<String, Object> biConsumer = (key, value) -> {
                    Class<?> instanceClass = value.getClass();
                    if (!classes.contains(instanceClass) && ReflectHelper.isMatch(instanceClass, rowType)) {
                        classes.add(instanceClass);
                        objectMap.put(key, value);
                    }
                };
                for (Class<?> interfaceClass : interfaces) {
                    if (SINGLETON_ENABLED) {
                        loadByRowType(interfaceClass).forEach(biConsumer);
                    } else {
                        objectMap.putAll(loadByRowType(interfaceClass));
                    }
                }
                if (rowType.isInterface()) {
                    loadByInterface(rowType).forEach(biConsumer);
                }
                return objectMap;
            }
        };
        Map<String, Object> objectMap = CACHE_ENABLED ?
                cache.get(rowType, supplier) :
                supplier.get();
        if (log.isDebugEnabled()) {
            if(objectMap.size() > 0) {
                StringJoiner joiner = new StringJoiner("\n\t");
                objectMap.forEach((k, v) -> joiner.add(k + ": " + v.toString()));
                log.debug("{} JUS instances loaded for: {} instances: \n\t{}", objectMap.size(), rowType, joiner.toString());
            } else {
                log.debug("no JUS instance loaded for {}", rowType);
            }
        }
        return objectMap;
    }

    private Map<String, Object> loadByInterface(Class<?> interfaceClass) {
        Map<String, Object> map = new HashMap<>();
        for (Object service : java.util.ServiceLoader.load(interfaceClass)) {
            map.put(service.getClass().getName(), service);
        }
        return map;
    }
}
