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

package org.coodex.pojomocker;

import org.coodex.util.Common;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 在自定义的关联处理方法上增加RelationMethod即可
 * <p>
 * Created by davidoff shen on 2017-05-17.
 */
@Deprecated
public abstract class AbstractRelationPolicy /*extends StringAcceptableService*/ implements RelationPolicy {

//    @Override
//    protected String getName() {
//        return getPolicyName();
//    }


    @Override
    public boolean accept(String param) {
        return Common.inArray(param, getPolicyNames());
    }

    @Override
    public final Object relate(String policyName, List fieldValues) {
        try {
            for (Method method : getClass().getMethods()) {
                RelationMethod relationMethod = method.getAnnotation(RelationMethod.class);
                if (relationMethod != null && relationMethod.value().equals(policyName)) {
                    method.setAccessible(true);
                    return method.invoke(this, fieldValues.toArray());
                }
            }
        } catch (Throwable th) {
            throw new RuntimeException(th.getLocalizedMessage(), th);
        }
        throw new RuntimeException("relation method for [" + policyName + "] not found.");
    }


}
