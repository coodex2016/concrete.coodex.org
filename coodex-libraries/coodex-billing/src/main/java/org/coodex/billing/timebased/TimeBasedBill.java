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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimeBasedBill<C extends TimeBasedChargeable> extends Bill<C> {

    private static final Comparator<Detail> DETAIL_COMPARATOR = (o1, o2) -> {
        TimeBasedDetail detail1 = o1 instanceof TimeBasedDetail ? (TimeBasedDetail) o1 : null;
        TimeBasedDetail detail2 = o2 instanceof TimeBasedDetail ? (TimeBasedDetail) o2 : null;
        if (detail1 != null && detail2 != null) {
            return detail1.getPeriod().getStart().compareTo(detail2.getPeriod().getStart());
        }
        if (detail1 != null) {
            return -1;
        }
        if (detail2 != null) {
            return 1;
        }
        return 0;
    };

    public TimeBasedBill(C chargeable) {
        super(chargeable);
    }

    @Override
    public List<Detail> getDetails() {
        List<Detail> details = new ArrayList<>(super.getDetails());
        details.sort(DETAIL_COMPARATOR);
        return details;
    }
}
