/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

import org.coodex.concurrent.ExecutorsHelper;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorTest2 {
    public static void main(String[] args) {

        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
//        String pid = rt.getName();
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
//        ExecutorService executorService = ExecutorsHelper.newScheduledThreadPool(
//                128, "test"
//        );
//        AtomicLong atomicLong = new AtomicLong();
//        ThreadPoolExecutor executorService = new ThreadPoolExecutor(
//                4, 100, 60L, TimeUnit.SECONDS,
//                new LinkedBlockingQueue<>(), r -> {
//            Thread thread = new Thread(r);
//            thread.setName("test-" + atomicLong.incrementAndGet());
//            return thread;
//        }
//        );
        ExecutorService executorService = ExecutorsHelper.newLinkedThreadPool(
            2,20,Integer.MAX_VALUE,2L, "test"
        );

        for (int i = 0; i < 5; i++) {
            int finalI = i;
            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + ": " + Thread.currentThread().getId() + " " + finalI);

            });
            Common.sleep(500);
//            System.out.println(executorService.getActiveCount());
        }
//
        Common.sleep(5000);
////        System.out.println(executorService.getActiveCount());
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executorService.execute(() -> {
                Common.sleep(10);
                System.out.println(Thread.currentThread().getName() + ": " +
                        Thread.currentThread() + " " + finalI);

            });
        }
////        executorService = ExecutorsHelper.wrap
//        executorService.execute(() -> {
//            Common.sleep(500);
//            lock.lock();
//            System.out.println(Thread.currentThread().getName() + " locked" + "  " + rt.getName());
//            try {
//                Common.sleep(10000);
//                condition.await();
//                System.out.println(Thread.currentThread().getName() + " lock notified.");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                lock.unlock();
//            }
//        });


//        System.out.println(threadPoolExecutor.getActiveCount());
//        Common.sleep(1000);
//        System.out.println(threadPoolExecutor.getActiveCount());
//        executorService.execute(() -> {
//            System.out.println(threadPoolExecutor.getActiveCount());
//            System.out.println(Thread.currentThread().getName() + " executed.");
//            lock.lock();
//            try {
//                System.out.println(Thread.currentThread().getName() + " locked");
//                condition.signal();
//            } finally {
//                lock.unlock();
//            }
//            Common.sleep(1000000);
//        });
//        for(int i = 0; i < 100; i ++) {
//            int finalI = i;
//            executorService.execute(() -> {
//                System.out.println(Thread.currentThread().getName() + ": " + finalI);
//
//            });
//        }
    }
}
