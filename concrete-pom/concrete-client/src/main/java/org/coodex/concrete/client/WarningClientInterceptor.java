/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.*;
import org.coodex.concrete.core.intercept.AbstractSyncInterceptor;
import org.coodex.concrete.core.intercept.annotations.ClientSide;
import org.coodex.util.JSONSerializer;
import org.coodex.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static org.coodex.concrete.core.intercept.InterceptOrders.OTHER;

@ClientSide
public class WarningClientInterceptor extends AbstractSyncInterceptor {

    private final static Logger log = LoggerFactory.getLogger(WarningClientInterceptor.class);
    private static final ServiceLoader<WarningHandle> WARNING_HANDLES
            = new LazyServiceLoader<WarningHandle>() {
    };
    //    private static Singleton<Collection<WarningHandle>> WARNING_HANDLES = new Singleton<>(new Singleton
    //    .Builder<Collection<WarningHandle>>() {
//        @Override
//        public Collection<WarningHandle> build() {
//            return new ServiceLoaderImpl<WarningHandle>() {
//            }.getAllInstances();
//        }
//    });
//    private static Singleton<Collection<WarningHandle>> WARNING_HANDLES
//            = Singleton.with(() -> new ServiceLoaderImpl<WarningHandle>() {
//    }.getAll().values());
    private static Type type = new GenericTypeHelper.GenericType<List<WarningData>>() {
    }.getType();
    private boolean noneHandlersWarning = false;
//    private static Singleton<ConcreteServiceLoader<WarningHandle>> WARNING_HANDLES = new Singleton<>(
//            new Singleton.Builder<ConcreteServiceLoader<WarningHandle>>() {
//                @Override
//                public ConcreteServiceLoader<WarningHandle> build() {
//                    return new ConcreteServiceLoader<WarningHandle>() {
//                    };
//                }
//            }
//    );

    @Override
    protected boolean accept_(DefinitionContext context) {
        return true;
    }

    @Override
    public int getOrder() {
        return OTHER;
    }

    private void warningOnce() {
        if (!noneHandlersWarning) {
            synchronized (this) {
                if (!noneHandlersWarning) {
                    log.warn("no warning handler found, but warning occurred.");
                    noneHandlersWarning = true;
                }
            }
        }
    }

    @Override
    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        if (serviceContext instanceof ClientSideContext) {
            ClientSideContext clientSideContext = (ClientSideContext) serviceContext;
            String warnings = SubjoinWrapper.getInstance().get(Subjoin.KEY_WARNINGS);
            if (!Common.isBlank(warnings)) {
                List<Warning> warningList = JSONSerializer.getInstance()
                        .parse(warnings, type);
                if (warningList.size() > 0) {
                    Collection<WarningHandle> handlers = WARNING_HANDLES.getAll().values();
                    for (Warning warning : warningList) {
                        if (handlers.isEmpty()) {
                            warningOnce();
                        } else {
                            for (WarningHandle handle : handlers) {
                                try {
                                    handle.onWarning(clientSideContext.getDestination(), warning);
                                } catch (Throwable th) {
                                    log.warn(th.getLocalizedMessage(), th);
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
