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

package org.coodex.concrete.common;

import org.coodex.concrete.common.modules.AbstractUnit;

import java.lang.reflect.Method;
import java.util.List;

public class AUnit extends AbstractUnit<AParam> {
    public AUnit(Method method, AModule module) {
        super(method, module);
    }

    @Override
    public String getName() {
        return getMethod().getName();
    }

    @Override
    protected AParam buildParam(Method method, int index) {
        return new AParam(method, index);
    }

    @Override
    public String getInvokeType() {
        return null;
    }

    @Override
    protected AParam[] toArrays(List<AParam> localParams) {
        return localParams.toArray(new AParam[0]);
    }

    @Override
    protected DefinitionContext toContext() {
        return ConcreteHelper.getContext(getMethod(), getDeclaringModule().getInterfaceClass());
    }

    @Override
    public int compareTo(AbstractUnit<AParam> o) {
        return 0;
    }
}
