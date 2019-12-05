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

public interface ClockAgent {
    /**
     * 替代 {@link System#currentTimeMillis()} 接口
     * @return
     */
    long currentTimeMillis();

    /**
     * 替代 {@link Calendar#getInstance()} 接口
     * @return
     */
    Calendar getCalendar();

    /**
     * 替代 {@link Thread#sleep(long)}  接口
     * @param millis
     */
    void sleep(long millis) throws InterruptedException;

    /**
     * 替代 {@link Object#wait(long)} 接口
     * @param obj
     * @param millis
     */
    void objWait(Object obj, long millis) throws InterruptedException;

    /**
     * 替代 {@link TimeUnit#sleep(long)} 接口
     * @param unit
     * @param timeout
     * @throws InterruptedException
     */
    void sleep(TimeUnit unit, long timeout) throws InterruptedException;

    long toMillis(long duration, TimeUnit timeUnit);
}
