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
import org.coodex.concrete.common.modules.AbstractModule;
import org.coodex.concrete.common.modules.AbstractParam;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.util.Common;

import java.lang.reflect.Method;
import java.util.List;
import java.util.StringJoiner;

//@SuppressWarnings("rawtypes")
public abstract class OwnServiceUnit/*<M extends OwnServiceModule>*/
        extends AbstractUnit<AbstractParam> {

    public static String getUnitKey(AbstractUnit<?> unit) {
        return Common.sha1(
                unit.getDeclaringModule().getInterfaceClass().getName() + "." +
                        unit.getMethod().getName() + "(" + getParameterTypesStr(unit.getMethod()) + ")"
        );
    }

    private final String key;

    public OwnServiceUnit(Method method, AbstractModule<?> module) {
        super(method, module);
        key = getUnitKey(this);
    }

    private static String getParameterTypesStr(Method method) {
        StringJoiner joiner = new StringJoiner(", ");
        for (Class<?> c : method.getParameterTypes()) {
            joiner.add(c.getName());
        }
        return joiner.toString();
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
    public int compareTo(AbstractUnit<AbstractParam> o) {
        return getName().compareTo(o.getName());
    }
}
