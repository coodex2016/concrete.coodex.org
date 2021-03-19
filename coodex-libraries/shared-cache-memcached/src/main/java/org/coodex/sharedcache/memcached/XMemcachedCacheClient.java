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

package org.coodex.sharedcache.memcached;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import org.coodex.sharedcache.SharedCacheClient;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

/**
 * Created by davidoff shen on 2016-11-24.
 */
public class XMemcachedCacheClient implements SharedCacheClient {

    private MemcachedClientBuilder builder;
    private long max_cached_time;

    public XMemcachedCacheClient(MemcachedClientBuilder builder, long max_cached_time) {
        this.builder = builder;
        this.max_cached_time = max_cached_time;
    }

    protected void assertKey(String key) {
        if (key == null) throw new NullPointerException("cache key is null.");
    }

    protected MemcachedClient getClient() throws IOException, InterruptedException, MemcachedException, TimeoutException {
        MemcachedClient client = builder.build();
        return client;
    }

    @Override
    public <T extends Serializable> T get(String key) {
        assertKey(key);
        try {
            MemcachedClient client = getClient();
            try {
                return client.get(key);
            } finally {
                if (client != null)
                    client.shutdown();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void put(String key, Serializable value) {
        put(key, value, max_cached_time);
    }

    @Override
    public void put(String key, Serializable value, long max_cached_time) {
        assertKey(key);
        int idleTime = (int) (max_cached_time / 1000);
        if (idleTime <= 0) throw new RuntimeException("idleTime must be larger then one second.");
        try {
            MemcachedClient client = getClient();
            try {
                client.set(key, idleTime, value);
            } finally {
                if (client != null)
                    client.shutdown();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void remove(String key) {
        if (key == null) return;
        try {
            MemcachedClient client = getClient();
            try {
                client.deleteWithNoReply(key);
            } finally {
                if (client != null)
                    client.shutdown();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }
}
