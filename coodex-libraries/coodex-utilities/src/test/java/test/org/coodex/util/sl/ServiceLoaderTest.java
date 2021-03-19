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

package test.org.coodex.util.sl;

import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceLoaderTest {
    private final static Logger log = LoggerFactory.getLogger(ServiceLoaderTest.class);

    public static void main(String[] args) {
        ServiceLoader<D<F>> serviceLoader1 = new LazyServiceLoader<D<F>>() {
        };
        serviceLoader1.getAll().forEach((key, v) -> {
            log.info("{}: {}", key, v);
        });

        ServiceLoader<E<B>> serviceLoader2 = new LazyServiceLoader<E<B>>() {
        };

        serviceLoader2.getAll().forEach((key, v) -> {
            log.info("{}: {}", key, v);
        });
    }
}
