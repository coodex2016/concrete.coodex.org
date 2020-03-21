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
import org.coodex.billing.timebased.WholeTimeRevision;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.coodex.billing.timebased.reference.box.Utils.timeUnitToCalendarConstant;

/**
 * 整体时长调减
 */
public class DurationRevision implements WholeTimeRevision {
    private final String name;
    private final TimeUnit unit;
    private final int duration;
    private final boolean fromStart;

    /**
     * @param name     名称
     * @param duration 时长
     */
    public DurationRevision(String name, int duration) {
        this(name, duration, true);
    }

    /**
     * @param name      名称
     * @param duration  时长
     * @param fromStart true:开始点开始调减，false: 结束点开始调减
     */
    public DurationRevision(String name, int duration, boolean fromStart) {
        this(name, TimeUnit.MINUTES, duration, fromStart);
    }

    /**
     * @param name     名称
     * @param unit     时长单位
     * @param duration 时长
     */
    public DurationRevision(String name, TimeUnit unit, int duration) {
        this(name, unit, duration, true);
    }

    /**
     * @param name      名称
     * @param unit      时长单位
     * @param duration  调减时长
     * @param fromStart true:开始点开始调减，false: 结束点开始调减
     */
    public DurationRevision(String name, TimeUnit unit, int duration, boolean fromStart) {
        this.name = name;
        this.unit = unit;
        this.duration = duration;
        this.fromStart = fromStart;
    }

    private static Period buildPeriod(Calendar c, int duration, TimeUnit timeUnit) {
        Calendar x1 = (Calendar) c.clone();
        Calendar x2 = (Calendar) c.clone();
        //noinspection MagicConstant
        x2.add(timeUnitToCalendarConstant(timeUnit), duration);
        return x1.before(x2) ? Period.BUILDER.create(x1, x2) : Period.BUILDER.create(x2, x1);
    }

    @Override
    public List<Period> revised(List<Period> periods) {
        List<Period> result = new ArrayList<>();
        int index = fromStart ? 0 : periods.size() - 1;
        int remainder = duration;
        while (fromStart ? index < periods.size() : index >= 0) {
            Period period = periods.get(index);
            if (period.duration(unit) <= remainder) {
                result.add(period);
                remainder -= period.duration(unit);
            } else {
                result.add(
                        fromStart ?
                                buildPeriod(period.getStart(), remainder, unit) :
                                buildPeriod(period.getEnd(), -remainder, unit)
                );
                remainder = 0;
            }

            if (remainder == 0) {
                break;
            }
            if (fromStart)
                index++;
            else
                index--;
        }
        return result;
    }

    @Override
    public String getName() {
        return name;
    }
}
