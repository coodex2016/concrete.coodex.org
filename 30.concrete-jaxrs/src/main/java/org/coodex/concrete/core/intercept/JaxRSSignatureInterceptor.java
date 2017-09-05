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
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.RuntimeContext;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.Common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-04-25.
 */
public class JaxRSSignatureInterceptor extends AbstractSignatureInterceptor {

    private String methodToProperty(Method method) {
        if(method.getParameterTypes().length != 0) return null;

        if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
            return null;
        }
        String methodName = method.getName();
        if (methodName.startsWith("get")) {
            return Common.lowerFirstChar(methodName.substring(3));
        } else if (methodName.startsWith("is") &&
                (method.getReturnType().equals(boolean.class) || method.getReturnType().equals(Boolean.class))) {
            return Common.lowerFirstChar(methodName.substring(2));
        }
        return null;
    }

    private Map<String, Object> beanToMap(Object bean) throws InvocationTargetException, IllegalAccessException {
        Class c = bean.getClass();
        Map<String, Object> objectMap = new HashMap<String, Object>();
        for (Method method : c.getMethods()) {
            String property = methodToProperty(method);
            if(property != null){
                method.setAccessible(true);
                Object o = method.invoke(bean);
                if(o != null) objectMap.put(property, o);
            }

        }
        return objectMap;
    }


    @Override
    protected Map<String, Object> buildContent(RuntimeContext context, Object[] args) {
        Unit unit = JaxRSHelper.getUnitFromContext(context, args);
        Param[] params = unit.getParameters();
        if (params == null) return new HashMap<String, Object>();
        // 1个参数的情况
        if (params.length == 1) {
            Class c = params[0].getType();
            // 非集合、数组、基础类型
            if (!Collection.class.isAssignableFrom(c) && !c.isArray() && !JaxRSHelper.isPrimitive(c)) {
                try {
                    return beanToMap(args[0]);
                } catch (Throwable th) {
                    throw ConcreteHelper.getException(th);
                }
            }
        }

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

    @Override
    protected String dataToString(byte[] data) {
        return new String(data);
    }
}
