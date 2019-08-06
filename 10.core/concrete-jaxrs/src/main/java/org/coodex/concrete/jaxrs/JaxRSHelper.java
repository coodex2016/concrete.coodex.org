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

import org.coodex.concrete.common.DefinitionContext;
import org.coodex.concrete.jaxrs.struct.JaxrsModule;
import org.coodex.concrete.jaxrs.struct.JaxrsParam;
import org.coodex.concrete.jaxrs.struct.JaxrsUnit;
import org.coodex.config.Config;
import org.coodex.util.Common;
import org.coodex.util.TypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.StringTokenizer;


/**
 * Created by davidoff shen on 2016-11-30.
 */
public class JaxRSHelper {

    public static final String HEADER_ERROR_OCCURRED = "CONCRETE-ERROR-OCCURRED";
    public static final String KEY_CLIENT_PROVIDER = "X-CLIENT-PROVIDER";
    private final static Logger log = LoggerFactory.getLogger(JaxRSHelper.class);


//    private final static Map<Class<?>, Module> MODULE_CACHE = new HashMap<>();


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

    public static boolean postPrimitive(JaxrsParam param) {
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
//    public static final synchronized Module getModule(final Class<?> type, String... packages) {
//        Module module = MODULE_CACHE.get(type);
//        if (module == null) {
//            if (isAbstract(type)) { //抽象的服务定义，则找具体定义
//                if (packages == null || packages.length == 0) {
//                    packages = ConcreteHelper.getApiPackages();
//                }
//                final Set<Class<?>> serviceType = new HashSet<>();
//                ReflectHelper.foreachClass((Class<?> serviceClass) -> {
//                    if (serviceClass.isInterface() &&
//                            type.isAssignableFrom(serviceClass) &&
//                            !isAbstract(serviceClass)) {
//                        serviceType.add(serviceClass);
//                    }
//                }, (String className) -> true, packages);
//
//                switch (serviceType.size()) {
//                    case 0:
//                        throw new ConcreteException(ErrorCodes.MODULE_DEFINITION_NOT_FOUND, type);
//                    case 1:
//                        module = new Module(serviceType.iterator().next());
//                        break;
//                    default:
//                        throw new ConcreteException(ErrorCodes.MODULE_DEFINITION_NON_UNIQUENESS, type);
//                }
//            } else {
//                module = new Module(type);
//            }
//            MODULE_CACHE.put(type, module);
//        }
//        return module;
//    }

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

    private static final JaxRSModuleMaker JAX_RS_MODULE_MAKER = new JaxRSModuleMaker();
    public static final JaxrsUnit getUnitFrom(Class clz, Method method) {
        JaxrsModule module = JAX_RS_MODULE_MAKER.make(clz);//getModule(clz);
        int count = method.getParameterTypes().length;// == null ? 0 : params.length;
        for (JaxrsUnit unit : module.getUnits()) {
            if (method.getName().equals(unit.getMethod().getName())
                    && count == unit.getParameters().length) {
                return unit;
            }
        }
        return null;
    }

    public static final JaxrsUnit getUnitFromContext(DefinitionContext context/*, Object[] params*/) {
//        Module module = getModule(context.getDeclaringClass());
//        Method method = context.getDeclaringMethod();
//        int count = method.getParameterTypes().length;// == null ? 0 : params.length;
//        for (Unit unit : module.getUnits()) {
//            if (method.getName().equals(unit.getMethod().getName())
//                    && count == unit.getParameters().length) {
//                return unit;
//            }
//        }
//        return null;
        return getUnitFrom(context.getDeclaringClass(), context.getDeclaringMethod());
    }

//    public static final Unit getUnitFromContext(DefinitionContext context/*, MethodInvocation invocation*/) {
//        return getUnitFromContext(context/*, invocation.getArguments()*/);
//    }


    public static JaxrsParam getSubmitBody(JaxrsUnit unit) {
        JaxrsParam toSubmit = null;
        for (int i = 0; i < unit.getParameters().length; i++) {
            JaxrsParam param = unit.getParameters()[i];
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

//    private static String LOGGING_FEATURE_CLASSNAME = "org.glassfish.jersey.logging.LoggingFeature";
//
//    public static Object getLoggingComponent(Logger logger) {
//        try {
//            Class loggingFeatureClass = Class.forName(LOGGING_FEATURE_CLASSNAME);
//            Class verbosity = Class.forName(LOGGING_FEATURE_CLASSNAME + "$Verbosity");
//            Object verbosity_payload_text = verbosity.getEnumConstants()[1];
//            //noinspection unchecked
//            Constructor constructor = loggingFeatureClass.getConstructor(
//                    java.util.logging.Logger.class,
//                    Level.class,
//                    verbosity,
//                    Integer.class);
//            if (constructor != null) {
//                return constructor.newInstance(
//                        java.util.logging.Logger.getLogger(logger.getName()),
//                        getLevel(logger),
//                        verbosity_payload_text,
//                        4096);
//            }
//        } catch (Throwable th) {
//            log.warn("get logging component failed: {}", th.getLocalizedMessage());
//        }
//        return null;
//    }
//
//    private static Level getLevel(Logger logger) {
//        if (logger.isTraceEnabled()) return Level.FINEST;
//        if (logger.isDebugEnabled()) return Level.FINE;
//        if (logger.isInfoEnabled()) return Level.CONFIG;
//        if (logger.isWarnEnabled()) return Level.WARNING;
//        if (logger.isErrorEnabled()) return Level.SEVERE;
//        return Level.OFF;
//    }
}
