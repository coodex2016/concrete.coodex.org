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

package org.coodex.util.clock;

import org.coodex.config.Config;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DefaultClockAgent implements ClockAgent {
    private final static Logger log = LoggerFactory.getLogger(DefaultClockAgent.class);

    public static final String KEY_BASELINE = "org.coodex.util.Clock.baseline";

    private final Float magnification;
    private final long baseLine;
    private final long start;

    public DefaultClockAgent() {
        magnification = Clock.getMagnification();
        Long l = toBaseLine(Config.get(KEY_BASELINE));
        if (l == null) {
            l = toBaseLine(System.getProperty(KEY_BASELINE));
        }
        start = getSystemStart();
        baseLine = l == null ? getSystemStart() : l.longValue();
    }

    private Long toBaseLine(String str) {
        if (!Common.isBlank(str) && !str.equalsIgnoreCase("now")) {
            try {
                return Common.strToDate(str).getTime();
            } catch (ParseException e) {
                log.warn("baseline parse error: {}", str, e);
            }
        }
        return null;
    }

    private Long getSystemStart() {
        return ManagementFactory.getRuntimeMXBean().getStartTime();
    }

    private long diff() {
        return (long) ((System.currentTimeMillis() - start) * magnification);
    }

    @Override
    public long currentTimeMillis() {
        return baseLine + diff();
    }

    @Override
    public Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis());
        return calendar;
    }

    @Override
    public void sleep(long millis) throws InterruptedException {
        if (millis <= 0) return;
        Thread.sleep((long) Math.max(millis / magnification, 1));
    }

    @Override
    public void objWait(Object obj, long millis) throws InterruptedException {
        if (millis <= 0) return;
        obj.wait((long) Math.max(millis / magnification, 1));
    }

    @Override
    public long toMillis(long duration, TimeUnit timeUnit) {
        return (long) (timeUnit.toMillis(duration )/ magnification);
    }
}
