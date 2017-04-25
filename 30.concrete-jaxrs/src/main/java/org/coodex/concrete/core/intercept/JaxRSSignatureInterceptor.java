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

package org.coodex.concrete.core.intercept;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-25.
 */
public class JaxRSSignatureInterceptor extends AbstractSignatureInterceptor {
    @Override
    protected Map<String, Object> buildContent(RuntimeContext context, Object[] args) {
        Unit unit = JaxRSHelper.getUnitFromContext(context, args);
        Map<String, Object> result = new HashMap<String, Object>();
        for (Param param : unit.getParameters()) {
            result.put(param.getName(), args[param.getIndex()]);
        }
        return result;
    }

    @Override
    protected void setArgument(RuntimeContext context, MethodInvocation joinPoint, String parameterName, Object value) {
        Unit unit = JaxRSHelper.getUnitFromContext(context, joinPoint);
        for (Param param : unit.getParameters()) {
            if (param.getName().equals(parameterName)) {
                joinPoint.getArguments()[param.getIndex()] = value;
                break;
            }
        }
    }
}
