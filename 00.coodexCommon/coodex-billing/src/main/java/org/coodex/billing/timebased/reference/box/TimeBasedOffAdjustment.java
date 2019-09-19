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

package org.coodex.billing.timebased.reference.box;

import org.coodex.billing.Bill;
import org.coodex.billing.box.AbstractOffAdjustment;
import org.coodex.billing.timebased.TimeBasedBill;
import org.coodex.billing.timebased.TimeBasedChargeable;
import org.coodex.billing.timebased.TimeBasedDetail;

/**
 * 所有时序对象调整
 *
 * @param <C> {@link TimeBasedChargeable}
 */
public class TimeBasedOffAdjustment<C extends TimeBasedChargeable> extends AbstractOffAdjustment<C> {


    public TimeBasedOffAdjustment(float offRate, String name) {
        super(offRate, name);
    }

    @Override
    protected long total(Bill<C> bill) {
        if (bill instanceof TimeBasedBill) {
            TimeBasedBill<C> timeBasedBill = (TimeBasedBill<C>) bill;
            long amount = 0;
            for (Bill.Detail detail : timeBasedBill.getDetails()) {
                if (detail instanceof TimeBasedDetail) {
                    amount += detail.getAmount();
                }
            }
            return amount;
        } else
            return 0;
    }
}
