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

package test.org.coodex.concrete.message;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concurrent.ExecutorsHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class ExecutorsTest {

    public static void main(String[] args) {
        int MAX = 100;
        Set<String> set = new HashSet<>();
        ExecutorService executorService = ConcreteHelper.getExecutor();
        for (int i = 1; i <= MAX; i++) {
            int finalI = i;
            executorService.execute(() -> {
                set.add(Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                    if (finalI == MAX) {
                        System.out.println(set.size());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        ExecutorsHelper.shutdownAll();
    }
}
