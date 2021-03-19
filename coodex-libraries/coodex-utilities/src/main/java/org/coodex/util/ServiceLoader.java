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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by davidoff shen on 2017-04-26.
 */
public interface ServiceLoader<T> {


    Map<String, T> getAll();


    T get(Class<? extends T> serviceClass);


    T get(String name);


    T get();

    T getDefault();

    /**
     * 对所有服务进行排序
     *
     * @return 排序后的所有服务
     * @see SPI
     */
    default List<T> sorted() {
        return sorted(Comparator.comparingInt(SPI::getServiceOrder));
    }

    /**
     * 对所有服务进行排序
     *
     * @param comparator comparator
     * @return 排序后的所有服务
     * @see SPI
     */
    default List<T> sorted(Comparator<? super T> comparator) {
        return Optional.ofNullable(getAll())
                .map(map ->
                        map.values().stream().sorted(comparator).collect(Collectors.toList())
                )
                .orElse(new ArrayList<>(0));
    }

}
