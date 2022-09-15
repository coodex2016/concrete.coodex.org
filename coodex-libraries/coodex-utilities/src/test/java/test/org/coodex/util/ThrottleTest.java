/*
 * Copyright (c) 2016 - 2022 coodex.org (jujus.shen@126.com)
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

package test.org.coodex.util;

import org.coodex.concurrent.Throttle;
import org.coodex.util.Clock;
import org.coodex.util.Common;

public class ThrottleTest {
    public static void main(String[] args) throws InterruptedException {
        Throttle throttle = Throttle.newBuilder()
                .asyncAlways(true)
                .interval(1000).build();

        long now = Clock.currentTimeMillis();
        int i = 0;
        while (Clock.currentTimeMillis() - now < 4500) {
            Common.sleep(1);
//            TimeUnit.NANOSECONDS.sleep(1);
            i++;
            int finalI = i;
            throttle.submit(() -> {
                System.out.println(Clock.currentTimeMillis() + "  " + finalI + " thread: " + Thread.currentThread().getName());
            });
        }

    }
}
