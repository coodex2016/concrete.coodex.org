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

import java.util.concurrent.Callable;

public class TracerTest {

    private final static Logger log = LoggerFactory.getLogger(TracerTest.class);


    public static void main(String[] args) {
        System.setProperty(Tracer.class.getName(), "true");

        Tracer.newTracer()
//                .logger(log) //  org.slf4j.Logger
//                .logger(String.class) // logger will be named after clazz
//                .logger("TEST") // logger name
//                .named("test") // tracer名
//                .named(new NameSupplier() { // supplier方式指定tracer名
//                    @Override
//                    public String getName() {
//                        return "test";
//                    }
//                })
                .trace(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Tracer.putTrace("hello", "coodex"); // 需要跟踪的信息项

                        Tracer.start("case1");
                        Clock.sleep(1000);
                        Tracer.end("case1");

                        Tracer.start("case2");
                        Clock.sleep(300);
                        Tracer.end("case2");
                        return null;
                    }
                });

    }
}
