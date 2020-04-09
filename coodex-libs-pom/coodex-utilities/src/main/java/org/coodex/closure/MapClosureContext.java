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

package org.coodex.closure;

import org.coodex.util.Common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public class MapClosureContext<K, V> extends StackClosureContext<Map<K, V>> {


    /**
     * @param key key
     * @return 上下文中key的值
     */
    public V get(K key) {
        Map<K, V> map = get();
        return map == null ? null : map.get(key);
    }

    public Object call(K key, V v, Supplier<?> supplier) {
        if (key == null)
            throw new RuntimeException("key MUST NOT null." + (v == null ? "" : v.toString()));

        Map<K, V> map = new HashMap<>();
        Map<K, V> current = get();
        if (current != null) {
            map.putAll(current);
        }
        map.put(key, v);
        return super.call(map, supplier);

    }

    @Deprecated
    public Object call(K key, V v, CallableClosure callableClosure) throws Throwable {
        return call(key, v, callableClosure == null ? null : callableClosure.toSupplier());
//        if (key == null)
//            throw new RuntimeException("key MUST NOT null." + (v == null ? "" : v.toString()));
//
//        Map<K, V> map = new HashMap<K, V>();
//        Map<K, V> current = get();
//        if (current != null) {
//            map.putAll(current);
//        }
//        map.put(key, v);
//        return super.call(map, callableClosure);
    }


//    @Deprecated
//    public Object useRTE(K key, V v, CallableClosure callableClosure) {
//        try {
//            return call(key, v, callableClosure);
//        } catch (Throwable th) {
//            throw Common.runtimeException(th);
//        }
//    }

    @Override
    public Object call(Map<K, V> map, Supplier<?> supplier) {
        Map<K, V> current = get();
        Map<K, V> context = new HashMap<>();
        if (current != null && current.size() > 0) {
            context.putAll(current);
        }
        if (map != null && map.size() > 0) {
            context.putAll(map);
        }
        return super.call(context, supplier);
    }

//    @Override
//    @Deprecated
//    public Object call(Map<K, V> map, CallableClosure callableClosure) throws Throwable {
//        return call(map, callableClosure == null ? null : callableClosure.toSupplier());
//    }

}
