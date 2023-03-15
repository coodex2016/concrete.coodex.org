/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
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

declare interface EnumItem<T>{
    key: string;
    value: T;
}
export declare class BaseEnum<T> {
    /**
     * 作废，命名拼写错误，某个枚举值的标签
     * @deprecated
     */
    _lableOf: (v: T) => string;
    /**
     * 某个枚举值的标签
     */
    _labelOf: (v: T) => string;
    /**
     * 常量的键值列表
     */
    _toArray: (values:T[])=>EnumItem<T>[];
}
