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

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SystemClockAgent implements ClockAgent {

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public Calendar getCalendar() {
        return Calendar.getInstance();
    }

    @Override
    public void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    @Override
    public void objWait(Object obj, long millis) throws InterruptedException {
        obj.wait(millis);
    }

    @Override
    public void sleep(TimeUnit unit, long timeout) throws InterruptedException {
        unit.sleep(timeout);
    }

    @Override
    public long toMillis(long duration, TimeUnit timeUnit) {
        return timeUnit.toMillis(duration);
    }
}
