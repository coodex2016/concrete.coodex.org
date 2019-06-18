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

package org.coodex.concrete.jaxrs;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.coodex.util.TypeHelper;

import java.lang.reflect.Method;
import java.util.*;

import static org.coodex.concrete.common.ConcreteHelper.isAbstract;


/**
 * Created by davidoff shen on 2016-11-30.
 */
public class JaxRSHelper {

    public static final String HEADER_ERROR_OCCURRED = "CONCRETE-ERROR-OCCURRED";
    public static final String KEY_CLIENT_PROVIDER = "X-CLIENT-PROVIDER";

    private final static Map<Class<?>, Module> MODULE_CACHE = new HashMap<>();

    /**
     * 0.2.4-SNAPSHOT以前版本，基础类型参数默认使用path传递，之后，默认使用body传递，除非明确定义了path变量
     *
     * @return 是否使用旧版本的默认传参行为
     */
    public static boolean used024Behavior() {
        String style = System.getProperty("jaxrs.style.before.024");
        if (style == null) {
            style = Config.get("jaxrs.style.before.024");
        }
        return Common.toBool(style, false);
    }

    public static boolean postPrimitive(Param param) {
        return !param.isPathParam();
    }

    @Deprecated
    public static String lowerFirstChar(String string) {
        return Common.lowerFirstChar(string);
    }

    @Deprecated
    public static String upperFirstChar(String string) {
        return Common.upperFirstChar(string);
    }

    @Deprecated
    public static String camelCase(String s) {
        return camelCase(s, false);
    }

    @Deprecated
    public static String camelCase(String s, String delimiters) {
        return camelCase(s, false, delimiters);
    }

    @Deprecated
    public static String camelCase(String s, boolean firstCharUpperCase) {
        return Common.camelCase(s, firstCharUpperCase);
    }

    @Deprecated
    public static String camelCase(String s, boolean firstCharUpperCase, String delimiters) {
        return Common.camelCase(s, firstCharUpperCase, delimiters);
    }

    public static String camelCaseByPath(String s) {
        return camelCaseByPath(s, false);
    }

    public static String camelCaseByPath(String s, boolean firstCharUpperCase) {
        StringTokenizer st = new StringTokenizer(s, "/");
        StringBuilder builder = new StringBuilder();
        while (st.hasMoreElements()) {
            String node = st.nextToken();
            if (node.length() == 0) continue;
            builder.append('/').append(Common.camelCase(node, firstCharUpperCase, ".-_ "));
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public static final synchronized Module getModule(final Class<?> type, String... packages) {
        Module module = MODULE_CACHE.get(type);
        if (module == null) {
            if (isAbstract(type)) { //抽象的服务定义，则找具体定义
                if (packages == null || packages.length == 0) {
                    packages = ConcreteHelper.getApiPackages();
                }
                final Set<Class<?>> serviceType = new HashSet<>();
                ReflectHelper.foreachClass((Class<?> serviceClass) -> {
                    if (serviceClass.isInterface() &&
                            type.isAssignableFrom(serviceClass) &&
                            !isAbstract(serviceClass)) {
                        serviceType.add(serviceClass);
                    }
                }, (String className) -> true, packages);

                switch (serviceType.size()) {
                    case 0:
                        throw new ConcreteException(ErrorCodes.MODULE_DEFINITION_NOT_FOUND, type);
                    case 1:
                        module = new Module(serviceType.iterator().next());
                        break;
                    default:
                        throw new ConcreteException(ErrorCodes.MODULE_DEFINITION_NON_UNIQUENESS, type);
                }
            } else {
                module = new Module(type);
            }
            MODULE_CACHE.put(type, module);
        }
        return module;
    }

//    @SuppressWarnings("unchecked")
//    private static final Set<Class<? >> getConcreteServiceClassFrom(
//            Class<?> clz, Class<?> type) {
//        Set<Class<?>> set = new HashSet<>();
//        if (type.isAssignableFrom(clz)) {
//            if (clz.isInterface() && clz.getAnnotation(Abstract.class) != null)
//                set.add(clz);
//            else {
//                set.addAll(getConcreteServiceClassFrom(clz.getSuperclass(), type));
//                for (Class<?> interfaceClz : clz.getInterfaces()) {
//                    set.addAll(getConcreteServiceClassFrom(interfaceClz, type));
//                }
//            }
//        }
//        return set;
//    }


    public static final Unit getUnitFromContext(DefinitionContext context/*, Object[] params*/) {
        Module module = getModule(context.getDeclaringClass());
        Method method = context.getDeclaringMethod();
        int count = method.getParameterTypes().length;// == null ? 0 : params.length;
        for (Unit unit : module.getUnits()) {
            if (method.getName().equals(unit.getMethod().getName())
                    && count == unit.getParameters().length) {
                return unit;
            }
        }
        return null;
    }

//    public static final Unit getUnitFromContext(DefinitionContext context/*, MethodInvocation invocation*/) {
//        return getUnitFromContext(context/*, invocation.getArguments()*/);
//    }


    public static Param getSubmitBody(Unit unit) {
        Param toSubmit = null;
        for (int i = 0; i < unit.getParameters().length; i++) {
            Param param = unit.getParameters()[i];
            if (!TypeHelper.isPrimitive(param.getType()) || JaxRSHelper.postPrimitive(param)) {
                toSubmit = param;
                break;
            }
        }
        return toSubmit;
    }

    public static String slash(String str) {
        if (Common.isBlank(str)) return "";

        return str.startsWith("/") ? str : ("/" + str);
    }


}
