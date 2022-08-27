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

import org.coodex.util.LazyServiceLoader;
import org.coodex.util.ServiceLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceLoaderTestCase {

    @Test
    public void test1() {
        ServiceLoader<ServiceA> serviceAServiceLoader = new LazyServiceLoader<ServiceA>() {
        };

        ServiceLoader<ServiceB> serviceBServiceLoader = new LazyServiceLoader<ServiceB>() {
        };

        Assertions.assertEquals(serviceAServiceLoader.getAll().size(), 1);
        Assertions.assertEquals(serviceBServiceLoader.getAll().size(), 1);


    }

    public static class AImpl implements ServiceA {
    }

    public static class BImpl implements ServiceB {
    }

}
