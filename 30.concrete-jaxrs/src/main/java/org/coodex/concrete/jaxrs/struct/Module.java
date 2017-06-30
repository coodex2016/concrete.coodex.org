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

import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.struct.AbstractModule;
import org.coodex.concrete.jaxrs.JaxRSHelper;
import org.coodex.concrete.jaxrs.Predicates;
import org.coodex.concrete.jaxrs.saas.RouteBy;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.coodex.concrete.jaxrs.JaxRSHelper.slash;

/**
 * Created by davidoff shen on 2016-11-30.
 */
public class Module extends AbstractModule<Unit> {

    private Unit[] units;
//    private List<Class<?>> inheritedChain = new ArrayList<Class<?>>();

    protected final boolean sameMethod(Method m1, Method m2) {
        if (m1 == null || m2 == null) return false;
        return m1.equals(m2) ||
                (m1.getName().equals(m2.getName()) && Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes()));

    }

//    private Class<?> findParent(Class<?> clz) {
//
//        for (Class<?> c : clz.getInterfaces()) {
//            if (ConcreteService.class.isAssignableFrom(clz)) {
//                return c.getAnnotation(MicroService.class) != null
//                        && c.getAnnotation(Abstract.class) != null
//                        ? c : findParent(c);
//            }
//        }
//        return null;
//    }

//    private void initInheritedChain() {
//        Stack<Class<?>> stack = new Stack<Class<?>>();
//        Class<?> parent = getInterfaceClass();
//        _while:
//        while (parent != null) {
//            stack.push(parent);
//            parent = findParent(parent);
//        }
//        while (!stack.isEmpty()) {
//            inheritedChain.add(stack.pop());
//        }
//    }

    public Module(Class<?> interfaceClass) {
        super(interfaceClass);

//        initInheritedChain();


        Set<Unit> units = new HashSet<Unit>();
        Map<String, Method> serviceAtoms = new HashMap<String, Method>();
        for (Method method : ConcreteHelper.getAllMethod(interfaceClass)) {

            if (method.getDeclaringClass() == Object.class) continue;

            Unit unit = new Unit(method, this);

            String serviceKey = Predicates.getHttpMethod(unit) + "$" + unit.getName();
            Method exists = serviceAtoms.get(serviceKey);
            if (sameMethod(method, exists)) {
                continue;
            }
            if (exists != null) {
                throw new RuntimeException(String.format("Method Conflict [%s]. m1:%s.%s, m2:%s.%s", serviceKey,
                        method.getDeclaringClass().getName(), method.getName(),
                        exists.getDeclaringClass().getName(), exists.getName()));
            }
            checkUnit(unit);
            units.add(unit);

            serviceAtoms.put(serviceKey, method);
        }
        this.units = units.toArray(new Unit[0]);
        Arrays.sort(this.units);
    }

    private void checkUnit(Unit unit) {
        String fullResource = getName() + unit.getName();
//        Matcher m = Pattern.compile("(\\{)[^{^}]*(\\})").matcher(fullResource);
        Matcher m = Pattern.compile("(\\{)[^{^}]{0,256}(\\})").matcher(fullResource);
        while (m.find()) {
            String param = m.group();
            param = param.substring(1, param.length() - 1).trim();
            if (!findPathParam(unit, param)) {
                throw new RuntimeException("path param [" + param + "] not found in "
                        + this.getInterfaceClass().getName() + "." + unit.getMethod().getName());
            }
        }

        RouteBy routeBy = unit.getAnnotation(RouteBy.class);//getDeclaredAnnotation(RouteBy.class);
        if (routeBy != null) {
            String routeByParam = routeBy.value();
            boolean found = false;
            for (Param param : unit.getParameters()) {
                if (param.isPathParam() && param.getName().equals(routeByParam)) {
                    found = true;
                    break;
                }
            }
            if (!found)
                throw new RuntimeException("routeBy [" + routeByParam + "] not found in "
                        + this.getInterfaceClass().getName() + "." + unit.getMethod().getName());
        }

    }

    private boolean findPathParam(Unit unit, String param) {
        for (Param p : unit.getParameters()) {
            if (p.isPathParam() && param.equals(p.getName())) {
                return true;
            }
        }
        return false;
    }

//    @Override
//    public List<Class<?>> getInheritedChain() {
//        return inheritedChain;
//    }

    @Override
    public String getName() {
//        StringBuilder builder = new StringBuilder();
//        for (Class<?> c : getInheritedChain()) {
//        for (int i = inheritedChain.size() - 1; i > 0; i--) {
//            Class<?> c = inheritedChain.get(i);
//            String serviceName = ConcreteHelper.getServiceName(c);
//            if (!Common.isBlank(serviceName)) {
//                if (!serviceName.startsWith("/")) {
//                    builder.append("/");
//                }
//                builder.append(serviceName);
//            }
//        }
//        if (builder.length() == 0) {
//        String serviceName = ConcreteHelper.getServiceName(c);
//        builder.append("/").append(ConcreteHelper.getServiceName());
//        }
        return slash(JaxRSHelper.camelCaseByPath(ConcreteHelper.getServiceName(getInterfaceClass()), true));
    }

    @Override
    public Unit[] getUnits() {
        return units;
    }

    @Override
    public int compareTo(AbstractModule o) {
        return getName().compareTo(o.getName());
    }
}
