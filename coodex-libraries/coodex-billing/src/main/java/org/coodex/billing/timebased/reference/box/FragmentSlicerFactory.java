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
import org.coodex.util.Section;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.coodex.billing.timebased.reference.box.Utils.getCal;
import static org.coodex.billing.timebased.reference.box.Utils.setDate;

public class FragmentSlicerFactory<C extends TimeBasedChargeable> implements SlicerFactory<C, FragmentSlicerProfile> {
    @Override
    public FragmentSlicer<C> build(FragmentSlicerProfile fragmentSlicerProfile) {
        return new FragmentSlicerImpl<>(fragmentSlicerProfile);
    }

    @Override
    public boolean accept(FragmentSlicerProfile param) {
        return param != null && FragmentSlicerProfile.class.equals(param.getClass());
    }

    public static class FragmentSlicerImpl<C extends TimeBasedChargeable> implements FragmentSlicer<C> {
        private final FragmentSlicerProfile fragmentProfile;

        public FragmentSlicerImpl(FragmentSlicerProfile fragmentProfile) {
            this.fragmentProfile = fragmentProfile;
        }


        @Override
        public List<Period> slice(Period period, C chargeable) {
            try {
                List<Period> result = new ArrayList<>();
                Calendar fStart = getCal(fragmentProfile.getStartTime());
                Calendar fEnd = getCal(fragmentProfile.getEndTime());
                setDate(fStart, period.getStart());
                setDate(fEnd, period.getStart());
                if (fStart.after(fEnd)) {// 隔夜
                    fStart.add(Calendar.DATE, -1);
                }
                List<Period> wholeTime = Collections.singletonList(period);
                while (fStart.before(period.getEnd())) {
                    List<Period> intersection = Section.intersect(
                            wholeTime,
                            Collections.singletonList(Period.BUILDER.create(fStart, fEnd)),
                            Period.BUILDER);
                    if (intersection.size() > 0) {
                        result.addAll(intersection);
                    }
                    fStart.add(Calendar.DATE, 1);
                    fEnd.add(Calendar.DATE, 1);
                }
                return result;
            } catch (ParseException e) {
                throw new RuntimeException(e.getLocalizedMessage(), e);
            }
        }
    }
}
