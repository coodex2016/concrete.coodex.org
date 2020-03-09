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
import org.coodex.config.Config;
import org.coodex.util.SingletonMap;

import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;
import static org.coodex.concrete.core.intercept.InterceptOrders.LIMITING;

/**
 * Created by davidoff shen on 2017-04-10.
 */
//@ServerSide
//@Local
@Deprecated
public class MaximumConcurrencyInterceptor extends AbstractInterceptor {
//    private static final String TAG_MC = "limiting.maximum.concurrency";
    //    private static final Profile_Deprecated MC_PROFILE = Profile_Deprecated.getProfile("limiting.maximum.concurrency.properties");
//    private static final Map<String, ConcurrencyStrategy> STRATEGIES = new HashMap<String, ConcurrencyStrategy>();
//
//    private static ConcurrencyStrategy getStrategy(String strategyName) {
//        synchronized (STRATEGIES) {
//            if (!STRATEGIES.containsKey(strategyName)) {
//                ConcurrencyStrategy strategy = new ConcurrencyStrategy(strategyName);
//                STRATEGIES.put(strategyName, strategy);
//            }
//        }
//        return STRATEGIES.get(strategyName);
//    }
//    private static final SingletonMap<String, ConcurrencyStrategy> STRATEGY_SINGLETON_MAP =
//            new SingletonMap<>(ConcurrencyStrategy::new);

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
//        ConcurrencyStrategy strategy = getConcurrencyStrategy(context);
//
//        if (strategy != null && !strategy.alloc())
//            throw new ConcreteException(ErrorCodes.OVERRUN);
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

//    @Override
//    public Object after(DefinitionContext context, MethodInvocation joinPoint, Object result) {
//        release(context);
//        return super.after(context, joinPoint, result);
//    }
//
//
//
//    @Override
//    public Throwable onError(DefinitionContext context, MethodInvocation joinPoint, Throwable th) {
//        release(context);
//        return super.onError(context, joinPoint, th);
//    }





}
