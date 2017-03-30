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

import org.coodex.concrete.jaxrs.client.impl.LocalInvokerFactory;
import org.coodex.util.Profile;

import javax.net.ssl.SSLContext;

/**
 * Created by davidoff shen on 2016-12-09.
 */
public class OkHttp3InvokerFactory extends AbstractInvokerFactory<OkHttp3Invoker> {
    private static final Profile PROFILE = Profile.getProfile("okHttp3.properties");

    @Override
    public boolean accept(String domain) {
        return !LocalInvokerFactory.isLocal(domain)
                && getDomainRule(domain) != null;
    }

    private String getDomainRule(String domain) {
        for (Object key : PROFILE.getProperties().keySet()) {
            if (key instanceof String) {
                String sKey = (String) key;
                if (domain.equalsIgnoreCase(PROFILE.getString(sKey))) {
                    return sKey;
                }
            }
        }
        return null;
    }

    @Override
    protected OkHttp3Invoker getHttpInvoker(String domain) {
        return getSSLInvoker(domain, null);
    }

    @Override
    protected OkHttp3Invoker getSSLInvoker(String domain, SSLContext context) {
        return new OkHttp3Invoker(domain, context);
    }

}
