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
import org.coodex.concrete.api.LimitingStrategy;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.IF;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.util.AcceptableServiceLoader;

import static org.coodex.concrete.common.ErrorCodes.OVERRUN;
import static org.coodex.concrete.core.intercept.InterceptOrders.LIMITING;

@ServerSide
public class LimitingInterceptor extends AbstractSyncInterceptor {
    private static final AcceptableServiceLoader<DefinitionContext, LimitingStrategy> STRATEGY_ACCEPTABLE_SERVICE_LOADER
            = new AcceptableServiceLoader<DefinitionContext, LimitingStrategy>(new TokenBucketLimiting() {
        @Override
        public boolean accept(DefinitionContext param) {
            return true;
        }
    }) {
    };

    @Override
    protected boolean accept_(DefinitionContext context) {
        return STRATEGY_ACCEPTABLE_SERVICE_LOADER.getServiceInstance(context) != null;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        DefinitionContext context = getContext(invocation);
        LimitingStrategy strategy = STRATEGY_ACCEPTABLE_SERVICE_LOADER.getServiceInstance(context);
        if (strategy != null) {
            IF.not(strategy.apply(context), OVERRUN);
        }
        try {
            return super.invoke(invocation);
        } finally {
            if (strategy != null) {
                strategy.release(context);
            }
        }
    }

//    @Override
//    public void before(DefinitionContext context, MethodInvocation joinPoint) {
//        LimitingStrategy strategy = STRATEGY_ACCEPTABLE_SERVICE_LOADER.getServiceInstance(context);
//        if (strategy != null) {
//            IF.not(strategy.apply(context), OVERRUN);
//        }
//    }

    @Override
    public int getOrder() {
        return LIMITING;
    }
}
