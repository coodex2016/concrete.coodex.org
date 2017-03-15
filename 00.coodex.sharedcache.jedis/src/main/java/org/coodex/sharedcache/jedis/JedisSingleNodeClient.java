package org.coodex.sharedcache.jedis;

import redis.clients.jedis.JedisPool;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class JedisSingleNodeClient extends AbstractJedisClient {

    private JedisPool pool;

    public JedisSingleNodeClient(JedisPool pool, long default_max_cache_time) {
        super(default_max_cache_time);
        this.pool = pool;
    }

    @Override
    protected JedisAdaptor getCommand() {
        return new Adaptor4Jedis(pool.getResource());
    }
}
