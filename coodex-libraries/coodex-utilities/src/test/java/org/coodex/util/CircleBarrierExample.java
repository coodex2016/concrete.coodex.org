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

package org.coodex.util;

import org.coodex.concurrent.ExecutorsHelper;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;

public class CircleBarrierExample {
    static ExecutorService executorService = ExecutorsHelper.newFixedThreadPool(2, "CirclicBarrier");

    private static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    @Test
    public void test() {
        final CyclicBarrier barrier = new CyclicBarrier(5);

        for (int i = 0; i < 5; i++) {
            final int finalI = i;
            execute(() -> {
                for (int j = 0; j < 4; j++) {
                    try {
                        Thread.sleep(Common.random(200, 1000));
                        barrier.await();
                        System.out.printf("id: %d stage %d ok.%n", finalI, j);
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                }
            });


        }
    }
}
