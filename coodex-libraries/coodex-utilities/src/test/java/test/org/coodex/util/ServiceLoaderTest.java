/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

import org.coodex.util.LazySelectableServiceLoader;
import org.coodex.util.LazyServiceLoader;

public class ServiceLoaderTest {

    private final static LazySelectableServiceLoader<Object, DemoService<Object>> demoServices =
            new LazySelectableServiceLoader<Object, DemoService<Object>>(/*(DemoService<Object>) param -> true*/) {
            };

    private final static LazyServiceLoader<DemoService<Object>> demoServices2 =
            new LazyServiceLoader<DemoService<Object>>(param -> true) {
            };

    public static void main(String[] args) {
        System.out.println('\0');
        long x = System.currentTimeMillis();
        long count = 0;
        for (int i = 0; i < 100000; i++) {
            count += demoServices.selectAll(null).size();
//            demoServices.getDefault();
        }
        System.out.println(count + " / " + (System.currentTimeMillis() - x));
        x = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
//            demoServices.select(null);
            demoServices2.get();

        }
        System.out.println(System.currentTimeMillis() - x);
    }
}
