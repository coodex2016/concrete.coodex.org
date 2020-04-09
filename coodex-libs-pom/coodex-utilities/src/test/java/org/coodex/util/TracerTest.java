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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

import static org.coodex.util.Common.sleep;

public class TracerTest {

    private final static Logger log = LoggerFactory.getLogger(TracerTest.class);


    public static void main(String[] args) {
        System.setProperty(Tracer.class.getName(), "true");

        System.out.println(
                Tracer.newTracer()
                        .trace(() -> {

                            Tracer.putTrace("hello", "coodex"); // 需要跟踪的信息项

                            Tracer.start("case1");
                            sleep(1000);
                            Tracer.end("case1");

                            Tracer.start("case2");
                            sleep(300);
                            Tracer.end("case2");
                            if (new Random().nextBoolean())
                                throw new RuntimeException("em~~~~");
                            else
                                return "hello tracer.";

                        })
        );
    }
}
