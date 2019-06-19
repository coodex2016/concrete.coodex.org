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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.Limiting;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.core.intercept.annotations.Local;
import org.coodex.concrete.core.intercept.annotations.ServerSide;
import org.coodex.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.core.intercept.InterceptOrders.LIMITING;

/**
 * Created by davidoff shen on 2017-04-10.
 */
@ServerSide
@Local
public class MaximumConcurrencyInterceptor extends AbstractInterceptor {
    private static final String TAG_MC = "limiting.maximum.concurrency";
    //    private static final Profile_Deprecated MC_PROFILE = Profile_Deprecated.getProfile("limiting.maximum.concurrency.properties");
    private static final Map<String, ConcurrencyStrategy> STRATEGIES = new HashMap<String, ConcurrencyStrategy>();

    private static ConcurrencyStrategy getStrategy(String strategyName) {
        synchronized (STRATEGIES) {
            if (!STRATEGIES.containsKey(strategyName)) {
                ConcurrencyStrategy strategy = new ConcurrencyStrategy(strategyName);
                STRATEGIES.put(strategyName, strategy);
            }
        }
        return STRATEGIES.get(strategyName);
    }

    @Override
    public int getOrder() {
        return LIMITING;
    }

    @Override
    protected boolean accept_(DefinitionContext context) {
        return isServiceMethod(context)
                && (context.getAnnotation(Limiting.class) != null);
    }

    @Override
    public void before(DefinitionContext context, MethodInvocation joinPoint) {
        ConcurrencyStrategy strategy = getConcurrencyStrategy(context);

        if (strategy != null && !strategy.alloc())
            throw new ConcreteException(ErrorCodes.OVERRUN);
    }

//    @Override
//    public Object around(RuntimeContext context, MethodInvocation joinPoint) throws Throwable {
//        ConcurrencyStrategy strategy = getConcurrencyStrategy(context);
//
//        if (strategy != null && !strategy.alloc())
//            throw new ConcreteException(ErrorCodes.OVERRUN);
//
//        try {
//            return joinPoint.proceed();
//        } catch (ConcreteException e) {
//            throw e;
//        } catch (Throwable e) {
//            throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, e.getLocalizedMessage(), e);
//        } finally {
//            if (strategy != null)
//                strategy.release();
//        }
//    }

    @Override
    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
        release(context);
        return super.after(context, joinPoint, result);
    }

    private void release(DefinitionContext context) {
        ConcurrencyStrategy strategy = getConcurrencyStrategy(context);
        if (strategy != null)
            strategy.release();
    }

    @Override
    public Throwable onError(DefinitionContext context, MethodInvocation joinPoint, Throwable th) {
        release(context);
        return super.onError(context, joinPoint, th);
    }

    private ConcurrencyStrategy getConcurrencyStrategy(DefinitionContext context) {
        ConcurrencyStrategy strategy = null;
        Limiting limiting = context.getAnnotation(Limiting.class);
//        if (limiting == null)
//            limiting = context.getDeclaringClass().getAnnotation(Limiting.class);

        if (limiting != null)
            strategy = getStrategy(limiting.strategy());
        return strategy;
    }

    static class ConcurrencyStrategy {
        // 计数器
        private final AtomicInteger counter = new AtomicInteger(0);
        private final String strategyName;


        public ConcurrencyStrategy(String strategyName) {
            this.strategyName = strategyName;
        }

        public synchronized boolean alloc() {
            if (counter.get() < getMaximum()) {
                counter.incrementAndGet();
                return true;
            } else {
                return false;
            }
        }

        public synchronized void release() {
            counter.decrementAndGet();
        }

        public long getMaximum() {
            return Config.getValue("max", Integer.MAX_VALUE, TAG_MC, getAppSet(), strategyName);
//            return MC_PROFILE.getInt(strategyName + ".max",
//                    MC_PROFILE.getInt("max", Integer.MAX_VALUE));
        }
    }

}
