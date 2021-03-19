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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 按自然日切片
 *
 * @param <C> {@link TimeBasedChargeable}
 */
public class FixedDateSlicerFactory<C extends TimeBasedChargeable> implements SlicerFactory<C, FixedDateSlicerProfile> {
    @Override
    public FragmentSlicer<C> build(FixedDateSlicerProfile fixedDateSlicerProfile) {
        return new FixedDateSlicer<>(fixedDateSlicerProfile);
    }

    @Override
    public boolean accept(FixedDateSlicerProfile param) {
        return param != null && FixedDateSlicerProfile.class.equals(param.getClass());
    }

    public static class FixedDateSlicer<C extends TimeBasedChargeable> implements FragmentSlicer<C> {
        private final FixedDateSlicerProfile profile;

        public FixedDateSlicer(FixedDateSlicerProfile profile) {
            this.profile = profile;
        }

        @Override
        public List<Period> slice(Period period, C chargeable) {
            try {
                Calendar start = Utils.getCal(profile.getStartTime());
                Utils.setDate(start, period.getStart());
                while (start.after(period.getStart())) {
                    start.add(Calendar.DATE, -1);
                }
                List<Period> periods = new ArrayList<>();
                while (start.before(period.getEnd())) {
                    Calendar next = (Calendar) start.clone();
                    next.add(Calendar.DATE, 1);
                    Period p = Period.BUILDER.create(
                            start.before(period.getStart()) ? period.getStart() : start,
                            next.before(period.getEnd()) ? next : period.getEnd()
                    );
                    if (p.duration(TimeUnit.SECONDS) > 0) {
                        periods.add(p);
                    }
                    start = next;
                }

                return periods;
            } catch (ParseException e) {
                throw new RuntimeException("cannot parse " + profile.getStartTime(), e);
            }
        }
    }

}
