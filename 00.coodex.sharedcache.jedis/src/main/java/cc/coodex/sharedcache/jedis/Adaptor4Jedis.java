package cc.coodex.sharedcache.jedis;

import redis.clients.jedis.Jedis;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class Adaptor4Jedis implements JedisAdaptor {

    private Jedis jedis;

    public Adaptor4Jedis(Jedis jedis) {
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
        jedis.close();
    }
}
