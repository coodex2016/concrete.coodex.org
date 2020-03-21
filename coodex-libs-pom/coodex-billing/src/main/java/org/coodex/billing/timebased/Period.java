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

import org.coodex.util.Common;
import org.coodex.util.Section;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 一个时间范围
 */
public class Period extends Section<Calendar> {

    public static final Builder<Calendar, Period> BUILDER = Period::new;

    private Period(Calendar start, Calendar end) {
        super(start, end);
    }

    /**
     * @param periods 时段列表
     * @param unit    时间单位
     * @return 所有时段的指定单位的时长和
     */
    @SuppressWarnings("WeakerAccess")
    public static long durationOf(List<Period> periods, TimeUnit unit) {
        long duration = 0L;
        if (periods != null && periods.size() != 0) {
            for (Period period : periods) {
                duration += period.duration(unit);
            }
        }
        return duration;
    }

    @Override
    protected Calendar cloneObject(Calendar calendar) {
        return (Calendar) calendar.clone();
    }

    /**
     * @param timeUnit 时间单位
     * @return 指定时间单位的时长
     */
    @SuppressWarnings("WeakerAccess")
    public long duration(TimeUnit timeUnit) {
        long duration = getEnd().getTimeInMillis() - getStart().getTimeInMillis();
        return timeUnit.convert(duration, TimeUnit.MILLISECONDS);
    }

    @Override
    public String toString() {
        return "[" +
                Common.calendarToStr(getStart()) +
                " - " +
                Common.calendarToStr(getEnd()) +
                "]";
    }
}
