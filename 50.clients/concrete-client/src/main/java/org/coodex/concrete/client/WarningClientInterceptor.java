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
import org.coodex.util.Common;
import org.coodex.util.GenericTypeHelper;
import org.coodex.util.ServiceLoaderImpl;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static org.coodex.concrete.core.intercept.InterceptOrders.OTHER;

@ClientSide
public class WarningClientInterceptor extends AbstractSyncInterceptor {

    private final static Logger log = LoggerFactory.getLogger(WarningClientInterceptor.class);
    private static Type type = new GenericTypeHelper.GenericType<List<WarningData>>() {
    }.getType();
    private static Singleton<Collection<WarningHandle>> WARNING_HANDLES = new Singleton<>(new Singleton.Builder<Collection<WarningHandle>>() {
        @Override
        public Collection<WarningHandle> build() {
            return new ServiceLoaderImpl<WarningHandle>() {
            }.getAllInstances();
        }
    });

    @Override
    protected boolean accept_(DefinitionContext context) {
        return true;
    }
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
    public int getOrder() {
        return OTHER;
    }

    @Override
    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        ServiceContext serviceContext = ConcreteContext.getServiceContext();
        if (serviceContext instanceof ClientSideContext) {
            ClientSideContext clientSideContext = (ClientSideContext) serviceContext;
            String warnings = SubjoinWrapper.getInstance().get(Subjoin.KEY_WARNINGS);
            if (!Common.isBlank(warnings)) {
                List<Warning> warningList = JSONSerializerFactory.getInstance()
                        .parse(warnings, type);
                if (warningList.size() > 0) {
                    for (Warning warning : warningList) {
                        for (WarningHandle handle : WARNING_HANDLES.getInstance()) {
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
        return result;
    }
}
