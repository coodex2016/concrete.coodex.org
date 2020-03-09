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

import org.coodex.util.Common;

import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static Calendar getCal(String timeDesc) throws ParseException {
        Calendar calendar = Common.strToCalendar(timeDesc, "HH:mm");
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }

    public static void setDate(Calendar c, Calendar date) {
        c.set(Calendar.YEAR, date.get(Calendar.YEAR));
        c.set(Calendar.MONTH, date.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
    }

    public static int timeUnitToCalendarConstant(TimeUnit unit) {
        switch (unit) {
            case MILLISECONDS:
                return Calendar.MILLISECOND;
            case SECONDS:
                return Calendar.SECOND;
            case MINUTES:
                return Calendar.MINUTE;
            case HOURS:
                return Calendar.HOUR;
            case DAYS:
                return Calendar.DATE;
            default:
                throw new IllegalArgumentException("unsupported TimeUnit: " + unit);

        }
    }
}
