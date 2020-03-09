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

package org.coodex.concurrent;

import org.coodex.util.Common;
import org.coodex.util.Singleton;

import java.util.concurrent.ScheduledExecutorService;

public abstract class AbstractCoalition<T> implements Coalition<T> {

    protected final ScheduledExecutorService scheduledExecutorService;// = Executors.newScheduledThreadPool(1);
    protected final Coalition.Callback<T> callback;
    protected final int interval;

    private static final Singleton<ScheduledExecutorService> sesSingleton = new Singleton<ScheduledExecutorService>(
            new Singleton.Builder<ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build() {
                    return ExecutorsHelper.newScheduledThreadPool(Common.toInt(
                            System.getProperty("coalition.executors.size"), 3
                            ),
                            "CoalitionPool");
                }
            }
    );

    public AbstractCoalition(Coalition.Callback<T> c, int interval, ScheduledExecutorService scheduledExecutorService) {
        if (scheduledExecutorService == null) throw new NullPointerException("scheduledExecutorService is null.");
        this.scheduledExecutorService = scheduledExecutorService;
        this.callback = c;
        this.interval = interval;
    }

    public AbstractCoalition(Coalition.Callback<T> c, int interval) {
        this(c, interval, sesSingleton.get());
    }


//    public void terminate() {
//        scheduledExecutorService.shutdownNow();
//    }


}
