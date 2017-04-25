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

package org.coodex.concrete.jaxrs.struct;

import org.coodex.concrete.common.struct.AbstractUnit;
import org.coodex.concrete.jaxrs.BigString;
import org.coodex.concrete.jaxrs.PathParam;

import javax.ws.rs.HttpMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.coodex.concrete.jaxrs.JaxRSHelper.isPrimitive;
import static org.coodex.concrete.jaxrs.Predicates.getHttpMethod;
import static org.coodex.concrete.jaxrs.Predicates.getRESTFulPath;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class Unit extends AbstractUnit<Param, Module> {


    private Param[] parameters;
    private List<Param> pojo = new ArrayList<Param>();
    private String name;

    public Unit(Method method, Module module) {
        super(method, module);
        int paramCount = method.getParameterTypes().length;
        parameters = new Param[paramCount];
        for (int i = 0; i < paramCount; i++) {
            parameters[i] = new Param(method, i);
            Param param = parameters[i];
            if (!isPrimitive(param.getType()) ||
                    (param.getType() == String.class
                            && param.getDeclaredAnnotation(BigString.class) != null)) {

                pojo.add(param);
                param.setPathParam(false);
            }
        }
        name = getNameOnInit();
        validation();
    }

    private void validation() {
        int pojoCount = pojo.size();
//        for (Param param : parameters) {
//            if (!isPrimitive(param.getType()) ||
//                    (param.getType() == String.class && param.getDeclaredAnnotation(BigString.class) != null))
//                pojoCount++;
//        }

        String httpMethod = getInvokeType();
        int pojoLimited = 0;
        if (httpMethod.equalsIgnoreCase(HttpMethod.POST)
                || httpMethod.equalsIgnoreCase(HttpMethod.PUT)
                || httpMethod.equalsIgnoreCase(HttpMethod.DELETE)) {
//            pojoLimited = 1;
            pojoLimited = Integer.MAX_VALUE;
        }
//
//        switch (httpMethod) {
//            case HttpMethod.POST:
//            case HttpMethod.PUT:
//                pojoLimited = 1;
//                break;
//            case HttpMethod.DELETE: // ?是否允许pojo
//            case HttpMethod.GET:
//                pojoLimited = 0;
//        }

        if (pojoCount > pojoLimited) {
            StringBuilder builder = new StringBuilder();
            builder.append("Object parameter count limited ").append(pojoLimited).append(" in HttpMethod.")
                    .append(httpMethod).append(", ").append(pojoCount).append(" used in ")
                    .append(getMethod().toGenericString());
            throw new RuntimeException(builder.toString());
        }

//        if(pojoCount >= 2)
//            throw new IllegalArgumentException("too many POJO parameters.")
//        String httpMethod =
    }

    private String getNameOnInit() {
        String methodName = getRESTFulPath(getMethod());

        StringBuffer buffer = new StringBuffer();

        if (methodName != null)
            buffer.append("/").append(methodName);

        String toTest = "/" + getDeclaringModule().getName()
                + (methodName == null ? "" : ("/" + methodName));

        for (Param parameter : getParameters()) {
            String pathParamValue = getPathParam(parameter);
            if (pathParamValue != null) {
                String restfulNode = "{" + pathParamValue + "}";

                if (toTest == null || toTest.indexOf(restfulNode) < 0) {
                    buffer.append("/").append(restfulNode);
                }
            }
        }
        return buffer.toString();
    }


    protected String getPathParam(Param parameter) {
        PathParam pathParam = parameter.getDeclaredAnnotation(PathParam.class);
        if (pathParam != null) return pathParam.value();
        javax.ws.rs.PathParam pathParam1 = parameter.getDeclaredAnnotation(javax.ws.rs.PathParam.class);
        if (pathParam1 != null) return pathParam1.value();
        Class<?> clz = parameter.getType();
        boolean isBigString = parameter.getDeclaredAnnotation(BigString.class) != null;
        //大字符串
        if (clz == String.class && isBigString) return null;

        return isPrimitive(clz) ? parameter.getName() : null;
//
//        return pathParam1 == null ?
//                (isPrimitive(parameter.getType())
//                        && (parameter.getType() == String.class
//                        && parameter.getDeclaredAnnotation(BigString.class) == null)
//                        ? parameter.getName() : null) :
//                pathParam1.value();
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getInvokeType() {
        return getHttpMethod(this);
    }

    @Override
    public Param[] getParameters() {
        return parameters;
    }

    @Override
    public int compareTo(AbstractUnit o) {
        int v = getName().replaceAll("\\{[^{}]*}", "")
                .compareTo(o.getName().replaceAll("\\{[^{}]*}", ""));
        if (v == 0)
            v = getName().compareTo(o.getName());
        return v == 0 ? getInvokeType().compareTo(o.getInvokeType()) : v;
    }

    public int getPojoCount() {
        return pojo.size();
    }

    public Param[] getPojo() {
        return pojo.toArray(new Param[0]);
    }
}
