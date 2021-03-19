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

import org.coodex.util.Clock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.coodex.concurrent.locks.AbstractResourceLockProvider.RESOURCE_CACHE_MAX_LIFE;


public abstract class AbstractResourceLock implements ResourceLock {


    private final ResourceId resourceId;
    private final ReentrantLock lock = new ReentrantLock();
    private long lastActive = Clock.currentTimeMillis();

    public AbstractResourceLock(ResourceId resourceId) {
        this.resourceId = resourceId;
    }


    /**
     * 申请逻辑锁，阻塞直到成功
     */
    protected abstract void alloc();

    /**
     * @return 逻辑锁是否已分配
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected abstract boolean allocated();

    /**
     * 释放逻辑锁
     */
    protected abstract void release();

    /**
     * 尝试申请逻辑锁
     *
     * @return 申请成功返回true, 否则false
     */
    protected abstract boolean tryAlloc();

    /**
     * 尝试申请逻辑锁，最长time毫秒
     *
     * @param time time
     * @return 申请成功返回true, 否则false
     */
    protected abstract boolean tryAlloc(long time);

    public AbstractResourceLock active() {
        lastActive = Clock.currentTimeMillis();
        return this;
    }

    public long getLastActive() {
        return lastActive;
    }

    @Override
    public void lock() {
        active();
        lock.lock();
        if (!allocated()) {
            synchronized (this) {
                if (!allocated()) {
                    try {
                        alloc();
                    } catch (RuntimeException re) {
                        lock.unlock();
                        throw re;
                    }
                }
            }
        }
    }


    public boolean isDeath() {
        return lastActive + RESOURCE_CACHE_MAX_LIFE < Clock.currentTimeMillis();
    }

    @Override
    public boolean tryLock() {
        active();
        boolean locked = lock.tryLock();
        if (locked) {
            if (!allocated()) {
                synchronized (this) {
                    if (!allocated()) {
                        if (tryAlloc()) {
                            return true;
                        } else {
                            lock.unlock();
                            locked = false;
                        }
                    }
                }
            }
        }
        return locked;
    }


    private long toMillis(long time, TimeUnit unit) {
        return unit.toMillis(time);
//        switch (unit) {
//            case NANOSECONDS:
//                return time / 1000000L;
//            case MICROSECONDS:
//                return time / 1000L;
//            case MILLISECONDS:
//                return time;
//            case SECONDS:
//                return time * 1000L;
//            case MINUTES:
//                return time * 1000L * 60;
//            case HOURS:
//                return time * 1000L * 60 * 60;
//            case DAYS:
//                return time * 1000L * 60 * 60 * 24;
//        }
//        return 0l;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        active();
        long deathLine = Clock.currentTimeMillis() + toMillis(time, unit);
        boolean locked = lock.tryLock(time, unit);
        if (locked) {
            if (!allocated()) {
                synchronized (this) {
                    if (!allocated()) {
                        if (tryAlloc(deathLine - Clock.currentTimeMillis())) {
                            return true;
                        } else {
                            lock.unlock();
                            locked = false;
                        }
                    }
                }
            }
        }
        return locked;
    }

    @Override
    public void unlock() {
        active();

        if (lock.getQueueLength() == 0 && lock.getHoldCount() == 1) {
            release();
        }
        lock.unlock();
//
//        if (lock.getHoldCount() == 0 && lock.getQueueLength() == 0) {
//            release();
//        }
    }

    public ResourceId getId() {
        return resourceId;
    }

}
