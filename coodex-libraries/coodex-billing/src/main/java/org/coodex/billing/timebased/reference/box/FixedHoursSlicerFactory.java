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

import org.coodex.billing.timebased.Period;
import org.coodex.billing.timebased.TimeBasedChargeable;
import org.coodex.billing.timebased.reference.FragmentSlicer;
import org.coodex.billing.timebased.reference.SlicerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FixedHoursSlicerFactory<C extends TimeBasedChargeable> implements SlicerFactory<C, FixedHoursSlicerProfile> {
    @Override
    public FragmentSlicer<C> build(FixedHoursSlicerProfile fixedHoursSlicerProfile) {
        return new FixedHoursSlicer<>(fixedHoursSlicerProfile.getFixedHours());
    }

    @Override
    public boolean accept(FixedHoursSlicerProfile param) {
        return param != null && FixedHoursSlicerProfile.class.equals(param.getClass());
    }

    public static class FixedHoursSlicer<C extends TimeBasedChargeable> implements FragmentSlicer<C> {
        private final int fixedHours;

        FixedHoursSlicer(int fixedHours) {
            this.fixedHours = fixedHours;
        }

        @Override
        public List<Period> slice(Period period, C chargeable) {
            Calendar start = (Calendar) period.getStart().clone();
            List<Period> result = new ArrayList<>();
            while (start.before(period.getEnd())) {
                Calendar next = (Calendar) start.clone();
                next.add(Calendar.HOUR, fixedHours);
                Period p = Period.BUILDER.create(
                        start,
                        next.before(period.getEnd()) ? next : period.getEnd());
                if (p.duration(TimeUnit.SECONDS) > 0) {
                    result.add(p);
                }
                start = next;
            }
            return result;
        }
    }

}
