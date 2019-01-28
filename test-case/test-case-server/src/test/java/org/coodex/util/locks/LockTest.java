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

import org.coodex.concurrent.locks.*;
import org.coodex.util.Clock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;


public class LockTest {


    public static long test(Thread... threads) throws InterruptedException {
        AbstractLockTestThread.reset();
        long current = Clock.currentTimeMillis();

        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        long used = Clock.currentTimeMillis() - current;

        System.out.println(String.format("%s: %,d; used: %d ms",
                threads[0].getClass().getSimpleName(),
                AbstractLockTestThread.getSum(),
                used));
        return used;
    }

    public static long test(Class<? extends AbstractLockTestThread> threadClass, int threadCount) throws IllegalAccessException, InstantiationException, InterruptedException {
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = threadClass.newInstance();
        }
        return test(threads);
    }

    public static long test(Class<? extends AbstractLockTestThread> threadClass) throws InstantiationException, IllegalAccessException, InterruptedException {
        return test(threadClass, 3);
    }


    public static void main(String[] args) throws InterruptedException, IllegalAccessException, InstantiationException {
        int times = 3;
        Map<Class<? extends AbstractLockTestThread>, Long> total = new HashMap<Class<? extends AbstractLockTestThread>, Long>();
//        total.put(NoneLockThread.class, 0l);
        total.put(ReentrantLockThread.class, 0l);
        total.put(SynchronizedThread.class, 0l);
        total.put(ResourceLockThread.class, 0l);
        total.put(ResourceThread.class, 0l);
//        total.put(ZookeeperResourceThread.class, 0l);
        for (int x = 0; x < times; x++) {
            System.out.println("test start: " + (x + 1));
            for (Class<? extends AbstractLockTestThread> threadClass : total.keySet()) {
                total.put(threadClass, total.get(threadClass) + test(threadClass));
            }
        }

        System.out.println();
        for (Class<? extends AbstractLockTestThread> threadClass : total.keySet()) {
            System.out.println(String.format("%s: avg %.2f ms.",
                    threadClass.getSimpleName(),
                    total.get(threadClass) / 1.0f / times));
        }

    }
}

abstract class AbstractLockTestThread extends Thread {
    private static final long TIMES = 1 * 1000 * 1000;
    private static int sum = 0;

    public static int getSum() {
        return sum;
    }

    public static void reset() {
        sum = 0;
    }

    protected void sum_pp() {
        sum++;
    }


    @Override
    public void run() {
        for (long i = 0; i < TIMES; i++) {
            sum_pp();
        }
    }

}

class SynchronizedThread extends AbstractLockTestThread {
    @Override
    protected void sum_pp() {
        synchronized (SynchronizedThread.class) {
            super.sum_pp();
        }
    }
}

class ReentrantLockThread extends AbstractLockTestThread {
    private static ReentrantLock lock = new ReentrantLock();

    @Override
    protected void sum_pp() {
        try {
            SyncWrapper.call(lock, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ReentrantLockThread.super.sum_pp();
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class NoneLockThread extends AbstractLockTestThread {
}

class ResourceLockThread extends AbstractLockTestThread {
    private static ResourceLock lock =
            ResourceThread.resourceLockProvider.getLock(new ResourceId());

    @Override
    protected void sum_pp() {
        try {
            SyncWrapper.call(lock, new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ResourceLockThread.super.sum_pp();
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class ResourceThread extends AbstractLockTestThread {
    static ResourceLockProvider resourceLockProvider = new LocalResourceLockProvider();
    static ResourceId resourceId = new ResourceId("a", "x2");

    @Override
    protected void sum_pp() {
        try {
            SyncWrapper.call(resourceLockProvider.getLock(resourceId), new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    ResourceThread.super.sum_pp();
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//
//
//class ZookeeperResourceThread extends AbstractLockTestThread {
//
//    static TestZookeeperResourceLockProvider provider = new TestZookeeperResourceLockProvider();
//
//    @Override
//    protected void sum_pp() {
//        try {
//            SyncWrapper.call(provider.getLock(ResourceThread.resourceId), new Callable<Void>() {
//                @Override
//                public Void call() throws Exception {
//                    ZookeeperResourceThread.super.sum_pp();
//                    return null;
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//
//
//class TestZookeeperResourceLockProvider extends ZookeeperResourceLockProvider {
//
//    private Singleton<ZooKeeper> client = new Singleton<ZooKeeper>(
//            new Singleton.Builder<ZooKeeper>() {
//                @Override
//                public ZooKeeper build() {
////                    return new ZooKeeper("localhost:2181",50000,null);
////                    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
////                    CuratorFramework client = CuratorFrameworkFactory.newClient(
////                            "localhost:2181",
////                            500000,
////                            3000,
////                            retryPolicy);
////                    client.start();
////                    return client;
//                    return null;
//                }
//            }
//    );
//
//    public TestZookeeperResourceLockProvider() {
//        super();
//    }
//
//    @Override
//    protected ZooKeeper getClient() {
//        return client.getInstance();
//    }
//}