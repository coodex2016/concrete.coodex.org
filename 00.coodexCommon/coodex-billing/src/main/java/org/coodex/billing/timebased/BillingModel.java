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

package org.coodex.billing.timebased;

import org.coodex.billing.Bill;
import org.coodex.util.SelectableService;

import java.util.List;

/**
 * 时序计费模型
 */
public interface BillingModel<C extends TimeBasedChargeable> extends SelectableService<String> {

    /**
     * @param chargeable 计费对象
     * @return 创建一个计费模型实例
     */
    Instance<C> create(C chargeable);

    /**
     * 计费模型的实例
     *
     * @param <C> 时序可计费对象
     */
    interface Instance<C extends TimeBasedChargeable> {

        /**
         * @return 切分前算法
         */
        Algorithm<C> getWholeTimeAlgorithm();

        /**
         * @param period     计费时段
         * @param chargeable 计费对象
         * @return 切分后的计费片段
         */
        List<Fragment<C>> slice(Period period, C chargeable);
    }

    interface Algorithm<C extends TimeBasedChargeable> {
        /**
         * @param periodList 计费的周期
         * @return 为空或者为0则表示不适用
         */
        List<Bill.Detail> calc(List<Period> periodList, C chargeable);
    }

    class Fragment<C extends TimeBasedChargeable> {
        private final Algorithm<C> algorithm;
        private final Period period;


        public Fragment(Algorithm<C> algorithm, Period period) {
            this.algorithm = algorithm;
            this.period = period;
        }

        public Algorithm<C> getAlgorithm() {
            return algorithm;
        }

        public Period getPeriod() {
            return period;
        }
    }
}
