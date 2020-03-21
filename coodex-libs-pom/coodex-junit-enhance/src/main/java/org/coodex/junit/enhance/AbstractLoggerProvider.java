/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.junit.enhance;

import org.coodex.util.SingletonMap;
import org.slf4j.Logger;

public abstract class AbstractLoggerProvider implements LoggerProvider {
    private SingletonMap<String, Logger> loggerSingletonMap = SingletonMap.<String, Logger>builder()
            .function(AbstractLoggerProvider.this::build).build();

    @Override
    public final Logger getLogger(String name) {
        return loggerSingletonMap.get(name);
    }

    protected abstract Logger build(String name);
}
