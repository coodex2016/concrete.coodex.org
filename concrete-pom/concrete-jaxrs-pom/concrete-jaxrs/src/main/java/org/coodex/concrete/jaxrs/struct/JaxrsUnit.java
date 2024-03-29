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

package org.coodex.concrete.jaxrs.struct;

import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.modules.AbstractUnit;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.util.Common;

import javax.ws.rs.HttpMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.coodex.concrete.common.ConcreteHelper.isPrimitive;
import static org.coodex.concrete.jaxrs.JaxRSHelper.slash;
import static org.coodex.concrete.jaxrs.Predicates.getHttpMethod;
import static org.coodex.concrete.jaxrs.Predicates.removePredicate;

//import org.coodex.concrete.jaxrs.PathParam;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class JaxrsUnit extends AbstractUnit<JaxrsParam/*, JaxrsModule*/> {


    private final String name;
    private List<JaxrsParam> pojo;
    private String declaredName = null;

    public JaxrsUnit(Method method, JaxrsModule module) {
        super(method, module);
        name = getNameOnInit();
        validation();
    }

    private synchronized List<JaxrsParam> _getPojo() {
        if (pojo == null) {
            pojo = new ArrayList<>();
        }
        return pojo;
    }

    private void validation() {
        int pojoCount = getPojoCount();
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
            throw new RuntimeException("Object parameter count limited " + pojoLimited + " in HttpMethod." +
                    httpMethod + ", " + pojoCount + " used in " +
                    getMethod().toGenericString());
        }

//        if(pojoCount >= 2)
//            throw new IllegalArgumentException("too many POJO parameters.")
//        String httpMethod =
    }

//    private List<Class<?>> buildLink() {
//        Stack<Class<?>> stack = new Stack<Class<?>>();
//        Class<?> current = getDeclaringModule().getInterfaceClass();
//        Class<?> root = getMethod().getDeclaringClass();
//        while (!root.equals(current)) {
//
//        }
//        return stack;
//    }


    private String getDeclaredName() {
        if (declaredName == null) {
            declaredName = getUnitDeclaredName();
        }
        return declaredName;
    }

    private String getNameOnInit() {
        return getDeclaredName();
    }

    private String getUnitDeclaredName() {
        List<Class<?>> inheritedChain = ConcreteHelper.inheritedChain(
                getMethod().getDeclaringClass(), getDeclaringModule().getInterfaceClass());
        if (inheritedChain == null) {
            inheritedChain = Collections.emptyList();
        }

        StringBuilder buffer = new StringBuilder();
        for (Class<?> c : inheritedChain) {
            String serviceName = ConcreteHelper.getServiceName(c);
            if (!Common.isBlank(serviceName)) {
                buffer.append(slash(Common.camelCase(serviceName, true)));
            }
        }

        ConcreteService concreteService = getMethod().getAnnotation(ConcreteService.class);
        buffer.append(slash(concreteService == null ? removePredicate(getMethod().getName()) : concreteService.value()));

        return buffer.toString();
    }


    private String getPathParam(JaxrsParam parameter) {
//        PathParam pathParam = parameter.getDeclaredAnnotation(PathParam.class);
//        if (pathParam != null) return pathParam.value();
//        PathParam pathParam1 = parameter.getDeclaredAnnotation(PathParam.class);
//        if (pathParam1 != null) return pathParam1.value();
        if (JaxRSHelper.postPrimitive(parameter)) {
            return null;
        }

        Class<?> clz = parameter.getType();
//        boolean isBigString = parameter.getDeclaredAnnotation(BigString.class) != null;
//        //大字符串
//        if (clz == String.class && isBigString) return null;

        return isPrimitive(clz) ?
//                JaxRSHelper.used024Behavior() ?
                parameter.getName() :
//                        null :
                null;
//
//        return pathParam1 == null ?
//                (isPrimitive(parameter.getType())
//                        && (parameter.getType() == String.class
//                        && parameter.getDeclaredAnnotation(BigString.class) == null)
//                        ? parameter.getName() : null) :
//                pathParam1.value();
    }

//    private String post024Style(Param param) {
//
//    }


    @Override
    public String getName() {
        return name;
    }

    private void addToBody(JaxrsParam param) {
        param.setPathParam(false);
        _getPojo().add(param);
    }

    private boolean isBodyPrimitive(JaxrsParam param) {
//        if (used024Behavior()) {
//            return param.getDeclaredAnnotation(Body.class) != null;
//        } else {
        //noinspection IndexOfReplaceableByContains
        return getDeclaredName().indexOf(String.format("{%s}", param.getName())) < 0;
//        }
    }

    @Override
    protected void afterInit() {
        if (getPojoCount() > 0) {
            if (getPojoCount() == 1 && !isPrimitive(getPojo()[0].getType()) && !getPojo()[0].getType().isEnum()) {
                return;
            }
            for (JaxrsParam param : getPojo()) {
                param.setAssembled(true);
            }
        }
    }

    @Override
    protected JaxrsParam buildParam(Method method, int index) {
//        String toTest = getDeclaredName();
//        int paramCount = method.getParameterTypes().length;
//        parameters = new Param[paramCount];
//        for (int i = 0; i < paramCount; i++) {
        JaxrsParam param = new JaxrsParam(method, index);

//            parameters[i] = new Param(method, i);
//            Param param = parameters[i];
        if (!isPrimitive(param.getType()) ||
                /*(param.getType() == String.class
                        && param.getDeclaredAnnotation(BigString.class) != null) */
                isBodyPrimitive(param)) {
//            _getPojo().add(param);
//            param.setPathParam(false);
            addToBody(param);
        }
//        }
        return param;
    }

    @Override
    public String getInvokeType() {
        return getHttpMethod(this);
    }

//    @Override
//    public Param[] getParameters() {
//        return parameters;
//    }

    @Override
    protected JaxrsParam[] toArrays(List<JaxrsParam> params) {
        return params.toArray(new JaxrsParam[0]);
    }

    @Override
    protected DefinitionContext toContext() {
        return ConcreteHelper.getContext(getMethod(), getDeclaringModule().getInterfaceClass());
    }

    @Override
    public int compareTo(AbstractUnit<JaxrsParam> o) {
        // todo 优化正则表达式
        //noinspection RegExpRedundantEscape
        int v = getName().replaceAll("(\\{)[^{^}]{0,256}(\\})", "")
                .compareTo(o.getName().replaceAll("(\\{)[^{^}]{0,256}(\\})", ""));
        if (v == 0) {
            v = getName().compareTo(o.getName());
        }
        return v == 0 ? getInvokeType().compareTo(o.getInvokeType()) : v;
    }

    public int getPojoCount() {
        return _getPojo().size();
    }

    public JaxrsParam[] getPojo() {
        return _getPojo().toArray(new JaxrsParam[0]);
    }

    public boolean isAssembledPojo() {
        return pojo != null && pojo.stream().anyMatch(JaxrsParam::isAssembled);
    }
}
