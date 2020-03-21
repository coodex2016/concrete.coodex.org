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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingletonMapTest {
    private final static Logger log = LoggerFactory.getLogger(SingletonMapTest.class);

    public static void main(String[] args) throws InterruptedException {
        SingletonMap<String, String> map = SingletonMap.<String, String>builder()
                .function(key -> {
                    log.debug("new object build for {}", key);
                    return key;
                })
                .activeOnGet(true)
                .maxAge(1000)
                .build();

        for (int i = 1; i < 50; i++) {
            map.get("123");
            Thread.sleep(i * 200);
        }
    }
}
