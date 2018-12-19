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

package org.coodex.util.locks;

import java.util.concurrent.locks.ReentrantLock;

public class Test2 {

    static final ReentrantLock lock = new ReentrantLock();

    private static void trace(String str){
        System.out.println(String.format("%s, thread: %d; queueLen: %d; isHeld: %s; holdCount: %d",
                str,
                Thread.currentThread().getId(),
                lock.getQueueLength(),
                String.valueOf(lock.isHeldByCurrentThread()),
                lock.getHoldCount()));
    }

    public static void main(String[] args) {

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        trace("before");
                        lock.lock();
                        trace("after1");
                        lock.lock();
                        trace("after2");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        trace("after3");
                        lock.unlock();
                        trace("unlck1");
                        lock.unlock();
                        trace("unlck2");
                    }
                }
        ).start();

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        trace("before");
                        if(lock.tryLock()) {
                            trace("after ");
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            trace("afterX");
                            lock.unlock();
                            trace("unlock");
                        } else {
                            trace("XXXXXX");
                        }
                    }
                }
        ).start();
    }
}
