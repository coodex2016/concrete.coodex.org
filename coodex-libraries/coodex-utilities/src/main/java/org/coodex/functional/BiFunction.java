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

package org.coodex.functional;

import java.util.Objects;
import java.util.function.Function;

public interface BiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @return the function result
     */
    R apply(T t, U u);

//    /**
//     * Returns a composed function that first applies this function to
//     * its input, and then applies the {@code after} function to the result.
//     * If evaluation of either function throws an exception, it is relayed to
//     * the caller of the composed function.
//     *
//     * @param <V> the type of output of the {@code after} function, and of the
//     *           composed function
//     * @param after the function to apply after this function is applied
//     * @return a composed function that first applies this function and then
//     * applies the {@code after} function
//     * @throws NullPointerException if after is null
//     */
//    default <V> java.util.function.BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
//        Objects.requireNonNull(after);
//        return (T t, U u) -> after.apply(apply(t, u));
//    }
}
