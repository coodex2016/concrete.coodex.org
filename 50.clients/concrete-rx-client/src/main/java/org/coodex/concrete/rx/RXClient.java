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

package org.coodex.concrete.rx;

import org.coodex.concrete.client.ClientCommon;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.util.AcceptableServiceLoader;

@Deprecated
public class RXClient {


    private static final AcceptableServiceLoader<ClientCommon.Domain, RXClientProvider> providers
            = new AcceptableServiceLoader<ClientCommon.Domain, RXClientProvider>(new ConcreteServiceLoader<RXClientProvider>() {
    });

    public static <T> T getInstance(Class<T> rxClass) {
        return getInstance(rxClass, null);
    }

    public static <T> T getInstance(Class<T> rxClass, String domain){
        return getInstance(rxClass, domain, null);
    }

    @Deprecated
    public static <T> T getInstance(Class<T> rxClass, String domain, String tokenManagerKey) {
        if (rxClass.getAnnotation(ReactiveExtensionFor.class) != null) {
            ClientCommon.Domain d = ClientCommon.getDomain(domain);

            RXClientProvider rxClientProvider = providers.getServiceInstance(d);
            if (rxClientProvider == null) {
                throw new RuntimeException("rx client provider for " + domain + " not found.");
            }

            return rxClientProvider.getInstance(rxClass, d, tokenManagerKey);
        } else {
            throw new RuntimeException(rxClass + " is not a RXSupport service.");
        }
    }
}
