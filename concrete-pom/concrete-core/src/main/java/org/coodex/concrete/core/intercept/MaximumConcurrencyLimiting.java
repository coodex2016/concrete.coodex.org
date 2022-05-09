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

import org.coodex.concrete.api.LimitingStrategy;
import org.coodex.concrete.api.limiting.MaximumConcurrency;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.SingletonMap;

import java.util.concurrent.atomic.AtomicInteger;

import static org.coodex.concrete.common.ConcreteHelper.getAppSet;

public class MaximumConcurrencyLimiting implements LimitingStrategy {

    private static final String TAG_MC = "limiting.maximum-concurrency";
    private static final SingletonMap<String, ConcurrencyStrategy> STRATEGY_SINGLETON_MAP =
            SingletonMap.<String, ConcurrencyStrategy>builder().function(ConcurrencyStrategy::new).build();

    @Override
    public boolean apply(DefinitionContext definitionContext) {
        ConcurrencyStrategy strategy = getConcurrencyStrategy(definitionContext);
        return strategy == null || strategy.alloc();
    }

    private ConcurrencyStrategy getConcurrencyStrategy(DefinitionContext context) {
        ConcurrencyStrategy strategy = null;
        MaximumConcurrency maximumConcurrency = context.getAnnotation(MaximumConcurrency.class);
        if (maximumConcurrency != null)
            strategy = STRATEGY_SINGLETON_MAP.get(maximumConcurrency.strategy());
        return strategy;
    }

    @Override
    public void release(DefinitionContext context) {
        ConcurrencyStrategy strategy = getConcurrencyStrategy(context);
        if (strategy != null)
            strategy.release();
    }

    @Override
    public boolean accept(DefinitionContext param) {
        return param.getAnnotation(MaximumConcurrency.class) != null;
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
//            ConcreteHelper.getString()
            return Config.getValue("max",
                    Common.toInt(System.getProperty("limiting.maximum-concurrency.max"), Integer.MAX_VALUE),
                    TAG_MC,
                    getAppSet(),
                    strategyName);
//            return MC_PROFILE.getInt(strategyName + ".max",
//                    MC_PROFILE.getInt("max", Integer.MAX_VALUE));
        }
    }
}
