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

package org.coodex.pojomocker.sequence;

import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Calendar;

@Deprecated
public abstract class DateTimeSequence<T> extends AbstractConfigurableSequenceGenerator<T> {

    private final static Logger log = LoggerFactory.getLogger(DateTimeSequence.class);

    private int size;
    private int index;
    private Calendar start;
    private int unit;
    private int interval;

    @Override
    public int size() {
        return size;
    }

    @Override
    public void reset() {
        size = getConfig().getValue("size", 20);
        index = 0;
        start = null;
        String startTimeStr = getConfig().get("startTime");
        if (!Common.isBlank(startTimeStr)) {

            String startTimeFormat = getConfig().get("format");
            String parseFormat = Common.isBlank(startTimeFormat) ? Common.DEFAULT_DATETIME_FORMAT : startTimeFormat;
            try {
                start = Common.strToCalendar(startTimeStr, parseFormat);
            } catch (ParseException e) {
                if (Common.isBlank(startTimeFormat)) {
                    log.warn("no [for] for time {}", startTimeStr);
                } else {
                    log.warn("time {} format {} mismatch.", startTimeFormat, startTimeStr);
                }
            }
        }
        if (start == null) {
            start = Clock.now();
        }

        String inteval = getConfig().getValue("interval", "1h");
        parseInterval(inteval);

    }

    private void parseInterval(String inteval) {
        char[] seq = inteval.toLowerCase().toCharArray();
        StringBuilder number = new StringBuilder();
        StringBuilder unitBuilder = new StringBuilder();
        boolean isNumber = true;
        for (char ch : seq) {
            if (isNumber) {
                if (ch == '-' || (ch >= '0' && ch <= '9')) {
                    number.append(ch);
                } else {
                    isNumber = false;
                }
            }

            if (!isNumber) {
                if (ch == ' ' && unitBuilder.length() == 0)
                    continue;
                else {
                    unitBuilder.append(ch);
                }
            }
        }
        this.interval = Integer.parseInt(number.toString());

        String unitStr = unitBuilder.toString();
        if ("y".equals(unitStr) || "year".equals(unitStr)) {
            this.unit = Calendar.YEAR;
        } else if ("month".equals(unitStr)) {
            this.unit = Calendar.MONTH;
        } else if ("d".equals(unitStr) || "day".equals(unitStr) || "days".equals(unitStr)) {
            this.unit = Calendar.DATE;
        } else if ("h".equals(unitStr) || "hour".equals(unitStr) || "hours".equals(unitStr)) {
            this.unit = Calendar.HOUR;
        } else if ("m".equals(unitStr) || "min".equals(unitStr)) {
            this.unit = Calendar.MINUTE;
        } else if ("s".equals(unitStr) || "sec".equals(unitStr)) {
            this.unit = Calendar.SECOND;
        } else if ("ms".equals(unitStr)) {
            this.unit = Calendar.MILLISECOND;
        } else {
            throw new RuntimeException("invalid interval setting: " + inteval);
        }
    }

    protected Calendar getNext() {
        Calendar calendar = (Calendar) start.clone();
        calendar.add(unit, interval * index++);
        return calendar;
    }

    protected abstract T copy(Calendar calendar);

    @Override
    public T next() {
        return copy(getNext());
    }
}
