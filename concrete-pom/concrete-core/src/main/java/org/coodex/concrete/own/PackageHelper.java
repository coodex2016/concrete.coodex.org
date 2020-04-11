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

import org.coodex.concrete.common.JSONSerializer;
import org.coodex.concrete.common.JSONSerializerFactory;
import org.coodex.concrete.common.modules.AbstractParam;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.util.Common;
import org.coodex.util.GenericTypeHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.coodex.util.GenericTypeHelper.toReference;
import static org.coodex.util.TypeHelper.isPrimitive;

//import org.coodex.util.GenericType;
//import org.coodex.util.TypeHelper;

public class PackageHelper {

    private static ThreadLocal<Class<?>> context = new ThreadLocal<>();

    private static Type paramType(AbstractParam param) {
        return isPrimitive(param.getType()) ? param.getType() :
                toReference(param.getGenericType(), context.get());
    }

    public static RequestPackage<?> buildRequest(String msgId, OwnServiceUnit unit, Object[] args) {
        RequestPackage<?> requestPackage = new RequestPackage<>();
        requestPackage.setMsgId(msgId);
        requestPackage.setServiceId(unit.getKey());

        AbstractParam[] parameters = unit.getParameters();
        switch (parameters.length) {
            case 0:
                break;
            case 1:
                requestPackage.setContent(Common.cast(args[0]));
                break;
            default:
                Map<String, Object> toSend = new HashMap<>();
                for (int i = 0; i < parameters.length; i++) {
                    toSend.put(parameters[i].getName(), args[i]);
                }
                requestPackage.setContent(Common.cast(toSend));
                break;
        }
        return requestPackage;
    }


    static Object[] analysisParameters(String json, AbstractUnit<?> unit) {
//        if (content == null) return null;
        AbstractParam[] abstractParams = unit.getParameters();
        if (abstractParams.length == 0) return null;

//        Class<?> context = unit.getDeclaringModule().getInterfaceClass();
        context.set(unit.getDeclaringModule().getInterfaceClass());
        try {
            JSONSerializer serializer = JSONSerializerFactory.getInstance();

            List<Object> objects = new ArrayList<>();
            if (abstractParams.length == 1) {
                objects.add(json == null ? null :
                        serializer.parse(json, paramType(abstractParams[0])));
            } else {
                Map<String, String> map = serializer.parse(
                        json,
                        new GenericTypeHelper.GenericType<Map<String, String>>() {
                        }.getType());

                for (AbstractParam param : abstractParams) {
                    String value = map.get(param.getName());
                    objects.add(value == null ? null :
                            serializer.parse(value, paramType(param))
                    );
                }
            }
            return objects.toArray();
        } finally {
            context.remove();
        }
    }
}
