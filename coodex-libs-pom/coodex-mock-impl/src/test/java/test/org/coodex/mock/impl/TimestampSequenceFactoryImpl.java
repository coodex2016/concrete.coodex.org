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

package test.org.coodex.mock.impl;

import org.coodex.mock.SequenceMocker;
import org.coodex.util.Common;

import java.lang.annotation.Annotation;
import java.util.Calendar;
import java.util.Random;

public class TimestampSequenceFactoryImpl implements TimestampSequenceFactory {

    @Override
    public SequenceMocker<String> newSequenceMocker(Annotation... annotations) {
        Interval interval = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(Interval.class)) {
                interval = (Interval) annotation;
                break;
            }
        }
        return new TimestampSequence(interval);
    }

    static class TimestampSequence implements SequenceMocker<String> {
        private final int timeUnit;
        private final int interval;
        private final Calendar timestamp = Calendar.getInstance();

        TimestampSequence(Interval interval) {
            this.timeUnit = interval == null ? Calendar.MINUTE : interval.timeUnit();
            this.interval = interval == null ? 10 : interval.interval();
            // 初始化开始时间
            timestamp.add(Calendar.DATE, -new Random().nextInt(10));
        }

        @Override
        public String next() {
            timestamp.add(timeUnit, interval);// 增加一个时间间隔
            return Common.calendarToStr(timestamp);
        }
    }
}
