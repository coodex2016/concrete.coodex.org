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

package org.coodex.concurrent.locks;

import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractResourceLockProvider implements ResourceLockProvider {
    public final static long RESOURCE_CACHE_MAX_LIFE = 10000L; // 10 seconds
    public final static long POLLING_CYCLE = RESOURCE_CACHE_MAX_LIFE / 2;
    private final static Logger log = LoggerFactory.getLogger(AbstractResourceLockProvider.class);
    private static AbstractResourceLock[] toArraysParam = new AbstractResourceLock[0];
    private static Comparator<AbstractResourceLock> comparator = new Comparator<AbstractResourceLock>() {
        @Override
        public int compare(AbstractResourceLock o1, AbstractResourceLock o2) {
            return (int) (o1.getLastActive() - o2.getLastActive());
        }
    };
    private static Singleton<ScheduledExecutorService> scheduledExecutorServiceSingleton = new Singleton<ScheduledExecutorService>(

            new Singleton.Builder<ScheduledExecutorService>() {
                @Override
                public ScheduledExecutorService build() {
                    return ExecutorsHelper.newSingleThreadScheduledExecutor("cleanDeathResource");
                }
            }
    );
    protected Map<ResourceId, AbstractResourceLock> locksMap = new HashMap<ResourceId, AbstractResourceLock>(8);

    private Runnable cleanRunner = new Runnable() {
        @Override
        public void run() {
            try {
                cleanDeathResource();
            } finally {
                poll();
            }
        }
    };


    protected abstract AbstractResourceLock buildResourceLock(ResourceId id);

    public AbstractResourceLockProvider() {
        poll();
    }

    @Override
    public ResourceLock getLock(ResourceId id) {
        synchronized (locksMap) {
            if (!locksMap.containsKey(id)) {
                locksMap.put(id, buildResourceLock(id));
            }
            return locksMap.get(id).active();
        }
    }

    private void cleanDeathResource() {
        if (locksMap.size() == 0) return;

        synchronized (locksMap) {
            StringBuilder builder = new StringBuilder();
            if (log.isDebugEnabled()) {
                builder.append(getClass().getName()).append(" before clean: ").append(locksMap.size())
                        .append(" resource(s)");
            }
            int count = 0;
            AbstractResourceLock[] locks = locksMap.values().toArray(toArraysParam);
            Arrays.sort(locks, comparator);
            for (int i = 0, l = locks.length; i < l; i++) {
                if (locks[i].isDeath()) {
                    locksMap.remove(locks[i].getId());
                    count++;
                } else {
                    break;
                }
            }
            if (log.isDebugEnabled() && count > 0) {
                builder.append("\n\tafter clean: ").append(locksMap.size()).append(" resource(s).");
                log.debug(builder.toString());
            }
        }
    }

    protected void poll() {
        scheduledExecutorServiceSingleton.getInstance().schedule(
                cleanRunner, POLLING_CYCLE, TimeUnit.MILLISECONDS
        );
    }
}
