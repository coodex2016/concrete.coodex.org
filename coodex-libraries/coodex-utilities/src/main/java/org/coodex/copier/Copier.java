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

package org.coodex.copier;

import java.util.Collection;

/**
 * 定义复制PO(持久化对象)/VO(视图对象)的标准
 * <p>
 * 根据 sujiwu@126.com 的设计思路、代码修改
 * <p>
 * 由AbstractCopier提供默认实现，具体复制实例实现copy(src, target)即可，如有需要，可自行重载init(target)方法
 *
 * @see AbstractCopier
 * <p>
 * Created by davidoff shen on 2017-03-17.
 */
public interface Copier<SRC, TARGET> {

    /**
     * @return 新建目标对象实例
     */
    TARGET newTargetObject();

    /**
     * @param target 目标对象实例
     * @return 初始化目标对象
     */
    TARGET initTargetObject(TARGET target);

    /**
     * @return 初始化一个新的目标实例
     */
    TARGET initTargetObject();

    /**
     * @param src    src
     * @param target target
     * @return 属性复制
     */
    TARGET copy(SRC src, TARGET target);

    /**
     * @param src
     * @return 复制一个新的目标实例
     */
    TARGET copy(SRC src);


    /**
     * 复制集合
     *
     * @param srcCollection srcCollection
     * @param clazz         clazz
     * @param <T>           <T>
     * @return 集合复制
     */
    <T extends Collection<TARGET>> T copy(Collection<SRC> srcCollection, Class<T> clazz);


    /**
     * @param srcCollection srcCollection
     * @return 集合复制
     */
    Collection<TARGET> copy(Collection<SRC> srcCollection);

}
