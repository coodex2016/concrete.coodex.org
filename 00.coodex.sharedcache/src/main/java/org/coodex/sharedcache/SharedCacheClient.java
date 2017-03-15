package org.coodex.sharedcache;

import java.io.Serializable;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public interface SharedCacheClient {

    <T extends Serializable> T get(String key);

    void put(String key, Serializable value);

    void put(String key, Serializable value, long max_cached_time);

    void remove(String key);



}
