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

import org.coodex.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public abstract class AbstractClockAgent implements ClockAgent {
    private final static Logger log = LoggerFactory.getLogger(AbstractClockAgent.class);

    private Float magnification;
    private long baseLine;
    private long start;

    public AbstractClockAgent(Float magnification, long baseLine, long start) {
        this.magnification = magnification;
        this.baseLine = baseLine;
        this.start = start;
        if (log.isDebugEnabled()) {
            log.debug("ClockAgent[{}]: \n\tmagnification: {}\n\tbaseLine: {}\n\tstart at: {}",
                    this.getClass().getName(),
                    this.magnification,
                    Common.calendarToStr(Common.longToCalendar(this.baseLine), "yyyy-MM-dd HH:mm:ss.SSS"),
                    Common.calendarToStr(Common.longToCalendar(this.start), "yyyy-MM-dd HH:mm:ss.SSS"));
        }
    }

    public Float getMagnification() {
        return magnification;
    }

    public long getBaseLine() {
        return baseLine;
    }

    public long getStart() {
        return start;
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
        synchronized (obj) {
            obj.wait((long) Math.max(millis / magnification, 1));
        }
    }

    @Override
    public long toMillis(long duration, TimeUnit timeUnit) {
        return (long) (timeUnit.toMillis(duration) / magnification);
    }

    @Override
    public void sleep(TimeUnit unit, long timeout) throws InterruptedException {
        sleep(unit.toMillis(timeout));
    }
}
