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
import org.coodex.billing.timebased.AbstractTimeBasedCalculator;
import org.coodex.billing.timebased.BillingModel;
import org.coodex.billing.timebased.Period;
import org.coodex.billing.timebased.TimeBasedChargeable;
import org.coodex.billing.timebased.reference.AbstractAlgorithm;
import org.coodex.billing.timebased.reference.AlgorithmFactory;

public class FreeAlgorithmFactory implements AlgorithmFactory<TimeBasedChargeable, FreeAlgorithmProfile> {

    private final static BillingModel.Algorithm<TimeBasedChargeable> ALGORITHM =
            new AbstractAlgorithm<TimeBasedChargeable>() {
                @Override
                protected Bill.Detail calc(Period period, long duration, TimeBasedChargeable chargeable) {
                    return new AbstractTimeBasedCalculator.TimeBasedDetailImpl(
                            period, 0, "free"
                    );
                }
            };

    @Override
    public BillingModel.Algorithm<TimeBasedChargeable> build(FreeAlgorithmProfile freeAlgorithmProfile) {
        return ALGORITHM;
    }

    @Override
    public boolean accept(FreeAlgorithmProfile param) {
        return param != null && FreeAlgorithmProfile.class.equals(param.getClass());
    }
}
