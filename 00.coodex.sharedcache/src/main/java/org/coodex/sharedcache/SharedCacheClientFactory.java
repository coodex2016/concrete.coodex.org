package org.coodex.sharedcache;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public interface SharedCacheClientFactory {

    Long DEFAULT_MAX_CACHED_SECENDS = 3600l;

    boolean isAccepted(String driverName);

    SharedCacheClient getClientInstance();

}
