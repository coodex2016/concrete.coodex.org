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

import org.coodex.billing.Bill;
import org.coodex.billing.timebased.AbstractTimeBasedCalculator;
import org.coodex.billing.timebased.BillingModel;
import org.coodex.billing.timebased.Period;
import org.coodex.billing.timebased.reference.AbstractAlgorithm;
import org.coodex.billing.timebased.reference.AlgorithmFactory;

public class PerHourAlgorithmFactory implements AlgorithmFactory<KTVConsumption, PerHourAlgorithmProfile> {
    @Override
    public BillingModel.Algorithm<KTVConsumption> build(final PerHourAlgorithmProfile perHourAlgorithmProfile) {
        return new AbstractAlgorithm<KTVConsumption>() {

            private int getPricePerHour(KTVConsumption ktvConsumption) {
                switch (ktvConsumption.getRoomType()) {
                    case LARGE:
                        return perHourAlgorithmProfile.getPriceLargeRoom();
                    case MIDDLE:
                        return perHourAlgorithmProfile.getPriceMiddleRoom();
                    default:
                        return perHourAlgorithmProfile.getPriceSmallRoom();
                }
            }

            private long getPrice(KTVConsumption ktvConsumption, long duration) {
                int remainder = (int) (duration % 60);
                int quotient = (int) (duration / 60);
                int pricePerHour = getPricePerHour(ktvConsumption);
                return quotient * pricePerHour + (remainder > 0 ? pricePerHour : 0);
            }

            private String getItemName(KTVConsumption ktvConsumption) {
                switch (ktvConsumption.getRoomType()) {
                    case LARGE:
                        return "大包包间费";
                    case MIDDLE:
                        return "中包包间费";
                    default:
                        return "小包包间费";
                }
            }

            @Override
            protected Bill.Detail calc(Period period, long duration, KTVConsumption chargeable) {
                return new AbstractTimeBasedCalculator.TimeBasedDetailImpl(
                        period,
                        getPrice(chargeable, duration),
                        getItemName(chargeable) + " 消费 " + duration + " 分钟"
                );
            }
        };
    }

    @Override
    public boolean accept(PerHourAlgorithmProfile param) {
        return param != null;
    }
}
