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

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConcreteServiceLoader;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.jaxrs.client.impl.X509CertsSSLContextFactory;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-03-27.
 */
public abstract class AbstractInvokerFactory<T extends Invoker> implements InvokerFactory {

    private final static Logger log = LoggerFactory.getLogger(AbstractInvokerFactory.class);

    private final static SSLContextFactory DEFAULT_CONTEXT_FACTORY = new X509CertsSSLContextFactory();

    private final static ServiceLoader<SSLContextFactory> SSL_CONTEXT_FACTORY
            = new ConcreteServiceLoader<SSLContextFactory>() {
        @Override
        public SSLContextFactory getConcreteDefaultProvider() {
            return DEFAULT_CONTEXT_FACTORY;
        }
    };

    private final Map<String, T> cache = new HashMap<String, T>();

    @Override
    public Invoker getInvoker(String domain) {
        synchronized (cache) {
            if (!cache.containsKey(domain)) {
                try {
                    URL url = new URL(domain.toLowerCase());
                    if ("https".equalsIgnoreCase(url.getProtocol())) {
                        cache.put(domain, getSSLInvoker(domain, SSL_CONTEXT_FACTORY.getInstance().getSSLContext(domain)));
                    } else {
                        cache.put(domain, getHttpInvoker(domain));
                    }
                } catch (Throwable throwable) {
                    throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, throwable.getLocalizedMessage(), throwable);
                }

            }
        }
        return cache.get(domain);
    }

    protected abstract T getHttpInvoker(String domain);

    protected abstract T getSSLInvoker(String domain, SSLContext context);

}
