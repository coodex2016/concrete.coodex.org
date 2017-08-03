package test.org.coodex.destributedcache.jedis;

import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.sharedcache.SharedCacheClientManager;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class JedisTest {
    public static void main(String [] args){
        SharedCacheClient client = SharedCacheClientManager.getClient("jedis");
        client.put("test1", "test1");
        client.put("test2", "test2", 1);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(client.get("test1"));
        System.out.println(client.get("test2"));
    }
}
