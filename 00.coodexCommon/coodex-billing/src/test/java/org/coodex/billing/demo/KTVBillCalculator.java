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

package org.coodex.billing.demo;

import org.coodex.billing.Revision;
import org.coodex.billing.timebased.AbstractTimeBasedCalculator;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class KTVBillCalculator extends AbstractTimeBasedCalculator<KTVConsumption> {
    @Override
    protected TimeUnit getTimeUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    protected KTVConsumption copyChargeable(KTVConsumption chargeable, List<Revision> revisions) {
        return chargeable;
    }

    @Override
    public boolean accept(KTVConsumption param) {
        return param != null;
    }
}
