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

package org.coodex.practice.saas.jersey;

import org.coodex.concrete.jaxrs.saas.Reverser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by davidoff shen on 2017-03-22.
 */
public class ExampleReverser implements Reverser {
    private final static Logger log = LoggerFactory.getLogger(ExampleReverser.class);

    @Override
    public boolean accept(String param) {
        log.debug("accept: {}", param);
        return true;
    }

    @Override
    public String resolve(String routeBy) {
        log.debug("resolve by: {}", routeBy);
        return "http://localhost:8080/test/s";
    }
}
