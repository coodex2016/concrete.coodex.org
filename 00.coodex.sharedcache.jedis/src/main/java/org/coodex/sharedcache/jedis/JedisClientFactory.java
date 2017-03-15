package org.coodex.sharedcache.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.sharedcache.SharedCacheClientFactory;
import org.coodex.util.Profile;
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

    public static final int DEFAULT_PORT = 6379;

    private static AbstractJedisClient client;

    private static Profile profile;

    @Override
    public boolean isAccepted(String driverName) {
        if(driverName == null) return false;
        return JEDIS_DRIVER_NAME.equalsIgnoreCase(driverName.trim());
    }

    private HostAndPort toHostAndPort(String desc) {
        int index = desc.indexOf(':');
        if (index < 0) return new HostAndPort(desc, DEFAULT_PORT);
        String host = desc.substring(0, index).trim();
        int port = Integer.valueOf(desc.substring(index + 1).trim());
        return new HostAndPort(host, port);
    }

    @Override
    public SharedCacheClient getClientInstance() {
        synchronized (JedisClientFactory.class) {
            if (client == null) {
                profile = Profile.getProfile("sharedcache-jedis.properties");
                String[] redisServers = profile.getStrList("redisServers");
                if (redisServers == null) throw new RuntimeException("no redis server defined.");


                Set<HostAndPort> servers = new HashSet<HostAndPort>();
                for (String server : redisServers) {
                    try {
                        servers.add(toHostAndPort(server));
                    } catch (Throwable e) {
                        throw new RuntimeException("unknown redis server: " + server, e);
                    }
                }

                GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

                poolConfig.setMinIdle(profile.getInt("pool.minIdle", GenericObjectPoolConfig.DEFAULT_MIN_IDLE));
                poolConfig.setMaxIdle(profile.getInt("pool.maxIdle", GenericObjectPoolConfig.DEFAULT_MAX_IDLE));
                poolConfig.setMaxTotal(profile.getInt("pool.maxTotal", GenericObjectPoolConfig.DEFAULT_MAX_TOTAL));

                // TODO 安全认证


                if (servers.size() == 0) throw new RuntimeException("no redis server defined.");
                long defaultMaxCacheTime = profile.getLong("defaultMaxCacheTime", DEFAULT_MAX_CACHED_SECENDS) * 1000;

                if (servers.size() == 1) {
                    HostAndPort server = servers.iterator().next();
                    client = new JedisSingleNodeClient(new JedisPool(poolConfig, server.getHost(), server.getPort()), defaultMaxCacheTime);
                } else {
                    client = new JedisClusterClient(new JedisCluster(servers, poolConfig), defaultMaxCacheTime);

                }
            }
        }
        return client;
    }
}
