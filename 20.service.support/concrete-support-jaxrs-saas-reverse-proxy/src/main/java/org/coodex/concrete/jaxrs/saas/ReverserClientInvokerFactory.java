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

package org.coodex.concrete.jaxrs.saas;

import org.coodex.concrete.jaxrs.client.AbstractInvokerFactory;

import javax.net.ssl.SSLContext;

/**
 * Created by davidoff shen on 2017-03-22.
 */
public class ReverserClientInvokerFactory extends AbstractInvokerFactory<ReverserClientInvoker> /*implements InvokerFactory */ {
    @Override
    public boolean accept(String domain) {
        return DeliveryContext.getContext() != null;
    }

//    @Override
//    public Invoker getInvoker(String domain) {
//        return new ReverserClientInvoker(domain);
//    }

    @Override
    protected ReverserClientInvoker getHttpInvoker(String domain, String tokenManagerKey) {
        return new ReverserClientInvoker(domain);
    }

    @Override
    protected ReverserClientInvoker getSSLInvoker(String domain, SSLContext context, String tokenManagerKey) {
        return new ReverserClientInvoker(domain, context);
    }
}
