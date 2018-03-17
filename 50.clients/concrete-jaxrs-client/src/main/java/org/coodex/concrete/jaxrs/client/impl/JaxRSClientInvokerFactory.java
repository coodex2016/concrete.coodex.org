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

package org.coodex.concrete.jaxrs.client.impl;

import org.coodex.concrete.jaxrs.client.AbstractInvokerFactory;

import javax.net.ssl.SSLContext;

/**
 * Created by davidoff shen on 2017-04-15.
 */
public class JaxRSClientInvokerFactory extends AbstractInvokerFactory<JaxRSClientInvoker> {
    @Override
    public boolean accept(String domain) {
        return false;
    }

    @Override
    protected JaxRSClientInvoker getHttpInvoker(String domain, String tokenManagerKey) {
        return new JaxRSClientInvoker(domain, null, tokenManagerKey);
    }

    @Override
    protected JaxRSClientInvoker getSSLInvoker(String domain, SSLContext context, String tokenManagerKey) {
        return new JaxRSClientInvoker(domain, context, tokenManagerKey);
    }
}
