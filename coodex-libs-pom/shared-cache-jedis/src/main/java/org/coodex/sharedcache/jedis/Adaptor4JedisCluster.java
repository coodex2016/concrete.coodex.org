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

package org.coodex.sharedcache.jedis;

import redis.clients.jedis.JedisCluster;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class Adaptor4JedisCluster implements JedisAdaptor {

    private JedisCluster jedis;

    public Adaptor4JedisCluster(JedisCluster jedis) {
        this.jedis = jedis;
    }


    @Override
    public Long del(byte[] key) {
        return jedis.del(key);
    }

    @Override
    public Long pexpire(byte[] key, long milliseconds) {
        return jedis.pexpire(key, milliseconds);
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return jedis.set(key, value);
    }

    @Override
    public byte[] get(byte[] key) {
        return jedis.get(key);
    }

    @Override
    public void close() {

    }
}
