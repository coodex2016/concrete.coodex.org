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

package org.coodex.concrete.own;

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.modules.AbstractParam;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.util.Common;

import java.lang.reflect.Method;
import java.util.List;

public abstract class OwnServiceUnit<M extends OwnServiceModule> extends AbstractUnit<AbstractParam, M> {
    private String key = null;

    public OwnServiceUnit(Method method, M module) {
        super(method, module);
        key = Common.sha1(String.format("%s:%s(%d)", // TODO "%s:%s(%s)",
                getDeclaringModule().getInterfaceClass().getName(),
                getName(),
                getParameters().length
                // TODO builder.toString()
        ));
    }

    @Override
    public String getName() {
        return getMethod().getName();
    }

    public String getKey() {
        return key;
    }

    @Override
    protected void afterInit() {
//        super.afterInit();
    }

    @Override
    protected AbstractParam buildParam(Method method, int index) {
        return new AbstractParam(method, index) {
        };
    }

    @Override
    protected AbstractParam[] toArrays(List<AbstractParam> abstractParams) {
        return abstractParams.toArray(new AbstractParam[0]);
    }

    @Override
    protected DefinitionContext toContext() {
        return ConcreteHelper.getContext(getMethod(), getDeclaringModule().getInterfaceClass());
    }

    @Override
    public int compareTo(AbstractUnit o) {
        return getName().compareTo(o.getName());
    }
}
