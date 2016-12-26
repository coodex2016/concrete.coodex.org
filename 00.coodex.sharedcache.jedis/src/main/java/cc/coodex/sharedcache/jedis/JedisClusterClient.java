package cc.coodex.sharedcache.jedis;

import redis.clients.jedis.JedisCluster;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class JedisClusterClient extends AbstractJedisClient {

    private JedisCluster cluster;
    private JedisAdaptor jedisAdaptor;

    public JedisClusterClient(JedisCluster cluster, long default_max_cache_time) {
        super(default_max_cache_time);
        this.cluster = cluster;
        jedisAdaptor = new Adaptor4JedisCluster(cluster);
    }

    @Override
    protected JedisAdaptor getCommand() {
        return jedisAdaptor;
    }

}
