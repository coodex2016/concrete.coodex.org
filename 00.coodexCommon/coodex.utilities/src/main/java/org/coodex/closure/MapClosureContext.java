/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.closure;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public class MapClosureContext<K, V> extends AbstractClosureContext<Map<K, V>> {

    protected Map<K, V> initValue() {
        return new HashMap<K, V>();
    }

    public V get(K key) {
        Map<K, V> map = $getVariant();
        return map == null ? null : map.get(key);
    }

    public Object runWith(K key, V v, Closure runnable) {
        if (runnable == null) return null;

        Map<K, V> map = $getVariant();
        if (map == null) {
            map = initValue();
            map.put(key, v);
            try {
                return closureRun(map, runnable);
            } finally {
                map.clear();
            }
        } else {
            map.put(key, v);
            return runnable.run();
        }

    }
}
