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
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorTest {
    public static void main(String [] args) throws InterruptedException {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) ExecutorsHelper.newPriorityThreadPool(5,10);

        for(int i = 0; i < 1000; i ++){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(Common.random(10,100));
//                        System.out.println("threadId: " + Thread.currentThread().getId());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        while(true){
            System.out.println(String.format("poolSize: %d; activedCount: %d", executorService.getPoolSize(), executorService.getActiveCount()));
            Thread.sleep(5000);
        }


    }
}
