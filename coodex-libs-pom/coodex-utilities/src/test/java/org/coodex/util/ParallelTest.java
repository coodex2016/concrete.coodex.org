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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.coodex.concurrent.Parallel;

import java.util.Arrays;

public class ParallelTest {

    public static void main(String[] args) {
        //使用10个线程的线程池作为并行处理容器
//        Parallel parallel = new Parallel(ExecutorsHelper.newFixedThreadPool(10));
        Parallel parallel = new Parallel();
        Runnable[] runnables = new Runnable[20];
        // 每个任务随机执行0-5000毫秒, 20%几率抛异常
        Arrays.fill(runnables, (Runnable) () -> {
            try {
                Thread.sleep(Common.random(5000));
                if (Common.random(5) > 3)
                    throw new RuntimeException("test");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        //所有任务执行完成后
        Parallel.Batch batch = parallel.run(runnables);

        System.out.println(JSON.toJSONString(batch, SerializerFeature.PrettyFormat));

        System.out.println(batch.getTimeConsuming());

//        Callable<Long>[] callables = new Callable<Long>[20];
//        Arrays.fill(callables, () -> {
//            try {
//                Thread.sleep(Common.random(5000));
//                if (Common.random(5) > 3)
//                    throw new RuntimeException("test");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return Clock.currentTimeMillis();
//        });
//
//        Parallel.CallableBatch<Long> callableBatch = parallel.call(callables);
//        System.out.println(JSON.toJSONString(callableBatch, SerializerFeature.PrettyFormat));
//
//        System.out.println(callableBatch.getTimeConsuming());
    }
}
