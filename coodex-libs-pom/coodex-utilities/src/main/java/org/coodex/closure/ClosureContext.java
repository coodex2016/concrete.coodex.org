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

package org.coodex.closure;

import java.util.function.Supplier;

/**
 * Created by davidoff shen on 2017-04-20.
 */
public interface ClosureContext<T> {
    /**
     * @return 获取上下文中的变量值
     */
    T get();

    Object call(T var, Supplier<?> supplier);

//    @Deprecated
//    Object call(T var, CallableClosure callable) throws Throwable;
//
//    /**
//     * 如果运行有异常，则转为运行期异常
//     *
//     * @param var
//     * @param callableClosure
//     * @return
//     */
//    @Deprecated
//    Object useRTE(T var, CallableClosure callableClosure);
}
