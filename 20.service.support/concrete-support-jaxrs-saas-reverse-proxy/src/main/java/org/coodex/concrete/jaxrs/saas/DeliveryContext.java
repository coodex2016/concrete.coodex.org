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

import org.coodex.closure.AbstractClosureContext;
import org.coodex.closure.CallableClosure;
import org.coodex.concrete.common.ConcreteHelper;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Created by davidoff shen on 2017-03-22.
 */
public class DeliveryContext {
    private final static DeliveryContextClosure DELIVERY_CONTEXT_CLOSURE = new DeliveryContextClosure();
    private final AsyncResponse response;
    private final MultivaluedMap<String, String> requestHeaders;

    public DeliveryContext(AsyncResponse response, MultivaluedMap<String, String> requestHeaders) {
        this.response = response;
        this.requestHeaders = requestHeaders;
    }

    public final static Object closureRun(DeliveryContext context, CallableClosure runnable) {
        try {
            return DELIVERY_CONTEXT_CLOSURE.runWith(context, runnable);
        } catch (Throwable throwable) {
            throw ConcreteHelper.getException(throwable);
        }
    }

    public final static DeliveryContext getContext() {
        return DELIVERY_CONTEXT_CLOSURE.getContext();
    }

    public AsyncResponse getResponse() {
        return response;
    }

//    @Deprecated
//    public final static Object closureRun(DeliveryContext context, ConcreteClosure runnable) {
//        return DELIVERY_CONTEXT_CLOSURE.runWith(context, runnable);
//    }

    public MultivaluedMap getRequestHeaders() {
        return requestHeaders;
    }

    private static class DeliveryContextClosure
            extends AbstractClosureContext<DeliveryContext> {

//        @Deprecated
//        Object runWith(DeliveryContext context, ConcreteClosure runnable) {
//            return closureRun(context, runnable);
//        }

        Object runWith(DeliveryContext context, CallableClosure callable) throws Throwable {
            return closureRun(context, callable);
        }

        DeliveryContext getContext() {
            return $getVariant();
        }

    }
}
