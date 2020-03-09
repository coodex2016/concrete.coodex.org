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

package org.coodex.mock.ext;

import org.coodex.mock.AbstractTypeMocker;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.GenericTypeHelper;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间的单值模拟
 */
public class DateTimeTypeMocker extends AbstractTypeMocker<DateTime> {

    @Override
    protected Class[] getSupportedClasses() {
        return new Class[]{
                Calendar.class, Date.class, String.class
        };
    }

    @Override
    protected boolean accept(DateTime annotation) {
        return annotation != null;
    }

    @Override
    public Object mock(DateTime mockAnnotation, Type targetType) {
        Class clazz = GenericTypeHelper.typeToClass(targetType);
        try {
            DateFormat format = Common.getSafetyDateFormat(mockAnnotation.format());//new SimpleDateFormat(mockAnnotation.format());

            long min = 0, max = Long.MAX_VALUE;
            if (mockAnnotation.min().length() > 0) {
                min = format.parse(mockAnnotation.min()).getTime();
            }

            if (mockAnnotation.max().length() > 0) {
                max = format.parse(mockAnnotation.max()).getTime();
            }
            long dateTime = (min == max) ? min : (long) (Math.random() * (Math.max(max, min) - Math.min(max, min))) + Math.min(max, min);

            if (Date.class.equals(clazz)) {
                return new Date(dateTime);
            } else if (Calendar.class.equals(clazz)) {
                Calendar calendar = Clock.now();
                calendar.setTimeInMillis(dateTime);
                return calendar;
            } else if (String.class.equals(clazz)) {
                return format.format(new Date(dateTime));
            } else
                return null;
        } catch (Throwable th) {
            throw new RuntimeException(th.getLocalizedMessage(), th);
        }
    }
}
