package org.coodex.sharedcache;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class SharedCacheClientFactoryProviderNotFoundException extends RuntimeException {
    public SharedCacheClientFactoryProviderNotFoundException(String message) {
        super("provider for [" + message + "] not found.");
    }
}
