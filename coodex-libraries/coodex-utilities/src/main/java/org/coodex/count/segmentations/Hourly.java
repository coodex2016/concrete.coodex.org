/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.count.segmentations;

import org.coodex.count.Segmentation;
import org.coodex.util.Clock;

import java.util.Calendar;

/**
 * Created by davidoff shen on 2017-04-19.
 */
public class Hourly implements Segmentation {
    @Override
    public long next() {
        Calendar c = Clock.now();
        c.add(Calendar.HOUR_OF_DAY, 1);
        return clearCalendar(c);
    }

    static long clearCalendar(Calendar c) {
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
