/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.concrete;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.util.Common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorTest {
    static boolean error = false;

    public static void main(String[] args) throws InterruptedException {
        final ThreadPoolExecutor executorService = (ThreadPoolExecutor) ExecutorsHelper.newPriorityThreadPool(20, 80);
//        test1(executorService);
//        test2(executorService);
        test3(executorService);


        while (true) {
            System.out.println(executorService);
            Thread.sleep(1000);
        }
//        Thread.sleep(5000);
//        System.out.println(executorService);
//        executorService.shutdown();
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(Common.random(1000,10000));
////                        System.out.println("threadId: " + Thread.currentThread().getId());
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });


    }
    private static void test3(final ThreadPoolExecutor executorService) throws InterruptedException {
        new Thread(){

            @Override
            public void run() {
                test1(executorService);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while(executorService.getActiveCount() > 0){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(executorService.getKeepAliveTime(TimeUnit.MILLISECONDS) + 1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                test1(executorService);
            }
        }.start();

    }

    private static void test1(final ThreadPoolExecutor executorService) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 100; i ++){
                    try {
                        for(int x = 0; x < 100; x ++) {
                            postTask(executorService);
                        }
                    }catch (RejectedExecutionException e){
                        e.printStackTrace();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(Common.random(1,50));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static void postTask(ThreadPoolExecutor executorService) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(Common.random(10, 200));
                    if (error) {
                        Thread.sleep(1000000000);
                    }
//                        System.out.println("threadId: " + Thread.currentThread().getId());
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }

        });
    }

    private static void test2(ThreadPoolExecutor executorService) {
        for (int i = 0; i < 10000; i++) {
            postTask(executorService);
        }
    }
}
