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

package org.coodex.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责对服务实例进行类型匹配检查
 */
public abstract class AbstractServiceLoaderProvider implements ServiceLoaderProvider {

    private final SingletonMap<Type, Map<String, Object>> cache = SingletonMap.<Type, Map<String, Object>>builder().build();


    @Override
    public Map<String, Object> load(Type serviceType) {
        return cache.get(serviceType, () -> {
            if (serviceType instanceof Class) {
                return loadByRowType((Class<?>) serviceType);
            } else if (serviceType instanceof ParameterizedType) {
                Map<String, Object> result = new HashMap<>();
                loadByRowType((Class<?>) ((ParameterizedType) serviceType).getRawType()).forEach((key, object) -> {
                    if (object != null && ReflectHelper.isMatch(object.getClass(), serviceType)) {
                        result.put(key, object);
                    }
                });
                return result;
            }
            return new HashMap<>();
        });
    }

    protected abstract Map<String, Object> loadByRowType(Class<?> rowType);
}
