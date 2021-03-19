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

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.coodex.config.Config;
import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.sharedcache.SharedCacheClientFactory;
import org.coodex.util.Singleton;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class JedisClientFactory implements SharedCacheClientFactory {
    public static final String JEDIS_DRIVER_NAME = "jedis";
    public static final String NAMESPACE_JEDIS = "sharedcache-jedis";

    public static final int DEFAULT_PORT = 6379;

//    private static AbstractJedisClient client;

    //    private static Profile_Deprecated profile;
    private static Singleton<AbstractJedisClient> client = Singleton.with(
            () -> {
//                    profile = Profile_Deprecated.getProfile("sharedcache-jedis.properties");
                String[] redisServers = Config.getArray("redisServers", NAMESPACE_JEDIS);
                if (redisServers == null) throw new RuntimeException("no redis server defined.");


                Set<HostAndPort> servers = new HashSet<>();
                for (String server : redisServers) {
                    try {
                        servers.add(toHostAndPort(server));
                    } catch (Throwable e) {
                        throw new RuntimeException("unknown redis server: " + server, e);
                    }
                }

                GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();

                poolConfig.setMinIdle(Config.getValue("pool.minIdle", GenericObjectPoolConfig.DEFAULT_MIN_IDLE, NAMESPACE_JEDIS));
                poolConfig.setMaxIdle(Config.getValue("pool.maxIdle", GenericObjectPoolConfig.DEFAULT_MAX_IDLE, NAMESPACE_JEDIS));
                poolConfig.setMaxTotal(Config.getValue("pool.maxTotal", GenericObjectPoolConfig.DEFAULT_MAX_TOTAL, NAMESPACE_JEDIS));

                // TODO 安全认证

                if (servers.size() == 0) throw new RuntimeException("no redis server defined.");
                long defaultMaxCacheTime = Config.getValue("defaultMaxCacheTime", DEFAULT_MAX_CACHED_SECONDS, NAMESPACE_JEDIS) * 1000;

                if (servers.size() == 1) {
                    HostAndPort server = servers.iterator().next();
                    return new JedisSingleNodeClient(new JedisPool(poolConfig, server.getHost(), server.getPort()), defaultMaxCacheTime);
                } else {
                    return new JedisClusterClient(new JedisCluster(servers, poolConfig), defaultMaxCacheTime);
                }
            }
    );

    private static HostAndPort toHostAndPort(String desc) {
        int index = desc.indexOf(':');
        if (index < 0) return new HostAndPort(desc, DEFAULT_PORT);
        String host = desc.substring(0, index).trim();
        int port = Integer.parseInt(desc.substring(index + 1).trim());
        return new HostAndPort(host, port);
    }

    @Override
    public boolean isAccepted(String driverName) {
        if (driverName == null) return false;
        return JEDIS_DRIVER_NAME.equalsIgnoreCase(driverName.trim());
    }

    @Override
    public SharedCacheClient getClientInstance() {
        return client.get();
    }

    @Override
    public boolean accept(String param) {
        return isAccepted(param);
    }
}
