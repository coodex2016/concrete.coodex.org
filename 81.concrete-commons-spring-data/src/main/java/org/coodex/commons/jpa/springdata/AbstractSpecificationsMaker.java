/*
 * Copyright (c) 2017 coodex.org (jujus.shen@126.com)
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

package org.coodex.commons.jpa.springdata;

import org.coodex.concrete.common.Assert;
import org.coodex.concrete.common.ConcreteHelper;
import org.springframework.data.jpa.domain.Specifications;

import java.lang.reflect.Method;

import static org.coodex.commons.jpa.springdata.SpecCommon.wrapper;

public abstract class AbstractSpecificationsMaker<C, T> implements SpecificationsMaker<C, T> {


    @Override
    public Specifications<T> make(C condition) {
        return wrapper(make(condition, null));
    }

    public Specifications<T> make(C condition, String name) {

        name = name == null ? "" : name;

        Assert.is(name.equals("make"), "Invalid SpecificationsMaker function name: make");

        Method makerFunction = null;
        for (Method method : this.getClass().getMethods()) {
            MakerFunction function = method.getAnnotation(MakerFunction.class);
            String functionName = function == null ? method.getName() : method.getName();

            if (isMakerFunction(method) && functionName.equals(name)) {
                makerFunction = method;
                break;
            }
        }

        try {
            return (Specifications<T>)
                    Assert.isNull(makerFunction, "SpecificationsMaker function not exists: " + name)
                            .invoke(this, condition);
        } catch (Throwable th) {
            throw ConcreteHelper.getException(th);
        }
    }

    private boolean isMakerFunction(Method method) {
        // TODO check result type and parameter type
        return true;
    }
}
