/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.jaxrs.client;

import org.coodex.concrete.client.ClientCommon;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.jaxrs.client.impl.JaxRSClientInvokerFactory;
import org.coodex.concrete.jaxrs.client.impl.LocalInvokerFactory;
import org.coodex.util.ServiceLoader;

/**
 * Created by davidoff shen on 2017-04-15.
 */
public abstract class AbstractClientInstanceFactory implements ClientInstanceFactory {

    private static final String LOCAL_INVOKER = "local";

    private static final LocalInvokerFactory LOCAL_INVOKER_FACTORY = new LocalInvokerFactory();

    public static final boolean isLocal(String domain) {
        return LOCAL_INVOKER.equalsIgnoreCase(domain);
    }

    private static final InvokerFactory DEFAULT_INVOKER_FACTORY = new JaxRSClientInvokerFactory();

    private static final ServiceLoader<InvokerFactory> INVOKER_FACTORY_SPI_FACADE =
            new ConcreteServiceLoader<InvokerFactory>() {
            };

//    private String getServiceRoot(String domain) {
//
//        String s = domain == null ?
//                ConcreteHelper.getProfile().getString("concrete.serviceRoot", "").trim() :
//                ConcreteHelper.getProfile().getString("concrete." + domain + ".serviceRoot", domain);
//        char[] buf = s.toCharArray();
//        int len = buf.length;
//        while (len > 0 && buf[len - 1] == '/') {
//            len--;
//        }
//        return new String(buf, 0, len);
//    }

    protected Invoker getRemoveInvoker(String domain) {
        domain = ClientCommon.getDomain(domain).getIdentify();

        for (InvokerFactory factory : INVOKER_FACTORY_SPI_FACADE.getAllInstances()) {
            if (factory.accept(domain)) {
                return factory.getInvoker(domain);
            }
        }

        return DEFAULT_INVOKER_FACTORY.getInvoker(domain);
    }


    @Override
    public final Invoker getInvoker(String domain) {
        return isLocal(domain) ? LOCAL_INVOKER_FACTORY.getInvoker(domain) : getRemoveInvoker(domain);
    }

//    protected abstract Invoker getRemoveInvoker(String domain);
}
