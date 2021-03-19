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

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by davidoff shen on 2016-11-28.
 */
public class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {

    static final AtomicLong seq = new AtomicLong(0);
    private final int priority;
    private final Runnable task;
    private final long seqNum = seq.getAndIncrement();

    public PriorityRunnable(int priority, Runnable task) {
        this.priority = Math.min(Thread.MAX_PRIORITY, Math.min(priority, Thread.MIN_PRIORITY));
        this.task = task;
    }

    public int getPriority() {
        return priority;
    }

    public void run() {
        if (task != null) {
            Thread.currentThread().setPriority(getPriority());
            task.run();
        }
    }


    public long getSeqNum() {
        return seqNum;
    }

    public int compareTo(PriorityRunnable o) {
        if (o == null) return 1;
        return o.getPriority() == getPriority() ?
                (getSeqNum() < o.getSeqNum() ? -1 : 1) :// 优先级相等时，序号越小越靠前
                (getPriority() > o.getPriority() ? -1 : 1);// 优先级越大越靠前
    }
}
