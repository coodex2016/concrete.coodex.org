package cc.coodex.closure.threadlocals;

import cc.coodex.closure.Closure;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2016-09-04.
 */
public class MapClosureThreadLocal<K, V> extends ClosureThreadLocal<Map<K, V>> {

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
