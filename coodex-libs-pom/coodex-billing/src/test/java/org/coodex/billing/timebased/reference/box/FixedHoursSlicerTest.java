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
import org.coodex.util.Common;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

public class FixedHoursSlicerTest {

    private static void show(List<Period> periods) {
        StringBuilder builder = new StringBuilder();
        for (Period period : periods) {
            builder.append("[").append(Common.calendarToStr(period.getStart()))
                    .append(", ").append(Common.calendarToStr(period.getEnd()))
                    .append("]\n");
        }
        System.out.println(builder.toString());
    }

    public static Period initPeriod() {
        return initPeriod(1);
    }

    public static Period initPeriod(int days) {
        Calendar s = Calendar.getInstance(), e = Calendar.getInstance();
        s.add(Calendar.DATE, -days);
        return Period.BUILDER.create(s, e);
    }

    private void testO(int hours){
        FixedHoursSlicerFactory<TimeBasedChargeable> fixedHoursSlicerFactory = new FixedHoursSlicerFactory<TimeBasedChargeable>() {
        };
        FixedHoursSlicerProfile fixedHoursSlicerProfile = new FixedHoursSlicerProfile(hours);
        List<Period> periods = fixedHoursSlicerFactory.build(fixedHoursSlicerProfile)
                .slice(initPeriod(), null);
        System.out.println("固定小时数分段 - " + hours + "小时");
        show(periods);
    }

    @Test
    public void test1() {
        testO(5);
        testO(8);
        testO(12);
    }

    @Test
    public void test2() {
        FragmentSlicerProfile fragmentProfile = new FragmentSlicerProfile("20:00", "08:00");
        FragmentSlicerFactory<TimeBasedChargeable> factory = new FragmentSlicerFactory<TimeBasedChargeable>() {
        };
        System.out.println("晚八到早八段");
        show(factory.build(fragmentProfile).slice(initPeriod(), null));
    }

    @Test
    public void test3() {
        FragmentSlicerProfile fragmentProfile = new FragmentSlicerProfile("08:00", "20:00");
        FragmentSlicerFactory<TimeBasedChargeable> factory = new FragmentSlicerFactory<TimeBasedChargeable>() {
        };
        System.out.println("早八到晚八段");
        show(factory.build(fragmentProfile).slice(initPeriod(), null));
    }

    private void testX(String start) {
        FixedDateSlicerProfile fixedDateSlicerProfile = new FixedDateSlicerProfile(start);
        FixedDateSlicerFactory<TimeBasedChargeable> factory = new FixedDateSlicerFactory<TimeBasedChargeable>() {
        };
        System.out.println("按自然日分段" + (start == null ? "" : (", 开始时刻 " + start)));
        show(factory.build(fixedDateSlicerProfile).slice(initPeriod(3), null));
    }

    @Test
    public void test4() {
        testX(null);
        testX("16:00");
    }


}
