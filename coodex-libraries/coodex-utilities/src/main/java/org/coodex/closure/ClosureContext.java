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
 *
 * @author davidoff shen
 */
public interface ClosureContext<T> {
    /**
     * 获取上下文中的变量值
     *
     * @return 获取上下文中的变量值
     */
    T get();

    /**
     * 带着上下文数据执行带返回值的方法
     *
     * @param var      上下文数据
     * @param supplier supplier
     * @return supplier.get()
     */
    Object call(T var, Supplier<?> supplier);

}
