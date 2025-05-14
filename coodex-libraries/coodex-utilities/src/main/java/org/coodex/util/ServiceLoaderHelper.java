/*
 * Copyright (c) 2016 - 2025 coodex.org (jujus.shen@126.com)
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

import java.util.*;
import java.util.stream.Collectors;

public class ServiceLoaderHelper {

    public static <T> List<T> sort(ServiceLoader<T> serviceLoader, Comparator<? super T> comparator) {
//        serviceLoader.sorted(comparator);
        Map<String, T> map = serviceLoader.getAll();
        List<T> sorted = new ArrayList<>();
        if (map != null) {
            sorted.addAll(map.values());
            Collections.sort(sorted, comparator);
        }
        return sorted;
    }

    public static <T> List<T> sort(ServiceLoader<T> serviceLoader) {
        return sort(serviceLoader, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return SPI.getServiceOrder(o1) - SPI.getServiceOrder(o2);
            }
        });
    }

}
