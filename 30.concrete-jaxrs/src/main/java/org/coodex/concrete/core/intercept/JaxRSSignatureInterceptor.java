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

import org.coodex.concrete.common.ConcreteContext;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.jaxrs.JaxRSHelper;

/**
 * Created by davidoff shen on 2017-04-25.
 */
@Deprecated
public class JaxRSSignatureInterceptor extends AbstractSignatureInterceptor {
    @Override
    public boolean accept(RuntimeContext context) {
        return super.accept(context) && JaxRSHelper.JAXRS_MODEL.equals(ConcreteContext.MODEL.get());
    }

//    @Override
//    protected Map<String, Object> buildContent(RuntimeContext context, Object[] args) {
////        Unit unit = JaxRSHelper.getUnitFromContext(context, args);
//        return buildContent(JaxRSHelper.getUnitFromContext(context, args), args);
////        Param[] params = unit.getParameters();
////        if (params == null) return new HashMap<String, Object>();
////        // 1个参数的情况
////        if (params.length == 1) {
////            Class c = params[0].getType();
////            // 非集合、数组、基础类型
////            if (!Collection.class.isAssignableFrom(c) && !c.isArray() && !JaxRSHelper.isPrimitive(c)) {
////                try {
////                    return beanToMap(args[0]);
////                } catch (Throwable th) {
////                    throw ConcreteHelper.getException(th);
////                }
////            }
////        }
////
////        Map<String, Object> result = new HashMap<String, Object>();
////
////        for (Param param : unit.getParameters()) {
////            result.put(param.getName(), args[param.getIndex()]);
////        }
////        return result;
//    }

//    @Override
//    protected AbstractUnit getUnitFromContext(RuntimeContext context) {
//        return JaxRSHelper.getUnitFromContext(context/*,args*/);
//    }

//    @Override
//    protected void setArgument(RuntimeContext context, MethodInvocation joinPoint, String parameterName, Object value) {
//        Unit unit = JaxRSHelper.getUnitFromContext(context, joinPoint);
//        for (Param param : unit.getParameters()) {
//            if (param.getName().equals(parameterName)) {
//                joinPoint.getArguments()[param.getIndex()] = value;
//                break;
//            }
//        }
//    }

    @Override
    protected String dataToString(byte[] data) {
        return new String(data);
    }
}
