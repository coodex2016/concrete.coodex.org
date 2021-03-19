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

package org.coodex.billing.timebased.reference;

import org.coodex.billing.Bill;
import org.coodex.billing.timebased.BillingModel;
import org.coodex.billing.timebased.Period;
import org.coodex.billing.timebased.TimeBasedChargeable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAlgorithm<C extends TimeBasedChargeable> implements BillingModel.Algorithm<C> {
    @Override
    public List<Bill.Detail> calc(List<Period> periodList, C chargeable) {
        List<Bill.Detail> details = new ArrayList<>();
        if (isDiscontinuityAllowed() && periodList.size() > 1) {
            details.add(
                    calc(
                            Period.BUILDER.create(
                                    periodList.get(0).getStart(),
                                    periodList.get(periodList.size() - 1).getEnd()),
                            Period.durationOf(periodList, getTimeUnit()),
                            chargeable)
            );
        } else {
            for (Period period : periodList) {
                details.add(calc(period, period.duration(getTimeUnit()), chargeable));
            }
        }
        return details;
    }

    /**
     * @return 是否允许不连续
     */
    @SuppressWarnings("WeakerAccess")
    protected boolean isDiscontinuityAllowed() {
        return true;
    }

    /**
     * @return 计算时使用的时间单位，默认为分钟
     */
    @SuppressWarnings("WeakerAccess")
    protected TimeUnit getTimeUnit() {
        return TimeUnit.MINUTES;
    }

    /**
     * @param period     时间范围
     * @param duration   在此时间范围内的累计时长，时长单位见{@link #getTimeUnit()}
     * @param chargeable 计费对象
     * @return 计费明细
     */
    @SuppressWarnings("WeakerAccess")
    protected abstract Bill.Detail calc(Period period, long duration, C chargeable);
}
