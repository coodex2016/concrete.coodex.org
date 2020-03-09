/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.sharedcache;

import org.coodex.util.LazySelectableServiceLoader;

/**
 * Created by davidoff shen on 2016-11-23.
 */
public class SharedCacheClientManager {


    //    private static ServiceLoader<SharedCacheClientFactory> factoryProviders;
    private static LazySelectableServiceLoader<String, SharedCacheClientFactory> factoryProviders =
            new LazySelectableServiceLoader<String, SharedCacheClientFactory>() {
            };
//            new Singleton<ServiceLoader<SharedCacheClientFactory>>(new Singleton.Builder<ServiceLoader<SharedCacheClientFactory>>() {
//                @Override
//                public ServiceLoader<SharedCacheClientFactory> build() {
//                    return ServiceLoader.load(SharedCacheClientFactory.class);
//                }
//            });

    private static void load() {
//        factoryProviders.get();
//        if (factoryProviders == null) {
//            synchronized (SharedCacheClientManager.class) {
//                if (factoryProviders == null)
//                    factoryProviders = ServiceLoader.load(SharedCacheClientFactory.class);
//            }
//        }
    }

    public static SharedCacheClient getClient(String driverName) {
        if (driverName == null) throw new NullPointerException("distributed cache driverName must not be NULL.");

//        load();
        SharedCacheClientFactory sharedCacheClientFactory = factoryProviders.select(driverName);
//        Iterator<SharedCacheClientFactory> factories = factoryProviders.get().iterator();
//        while (factories.hasNext()) {
//            SharedCacheClientFactory factory = factories.next();
//            if (factory.isAccepted(driverName)) {
//                return factory.getClientInstance();
//            }
//        }

        if (sharedCacheClientFactory != null) return sharedCacheClientFactory.getClientInstance();
        throw new SharedCacheClientFactoryProviderNotFoundException(driverName);
    }


}
