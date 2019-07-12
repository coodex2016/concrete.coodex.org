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

/**
 * 单值模拟器原型
 */
public interface Mocker {

    /**
     * 模拟器是否适用目标类型和指定的用Mock修饰过的Annotation
     *
     * @param mockAnnotation
     * @param targetType
     * @return
     */
    boolean accept(Annotation mockAnnotation, Type targetType);

    /**
     * 根据mockAnnotation和目标类型
     *
     * @param mockAnnotation
     * @param targetType
     * @return
     */
    Object mock(Annotation mockAnnotation, Mock.Nullable nullable, Type targetType);
}
