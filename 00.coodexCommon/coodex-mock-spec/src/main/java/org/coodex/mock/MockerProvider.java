/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public interface MockerProvider {


    /**
     * @param type        模拟的数据类型
     * @param annotations 模拟配置
     * @param <T>         类型泛型
     * @return 模拟值
     */
    <T> T mock(Class<T> type, Annotation... annotations);

    /**
     * @param type        要模拟类型的type，需要是具体的，不能有{@link java.lang.reflect.TypeVariable}
     * @param annotations 模拟配置
     * @return 模拟值
     */
    Object mock(Type type, Annotation... annotations);

}
