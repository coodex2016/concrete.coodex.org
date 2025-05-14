/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package org.coodex.junit.enhance;

import org.coodex.util.AbstractServiceLoaderProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestContextServiceLoaderProvider extends AbstractServiceLoaderProvider {

    private static final Set<Object> registeredService = new HashSet<>();

    public static void register(Object serviceInstance) {
        if (serviceInstance != null)
            registeredService.add(serviceInstance);
    }

    @Override
    protected Map<String, Object> loadByRowType(Class<?> rowType) {
        Map<String, Object> services = new HashMap<>();
        registeredService.stream()
                .filter(o -> rowType.isAssignableFrom(o.getClass()))
                .forEach(o -> services.put(o.toString(), o));
        return services;
    }
}
