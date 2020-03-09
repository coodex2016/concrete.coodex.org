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

package org.coodex.concurrent.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by davidoff shen on 2017-04-01.
 */
public abstract class ToRetryRunnable implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ToRetryRunnable.class);


    private final Runnable command;
    private final int maxTimes;
    private int retryTimes = 0;

    public ToRetryRunnable(Runnable command, int maxTimes) {
        this.command = command;
        this.maxTimes = maxTimes;
    }

    @Override
    public void run() {
        try {
            command.run();
        } catch (Throwable th) {
            retryTimes++;
            if (getRetryTimes() < getMaxTimes()) {
                retry();
            } else {
                onFailed();
            }
        }
    }

    public int getMaxTimes() {
        return maxTimes;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    protected abstract void retry();

    protected void onFailed() {
    }


}
