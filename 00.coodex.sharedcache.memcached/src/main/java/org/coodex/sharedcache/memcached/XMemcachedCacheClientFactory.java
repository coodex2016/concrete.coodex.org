/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.coodex.sharedcache.memcached;

import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.auth.AuthInfo;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.coodex.sharedcache.SharedCacheClient;
import org.coodex.sharedcache.SharedCacheClientFactory;
import org.coodex.util.Common;
import org.coodex.util.Profile;

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
