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

package org.coodex.metadata;


public interface Meta<T> {

    /**
     * @return 是否必须存在此键
     */
    default boolean isRequired() {
        return false;
    }

    /**
     * @return 是否必须非 {@code null}
     */
    default boolean isNotNull() {
        return false;
    }

    /**
     * @return 数据项名称，根元素为 ROOT
     */
    String getName();

}
