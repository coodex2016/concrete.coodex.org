package org.coodex.sharedcache.jedis;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public interface JedisAdaptor{
    Long del(byte[] key);
    Long pexpire(byte[] key, final long milliseconds);
    String set(byte[] key, byte[] value);
    byte[] get(byte[] key);
    void close();

}
