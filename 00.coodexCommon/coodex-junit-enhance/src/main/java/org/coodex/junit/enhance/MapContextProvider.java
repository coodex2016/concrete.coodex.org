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

package org.coodex.junit.enhance;

import org.junit.runner.Description;

import java.util.HashMap;
import java.util.Map;

public class MapContextProvider implements ContextProvider {
    @Override
    public Map<String, Object> createContext(Description description) {
        MapContext mapContext = description.getAnnotation(MapContext.class);
        Map<String, Object> map = new HashMap<>();
        if (mapContext != null) {
            for (Entry entry : mapContext.value()) {
                map.put(entry.key(), entry.value());
            }
        }
        Entry entry = description.getAnnotation(Entry.class);

        if (entry != null)
            map.put(entry.key(), entry.value());
        return map;
    }

    @Override
    public boolean accept(Description param) {
        return param.getAnnotation(MapContext.class) != null || param.getAnnotation(Entry.class) != null;
    }
}
