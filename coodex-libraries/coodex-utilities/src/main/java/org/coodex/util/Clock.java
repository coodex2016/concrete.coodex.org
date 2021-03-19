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

package org.coodex.util;

import org.coodex.config.Config;
import org.coodex.util.clock.ClockAgent;
import org.coodex.util.clock.DefaultClockAgent;
import org.coodex.util.clock.SystemClockAgent;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Clock为系统提供抽象的时钟获取接口，可根据系统参数设置时间速率。<br/>
 * 系统配置项：
 * <pre>
 *     org.coodex.util.Clock.baseline
 *     org.coodex.util.Clock.magnification
 * </pre>
 */
@SuppressWarnings("unused")
public final class Clock {

    public static final String KEY_MAGNIFICATION = Clock.class.getName() + ".magnification";
    private static final Singleton<ClockAgent> agentSingleton = Singleton.with(() -> {
        if (getMagnification() == 1.0f) {
            return new SystemClockAgent();
        } else {
            return new LazyServiceLoader<ClockAgent>() {
                @Override
                protected ClockAgent getDefaultInstance() {
                    return new DefaultClockAgent();
                }
            }.get();
        }
    });

    private static final ServiceLoader<TimestampProvider> TIMESTAMP_PROVIDER_LOADER
            = new LazyServiceLoader<TimestampProvider>(Clock::getCalendar) {
    };


    /**
     * @return 获取当前时间，可以通过外部注入
     * @see TimestampProvider
     */
    public static Calendar now() {
        return (Calendar) TIMESTAMP_PROVIDER_LOADER.get().now().clone();
    }

    public static Float getMagnification() {
        return Config.getValue(
                KEY_MAGNIFICATION,
                Common.to(System.getProperty(KEY_MAGNIFICATION), 1.0f),
                "clock");
    }

    /**
     * @return “人间”当前时间戳
     * @see ClockAgent#currentTimeMillis()
     */
    public static long currentTimeMillis() {
        return agentSingleton.get().currentTimeMillis();
    }

    /**
     * @return “人间”当前时间
     * @see ClockAgent#getCalendar()
     */
    public static Calendar getCalendar() {
        return agentSingleton.get().getCalendar();
    }

    /**
     * @param millis 毫秒数
     * @throws InterruptedException InterruptedException
     * @see ClockAgent#sleep(long)
     */
    public static void sleep(long millis) throws InterruptedException {
        agentSingleton.get().sleep(millis);
    }

    /**
     * @param obj    obj
     * @param millis millis
     * @throws InterruptedException InterruptedException
     * @see ClockAgent#objWait(Object, long)
     */
    public static void objWait(Object obj, long millis) throws InterruptedException {
        agentSingleton.get().objWait(obj, millis);
    }

    /**
     * @param timeUnit timeUnit
     * @param timeout  timeout
     * @throws InterruptedException InterruptedException
     * @see ClockAgent#sleep(TimeUnit, long)
     */
    public static void sleep(TimeUnit timeUnit, long timeout) throws InterruptedException {
        agentSingleton.get().sleep(timeUnit, timeout);
    }

    public static long toMillis(long duration, TimeUnit timeUnit) {
        return agentSingleton.get().toMillis(duration, timeUnit);
    }
}
