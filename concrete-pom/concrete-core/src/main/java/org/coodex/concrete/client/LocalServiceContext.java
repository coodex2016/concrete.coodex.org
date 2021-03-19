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

package org.coodex.concrete.client;

import org.coodex.concrete.common.*;
import org.coodex.concrete.core.token.TokenProxy;

import java.util.Locale;


public class LocalServiceContext extends AbstractServiceContext implements ContainerContext {

    private static final Caller localCaller = new Caller() {
        @Override
        public String getAddress() {
            return "local";
        }

        @Override
        public String getClientProvider() {
            return "local";
        }
    };

    private Token token;

    private Caller caller;

    public LocalServiceContext() {

        super(getSubjoinFromContext(), getLocaleFromContext());
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        token = TokenProxy.proxy(serviceContext == null ? null : serviceContext.getTokenId());
        caller = serviceContext != null && serviceContext instanceof ContainerContext ?
                ((ContainerContext) serviceContext).getCaller() : localCaller;
    }


    private static Subjoin getSubjoinFromContext() {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        return serviceContext == null ? null : serviceContext.getSubjoin();
    }

    private static Locale getLocaleFromContext() {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        return serviceContext == null ? null : serviceContext.getLocale();
    }

    @Override
    public Caller getCaller() {
        return caller;
    }

    @Override
    public Token getToken() {
        return token;
    }

    @Override
    public String getTokenId() {
        return getToken().getTokenId();
    }


//    public LocalServiceContext(Destination destination, RuntimeContext context) {
//        super(destination, context);
//        ServiceContext serviceContext = ConcreteContext.getServiceContext();
//        this.caller = serviceContext.getCaller();
//        this.courier = serviceContext.getCourier();
//        this.logging = serviceContext.getLogging();
//        this.model = serviceContext.getModel();
//        this.subjoin = serviceContext.getSubjoin();
//        this.side = SIDE_LOCAL_INVOKE;
//        this.token = serviceContext.getToken();
//    }
//
//    @Override
//    protected AbstractUnit getUnit(RuntimeContext context) {
//        return AModule.getUnit(context.getDeclaringClass(), context.getDeclaringMethod());
//    }
//
//
//    @Override
//    public Subjoin getSubjoin() {
//        return null;
//    }
//
//    @Override
//    public Locale getLocale() {
//        return null;
//    }
//
//    @Override
//    public String getTokenId() {
//        return null;
//    }
}
