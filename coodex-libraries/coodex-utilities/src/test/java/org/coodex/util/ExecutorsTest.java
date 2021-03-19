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

import org.coodex.concurrent.ExecutorsHelper;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorsTest {


    public static void main(String[] args) {
        final AtomicInteger integer = new AtomicInteger(0);
        final ExecutorService executorService = ExecutorsHelper.newLinkedThreadPool(
                8, 20, 30000, "test"
        );
        final Set<String> set = new HashSet<String>();
//        ExecutorService executorService = ExecutorsHelper.newFixedThreadPool(5,"abab");
////            @Override
////            public Thread newThread(Runnable r) {
////                SecurityManager s = System.getSecurityManager();
////                ThreadGroup group = (s != null) ? s.getThreadGroup() :
////                        Thread.currentThread().getThreadGroup();
////                Thread t = new Thread(group, r,
////                        "aaaa-" + integer.incrementAndGet(),
////                        0);
////                if (t.isDaemon())
////                    t.setDaemon(false);
////                if (t.getPriority() != Thread.NORM_PRIORITY)
////                    t.setPriority(Thread.NORM_PRIORITY);
////                return t;
////            }
////        });

        final int MAX = 1000;
        for (int i = 1; i <= MAX; i ++){
            final int finalI = i;
            executorService.execute(new Runnable() {
                private boolean con = true;
                @Override
                public void run() {
                    set.add(Thread.currentThread().getName());
//                    System.out.println(Thread.currentThread().getName());
                    if(finalI == MAX){
                        System.out.println(set.size());
                    }
//                    System.out.println(Thread.currentThread().getName());
                    try {
                        Thread.sleep(new Random().nextInt(100));
                        if (con) {
                            con = false;
                            final Runnable x = this;
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    x.run();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

//        ExecutorsHelper.shutdownAll();
    }
}
