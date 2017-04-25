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

package org.coodex.concrete.jaxrs;

import org.aopalliance.intercept.MethodInvocation;
import org.coodex.concrete.api.ConcreteService;
import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.jaxrs.struct.Module;
import org.coodex.concrete.jaxrs.struct.Param;
import org.coodex.concrete.jaxrs.struct.Unit;
import org.coodex.util.Common;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * Created by davidoff shen on 2016-11-30.
 */
public class JaxRSHelper {

    //    public static final String KEY_ERROR_CODE = "code";
//    public static final String KEY_ERROR_MESSAGE = "msg";
    public static final String HEADER_ERROR_OCCURRED = "CONCRETE-ERROR-OCCURRED";

    private static final Class[] PRIMITIVE_CLASSESS = new Class[]{
            String.class,
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Void.class,
            boolean.class,
            char.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            void.class,
    };

    public static boolean isPrimitive(Class c) {
        return Common.inArray(c, PRIMITIVE_CLASSESS);
    }

    public static boolean isBigString(Param param) {
        return String.class.isAssignableFrom(param.getType())
                && param.getDeclaredAnnotation(BigString.class) != null;
    }


    private static final int TO_LOWER = 'a' - 'A';

    public static String lowerFirstChar(String string) {
        if (string == null) return string;
        char[] charSeq = string.toCharArray();
        if (charSeq.length > 1 && charSeq[0] >= 'A' && charSeq[0] <= 'Z') {
            charSeq[0] = (char) (charSeq[0] + TO_LOWER);
            return new String(charSeq);
        }
        return string;
    }

    public static String upperFirstChar(String string) {
        if (string == null) return string;
        char[] charSeq = string.toCharArray();
        if (charSeq.length > 1 && charSeq[0] >= 'a' && charSeq[0] <= 'z') {
            charSeq[0] = (char) (charSeq[0] - TO_LOWER);
            return new String(charSeq);
        }
        return string;
    }


    private final static String DEFAULT_DELIM = ".-_ /\\";

    public static String camelCase(String s) {
        return camelCase(s, false);
    }

    public static String camelCase(String s, String delimiters) {
        return camelCase(s, false, delimiters);
    }

    public static String camelCase(String s, boolean firstCharUpperCase) {
        return camelCase(s, firstCharUpperCase, DEFAULT_DELIM);
    }

    public static String camelCase(String s, boolean firstCharUpperCase, String delimiters) {
        StringTokenizer st = new StringTokenizer(s, delimiters);
        StringBuilder builder = new StringBuilder();
        while (st.hasMoreElements()) {
            String node = st.nextToken();
            if (node.length() == 0) continue;
            builder.append(upperFirstChar(node));
        }
        return firstCharUpperCase ?
                upperFirstChar(builder.toString()) :
                lowerFirstChar(builder.toString());
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
            builder.append('/').append(camelCase(node, firstCharUpperCase, ".-_ "));
        }
        return builder.toString();
    }


    private final static Map<Class<?>, Module> MODULE_CACHE = new HashMap<Class<?>, Module>();

    public static final synchronized Module getModule(Class<? extends ConcreteService> type) {
        Module module = MODULE_CACHE.get(type);
        if (module == null) {
            module = new Module(type);
            MODULE_CACHE.put(type, module);
        }
        return module;
    }


    public static final Unit getUnitFromContext(DefinitionContext context, Object[] params) {
        Module module = getModule(context.getDeclaringClass());
        Method method = context.getDeclaringMethod();
        int count = params == null ? 0 : params.length;
        for (Unit unit : module.getUnits()) {
            if (method.getName().equals(unit.getMethod().getName())
                    && count == unit.getParameters().length) {
                return unit;
            }
        }
        return null;
    }

    public static final Unit getUnitFromContext(DefinitionContext context, MethodInvocation invocation) {
        return getUnitFromContext(context, invocation.getArguments());
    }


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


}
