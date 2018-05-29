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

import org.coodex.concrete.api.Abstract;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ConcreteHelper;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.ClassNameFilter;
import org.coodex.util.Common;
import org.coodex.util.ReflectHelper;
import org.coodex.util.TypeHelper;

import java.lang.reflect.Method;
import java.util.*;


/**
 * Created by davidoff shen on 2016-11-30.
 */
public class JaxRSHelper {

    //    public static final String KEY_ERROR_CODE = "code";
//    public static final String KEY_ERROR_MESSAGE = "msg";
    public static final String HEADER_ERROR_OCCURRED = "CONCRETE-ERROR-OCCURRED";
    public static final String KEY_CLIENT_PROVIDER = "X-CLIENT-PROVIDER";

//    public static final String JAXRS_MODEL = "jaxrs_model";

//    private static final Class[] PRIMITIVE_CLASSES = new Class[]{
//            String.class,
//            Boolean.class,
//            Character.class,
//            Byte.class,
//            Short.class,
//            Integer.class,
//            Long.class,
//            Float.class,
//            Double.class,
//            Void.class,
//            boolean.class,
//            char.class,
//            byte.class,
//            short.class,
//            int.class,
//            long.class,
//            float.class,
//            double.class,
//            void.class,
//    };
    private final static Map<Class<?>, Module> MODULE_CACHE = new HashMap<Class<?>, Module>();

    public static boolean isPrimitive(Class c) {
//        return Common.inArray(c, PRIMITIVE_CLASSES);
        return TypeHelper.isPrimitive(c);
    }


//    private static final int TO_LOWER = 'a' - 'A';

    public static boolean isBigString(Param param) {
        return String.class.isAssignableFrom(param.getType())
                && param.getDeclaredAnnotation(BigString.class) != null;
    }

    @Deprecated
    public static String lowerFirstChar(String string) {
        return Common.lowerFirstChar(string);
//        if (string == null) return string;
//        char[] charSeq = string.toCharArray();
//        if (charSeq.length > 1 && charSeq[0] >= 'A' && charSeq[0] <= 'Z') {
//            charSeq[0] = (char) (charSeq[0] + TO_LOWER);
//            return new String(charSeq);
//        }
//        return string;
    }


//    private final static String DEFAULT_DELIM = ".-_ /\\";

    @Deprecated
    public static String upperFirstChar(String string) {
        return Common.upperFirstChar(string);
//        if (string == null) return string;
//        char[] charSeq = string.toCharArray();
//        if (charSeq.length > 1 && charSeq[0] >= 'a' && charSeq[0] <= 'z') {
//            charSeq[0] = (char) (charSeq[0] - TO_LOWER);
//            return new String(charSeq);
//        }
//        return string;
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
//        return camelCase(s, firstCharUpperCase, DEFAULT_DELIM);
    }

    @Deprecated
    public static String camelCase(String s, boolean firstCharUpperCase, String delimiters) {
        return Common.camelCase(s, firstCharUpperCase, delimiters);
//        StringTokenizer st = new StringTokenizer(s, delimiters);
//        StringBuilder builder = new StringBuilder();
//        while (st.hasMoreElements()) {
//            String node = st.nextToken();
//            if (node.length() == 0) continue;
//            builder.append(upperFirstChar(node));
//        }
//        return firstCharUpperCase ?
//                upperFirstChar(builder.toString()) :
//                lowerFirstChar(builder.toString());
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
    public static final synchronized Module getModule(final Class<? extends ConcreteService> type, String... packages) {
        Module module = MODULE_CACHE.get(type);
        if (module == null) {
            if (type.getAnnotation(Abstract.class) != null) { //抽象的服务定义，则找具体定义
                if (packages == null || packages.length == 0) {
                    packages = ConcreteHelper.getApiPackages();
                }
                final Set<Class<? extends ConcreteService>> serviceType = new HashSet<Class<? extends ConcreteService>>();
                ReflectHelper.foreachClass(new ReflectHelper.Processor() {
                    @Override
                    public void process(Class<?> serviceClass) {
                        if (serviceClass.isInterface() &&
                                type.isAssignableFrom(serviceClass) &&
                                serviceClass.getAnnotation(Abstract.class) == null) {
                            serviceType.add((Class<? extends ConcreteService>) serviceClass);
                        }
                    }
                }, new ClassNameFilter() {
                    @Override
                    public boolean accept(String className) {
                        return true;
                    }
                }, packages);

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

    @SuppressWarnings("unchecked")
    private static final Set<Class<? extends ConcreteService>> getConcreteServiceClassFrom(
            Class<?> clz, Class<? extends ConcreteService> type) {
        Set<Class<? extends ConcreteService>> set = new HashSet<Class<? extends ConcreteService>>();
        if (type.isAssignableFrom(clz)) {
            if (clz.isInterface() && clz.getAnnotation(Abstract.class) != null)
                set.add((Class<? extends ConcreteService>) clz);
            else {
                set.addAll(getConcreteServiceClassFrom(clz.getSuperclass(), type));
                for (Class<?> interfaceClz : clz.getInterfaces()) {
                    set.addAll(getConcreteServiceClassFrom(interfaceClz, type));
                }
            }
        }
        return set;
    }


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
            if (!JaxRSHelper.isPrimitive(param.getType()) || JaxRSHelper.isBigString(param)) {
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
