package org.coodex.sharedcache;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class SharedCacheClientManager {


    private static ServiceLoader<SharedCacheClientFactory> factoryProviders;

    private static synchronized void load() {
        if (factoryProviders == null)
            factoryProviders = ServiceLoader.load(SharedCacheClientFactory.class);
    }

    public static SharedCacheClient getClient(String driverName) {
        if (driverName == null) throw new NullPointerException("distributed cache driverName must not be NULL.");

        load();

        Iterator<SharedCacheClientFactory> factories = factoryProviders.iterator();
        while (factories.hasNext()) {
            SharedCacheClientFactory factory = factories.next();
            if (factory.isAccepted(driverName)) {
                return factory.getClientInstance();
            }
        }
        throw new SharedCacheClientFactoryProviderNotFoundException(driverName);
    }


}
