/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.junit.enhance;

import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.LazyServiceLoader;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.coodex.util.Common.getUUIDStr;

public class TestUtils {
    public static final Time TIME = new TimeImpl();
    static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();
    private final static Logger log = LoggerFactory.getLogger(TestUtils.class);
    private static final String KEY_TIMESTAMP = getUUIDStr();
    private static final String KEY_NAME = getUUIDStr();
    private static final LazyServiceLoader<LoggerProvider> LOGGER_PROVIDER_LOADER = new LazyServiceLoader<LoggerProvider>(Slf4jLoggerProvider::new) {
    };
    public static final Logger logger = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class<?>[]{Logger.class},
            (proxy, method, args) -> {
                if (args.length > 0) {
                    return method.invoke(getLogger(), args);
                } else {
                    return method.invoke(getLogger());
                }
            });
    private static final LazySelectableServiceLoader<Description, ContextProvider> CONTEXT_PROVIDER_LOADER =
            new LazySelectableServiceLoader<Description, ContextProvider>(new ContextProvider() {
                @Override
                public Map<String, Object> createContext(Description description) {
                    return new HashMap<>();
                }

                @Override
                public boolean accept(Description param) {
                    return true;
                }
            }) {
            };


    private TestUtils() {

    }

    private static Logger getLogger() {
        String name = testCaseName();
        return Common.isBlank(name) ? log : LOGGER_PROVIDER_LOADER.get().getLogger(name);
    }

    public static String testCaseName() {
        return _get(KEY_NAME);
    }

    static Map<String, Object> contextClone() {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) return null;
        Map<String, Object> result = new HashMap<>(map);
        result.put(KEY_TIMESTAMP, Clock.now());
        return result;
    }

    private static Map<String, Object> buildContext(Description description) {
        Map<String, Object> map = new HashMap<>();
        CONTEXT_PROVIDER_LOADER.getAll().values().forEach(contextProvider -> {
            if (contextProvider.accept(description)) {
                map.putAll(contextProvider.createContext(description));
            }
        });
        Context.Data contextData = Context.Data.from(description.getAnnotation(Context.class));
        map.put(KEY_NAME, Common.isBlank(contextData.name) ? description.getMethodName() : contextData.name);
        try {
            map.put(KEY_TIMESTAMP,
                    Common.isBlank(contextData.timestamp) ?
                            Clock.getCalendar() :
                            Common.strToCalendar(contextData.timestamp, Common.DEFAULT_DATETIME_FORMAT));
        } catch (Throwable th) {
            System.err.println("invalid timestamp: " + contextData.timestamp);
            map.put(KEY_TIMESTAMP, Clock.getCalendar());
        }
        return map;
    }

    static Statement wrap(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (CONTEXT.get() != null) {
                    statement.evaluate();
                } else {
                    CONTEXT.set(buildContext(description));
                    try {
                        statement.evaluate();
                    } finally {
                        CONTEXT.remove();
                    }
                }
            }
        };
    }

    public static void asyncRun(Runnable runnable) {
        Map<String, Object> objectMap = CONTEXT.get();
        if (objectMap == null) {
            new Thread(runnable).start();
        } else {
            Map<String, Object> map = new HashMap<>(objectMap);
            map.put(KEY_TIMESTAMP, timestamp().clone());
            new Thread(() -> {
                CONTEXT.set(map);
                try {
                    runnable.run();
                } finally {
                    CONTEXT.remove();
                }
            }).start();
        }
    }

    static Calendar timestamp() {
        return get(KEY_TIMESTAMP, Clock::getCalendar);
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static <T> T _get(String key) {
        Map<String, Object> objectMap = CONTEXT.get();
        return objectMap == null ? null : (T) objectMap.get(key);
    }

    public static <T> T get(String key, Class<T> tClass) {
        return _get(key);
    }

    public static Object get(String key) {
        return _get(key);
    }

    @SuppressWarnings("SameParameterValue")
    static <T> T get(String key, Supplier<T> supplier) {
        Map<String, Object> objectMap = CONTEXT.get();
        if (objectMap == null) {
            return supplier.get();
        } else {
            @SuppressWarnings("unchecked")
            T o = (T) objectMap.get(key);
            if (o == null) {
                o = supplier.get();
                objectMap.put(key, o);
            }
            return o;
        }
    }


    public interface Time {

        Time hours(int hours);

        Time minutes(int minutes);

        Time seconds(int seconds);

        Time go(int hours, int minutes, int seconds);

        Time go(int year, int months, int days, int hours, int minutes, int seconds);

        Time days(int days);

        Time months(int months);

        Time years(int years);

        Time nextMinute();

        Time nextMinutes(int minutes);

        Time nextHour();

        Time nextHours(int hours);

        Time nextWeek();

        Time nextWeeks(int weeks);

        Time nextDay();

        Time nextDays(int days);

        Time nextMonth();

        Time nextMonths(int months);

        Time nextYear();

        Time nextYears(int years);

        void go(int milliSeconds);

    }


    static class TimeImpl implements Time {

        private static Calendar truncate(int field) {
            Calendar calendar = timestamp();
            switch (field) {
                case Calendar.YEAR:
                    calendar.set(Calendar.MONTH, 0);
                case Calendar.MONTH:
                    calendar.set(Calendar.DATE, 1);
                case Calendar.DATE:
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                case Calendar.HOUR:
                case Calendar.HOUR_OF_DAY:
                    calendar.set(Calendar.MINUTE, 0);
                case Calendar.MINUTE:
                    calendar.set(Calendar.SECOND, 0);
                case Calendar.SECOND:
                    calendar.set(Calendar.MILLISECOND, 0);
                case Calendar.MILLISECOND:
            }
            return calendar;
        }

        @Override
        public Time hours(int hours) {
            return go(hours, 0, 0);
        }

        @Override
        public Time minutes(int minutes) {
            return go(0, minutes, 0);
        }

        @Override
        public Time seconds(int seconds) {
            return go(0, 0, seconds);
        }

        @Override
        public Time go(int hours, int minutes, int seconds) {
            return go(0, 0, 0, hours, minutes, seconds);
        }

        @Override
        public Time go(int years, int months, int days, int hours, int minutes, int seconds) {
            Calendar calendar = timestamp();
            if (years > 0)
                calendar.add(Calendar.YEAR, years);
            if (months > 0)
                calendar.add(Calendar.MONTH, months);
            if (days > 0)
                calendar.add(Calendar.DATE, days);
            if (hours > 0)
                calendar.add(Calendar.HOUR, hours);
            if (minutes > 0)
                calendar.add(Calendar.MINUTE, minutes);
            if (seconds > 0)
                calendar.add(Calendar.SECOND, seconds);
            return this;
        }

        @Override
        public Time days(int days) {
            return go(0, 0, days, 0, 0, 0);
        }

        @Override
        public Time months(int months) {
            return go(0, months, 0, 0, 0, 0);
        }

        @Override
        public Time years(int years) {
            return go(years, 0, 0, 0, 0, 0);
        }

        private Time next(int amount, int field) {
            if (amount <= 0) throw new IllegalArgumentException("amount must greater than 0.");
            Calendar calendar = truncate(field);
            calendar.add(field, amount);
            return this;
        }

        @Override
        public Time nextWeek() {
            return nextWeeks(1);
        }

        @Override
        public Time nextWeeks(int weeks) {
            if (weeks <= 0) throw new IllegalArgumentException("amount must greater than 0.");
            Calendar calendar = truncate(Calendar.DATE);
            int nextFirstDayDiff = (calendar.get(Calendar.DAY_OF_WEEK) + 7 - calendar.getFirstDayOfWeek()) % 7;
            calendar.add(Calendar.DAY_OF_YEAR, nextFirstDayDiff + (weeks - 1) * 7);
            return this;
        }

        @Override
        public Time nextMinute() {
            return nextMinutes(1);
        }

        @Override
        public Time nextMinutes(int minutes) {
            return next(minutes, Calendar.MINUTE);
        }

        @Override
        public Time nextHour() {
            return nextHours(1);
        }

        @Override
        public Time nextHours(int hours) {
            return next(hours, Calendar.HOUR_OF_DAY);
        }

        @Override
        public Time nextDay() {
            return nextDays(1);
        }

        @Override
        public Time nextDays(int days) {
            return next(days, Calendar.DATE);
        }

        @Override
        public Time nextMonth() {
            return nextMonths(1);
        }

        @Override
        public Time nextMonths(int months) {
            return next(months, Calendar.MONTH);
        }

        @Override
        public Time nextYear() {
            return nextYears(1);
        }

        @Override
        public Time nextYears(int years) {
            return next(years, Calendar.YEAR);
        }

        @Override
        public void go(int milliSeconds) {
            Calendar calendar = timestamp();
            calendar.add(Calendar.MILLISECOND, milliSeconds);

        }

        @Override
        public String toString() {
            return Common.calendarToStr(timestamp());
        }
    }


}


