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


import org.coodex.util.Common;

import java.util.Collection;

/**
 * 根据 sujiwu@126.com 的设计思路、代码修改
 * <p>
 * Created by davidoff shen on 2017-03-17.
 */
public abstract class AbstractCopier<SRC, TARGET>
        extends AbstractCopierCommon
        implements Copier<SRC, TARGET> {

    public TARGET newTargetObject() {
        return Common.cast(newObject(Index.B));
    }

    @Override
    public TARGET initTargetObject(TARGET target) {
        return Common.cast(init(target, Index.B));
    }

    @Override
    public TARGET initTargetObject() {
        return initTargetObject(null);
    }

    public TARGET copy(SRC src) {
        return copy(src, initTargetObject());
    }

    @Override
    protected Object copy(Object o, Index srcIndex) {
        SRC src = Common.cast(o);
        return copy(src);
    }

    @Override
    public <T extends Collection<TARGET>> T copy(Collection<SRC> srcCollection, Class<T> clazz) {
        return copy(srcCollection, clazz, Index.A);
    }

    @Override
    public Collection<TARGET> copy(Collection<SRC> srcCollection) {
        return copy(srcCollection, Index.A);
    }
}
