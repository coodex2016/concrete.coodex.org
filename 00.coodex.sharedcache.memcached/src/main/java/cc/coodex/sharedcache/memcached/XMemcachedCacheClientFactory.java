package cc.coodex.sharedcache.memcached;

import cc.coodex.sharedcache.SharedCacheClient;
import cc.coodex.sharedcache.SharedCacheClientFactory;
import cc.coodex.util.Common;
import cc.coodex.util.Profile;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;

/**
 * Created by davidoff shen on 2016-11-24.
 */
public class XMemcachedCacheClientFactory implements SharedCacheClientFactory {
    public static final String DRIVER_NAME = "xmemcached";

    private MemcachedClientBuilder builder;
    private Profile profile = Profile.getProfile("sharedcache-xmemcached.properties");

    @Override
    public boolean isAccepted(String driverName) {
        if (Common.isBlank(driverName)) return false;
        return DRIVER_NAME.equalsIgnoreCase(driverName.trim());
    }

    @Override
    public SharedCacheClient getClientInstance() {
        init();
        return new XMemcachedCacheClient(builder, profile.getLong("defaultMaxCacheTime", DEFAULT_MAX_CACHED_SECENDS) * 1000);
    }

    private synchronized void init() {
        if (builder == null) {


            String[] servers = profile.getStrList("memcachedServers", " ");
            if (servers == null || servers.length == 0)
                throw new RuntimeException("no memcached server defined.");


            MemcachedClientBuilder memcachedClientBuilder = new XMemcachedClientBuilder(
                    AddrUtil.getAddresses(profile.getString("memcachedServers")));
            memcachedClientBuilder.setCommandFactory(new BinaryCommandFactory());
            memcachedClientBuilder.setConnectionPoolSize(profile.getInt("poolSize", 1));
            for (String server : servers) {
                String username = profile.getString("user." + server);
                if (!Common.isBlank(username)) {
                    String password = profile.getString("pwd." + server);
                    memcachedClientBuilder.addAuthInfo(AddrUtil.getOneAddress(server),
                            AuthInfo.typical(username, password));
                }
            }

            builder = memcachedClientBuilder;
        }
    }
}
