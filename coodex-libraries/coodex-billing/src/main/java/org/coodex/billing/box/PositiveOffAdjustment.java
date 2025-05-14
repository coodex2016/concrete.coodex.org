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

package org.coodex.billing.box;


import org.coodex.billing.Bill;
import org.coodex.billing.Chargeable;

/**
 * 所有正数明细都有off
 *
 * @param <C>
 */
@SuppressWarnings("unused")
public class PositiveOffAdjustment<C extends Chargeable> extends AbstractOffAdjustment<C> {

    /**
     * @param offRate off
     * @param name    名称
     */
    public PositiveOffAdjustment(float offRate, String name) {
        super(offRate, name);
    }

    @Override
    protected long total(Bill<C> bill) {
        long amount = 0;
        for (Bill.Detail detail : bill.getDetails()) {
            if (detail.getAmount() > 0) {
                amount += detail.getAmount();
            }
        }
        return amount;
    }
}
