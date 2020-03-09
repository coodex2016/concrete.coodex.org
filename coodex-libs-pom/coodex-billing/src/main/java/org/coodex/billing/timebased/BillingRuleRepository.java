/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

import org.coodex.util.SelectableService;

import java.util.Collection;

public interface BillingRuleRepository<T extends TimeBasedChargeable> extends SelectableService<T> {

    /**
     * @param chargeable 根据消费对象获取适用于消费的一组规则列表
     * @return
     */
    Collection<BillingRule> getRulesBy(T chargeable);
}
