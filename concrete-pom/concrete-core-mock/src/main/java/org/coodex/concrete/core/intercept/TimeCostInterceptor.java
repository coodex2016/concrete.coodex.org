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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.mockers.TimeCost;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.ServiceLoader;
import org.coodex.util.ServiceLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ServerSide
public class TimeCostInterceptor extends AbstractSyncInterceptor {
    private final static Logger log = LoggerFactory.getLogger(TimeCostInterceptor.class);
    private static ServiceLoader<ConcreteInterceptor> loader = new ServiceLoaderImpl<ConcreteInterceptor>() {

        @Override
        protected ConcreteInterceptor conflict(Class<? extends ConcreteInterceptor> providerClass, Map<String, Object> map) {
            return (ConcreteInterceptor) map.values().iterator().next();
        }
    };
    private Boolean accept = null;

    @Override
    protected boolean accept_(DefinitionContext context) {
        if (accept == null) {
            try {
                accept = loader.get(MockV2Interceptor.class) != null;
            } catch (Throwable th) {
                log.warn(th.getLocalizedMessage(), th);
                accept = false;
            }
        }
        return accept;
    }

    @Override
    public Object around(DefinitionContext context, MethodInvocation joinPoint) throws Throwable {
        long now = Clock.currentTimeMillis();
        long cost = 0;
        TimeCost timeCost = context.getAnnotation(TimeCost.class);
        if (timeCost != null) {
            cost = Common.random(Math.min(timeCost.min(), timeCost.max()), Math.max(timeCost.min(), timeCost.max()));
        }
        Object object = super.around(context, joinPoint);
        long used = Clock.currentTimeMillis() - now;
        if (cost > used) {
            Thread.sleep(cost - used);
        }
        return object;
    }

    @Override
    public int getOrder() {
        return InterceptOrders.SYSTEM_AUDIT + 1;
    }
}
