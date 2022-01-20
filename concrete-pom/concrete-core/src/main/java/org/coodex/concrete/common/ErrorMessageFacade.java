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

package org.coodex.concrete.common;

import org.coodex.concrete.api.ErrorCode;
import org.coodex.util.Common;
import org.coodex.util.I18N;
import org.coodex.util.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


/**
 * Created by davidoff shen on 2016-09-04.
 */
public class ErrorMessageFacade /*extends AbstractMessageFacade */ {

    final static ThreadLocal<Locale> LOCALE_CONTEXT = new ThreadLocal<>();
    /*
     * 没有MessageFormatter Provider的时候，以此Formatter输出，确保formatter不为空
     * 2016-11-01，修改默认使用JavaTextFormatMessageFormatter
     */
    private final static Logger log = LoggerFactory.getLogger(ErrorMessageFacade.class);
    private final static Set<Class<?>> REGISTERED = new HashSet<>();

    private final static Map<Integer, Field> errorCodes = new HashMap<>();


    private ErrorMessageFacade() {
    }

    private static boolean registerClass(Class<?> clz) {
        if (clz == null || clz.getAnnotation(ErrorCode.class) == null) {
            return false;
        }

        if (!REGISTERED.contains(clz)) {
            synchronized (errorCodes) {
                if (!REGISTERED.contains(clz)) {
                    for (Field f : clz.getDeclaredFields()) {

                        if (!(int.class.equals(f.getType())
                                && Modifier.isStatic(f.getModifiers())
                                && Modifier.isFinal(f.getModifiers())
                                && Modifier.isPublic(f.getModifiers()))) {
                            continue;
                        }

                        f.setAccessible(true);
                        try {
                            int code = f.getInt(null);
                            if (errorCodes.containsKey(code)) {
                                Field field = errorCodes.get(code);
                                if (!field.equals(f)) {
                                    log.warn("errorCode duplicate {}.{} and {}.{}",
                                            field.getDeclaringClass().getCanonicalName(), field.getName(),
                                            f.getDeclaringClass().getCanonicalName(), f.getName());
                                }
                            } else {
                                errorCodes.put(code, f);
                            }
                        } catch (IllegalAccessException e) {
                            log.warn("Cannot bind errorCode: {}.{}",
                                    f.getDeclaringClass().getCanonicalName(), f.getName());
                        }
                    }
                    REGISTERED.add(clz);
                    log.info("ErrorCode registered: {}", clz.getName());
                }
            }
        }
        return true;
    }

    /**
     * @param clz clz
     * @return 是否被ErrorMessage注册
     */
    public static boolean register(Class<?> clz) {
        return registerClass(clz);
    }

    public static Set<Integer> allRegisteredErrorCodes() {
        return errorCodes.keySet();
    }

    public static String getMessageTemplate(int code) {
        return getMessageOrPattern(false, code);
    }

    public static String getMessage(int code, Object... objects) {
        return getMessageOrPattern(true, code, objects);
    }

    private static String getNamespace(Field field) {
        if (field == null) {
            return ErrorCode.DEFAULT_NAMESPACE;
        }
        ErrorCode errorCode = field.getDeclaringClass().getAnnotation(ErrorCode.class);
        return errorCode == null || Common.isBlank(errorCode.value()) ? ErrorCode.DEFAULT_NAMESPACE : errorCode.value();
    }

    private static String getTemplateStr(Field field, String namespace, int code) {
        if (field == null) {
            return namespace + '.' + code;
        }
        ErrorCode.Template template = field.getAnnotation(ErrorCode.Template.class);
        if (template != null && !Common.isBlank(template.value())) {
            return template.value();
        }
        ErrorCode.Key key = field.getAnnotation(ErrorCode.Key.class);
        return namespace + '.' +
                (key == null || Common.isBlank(key.value()) ? String.valueOf(code) : key.value());
    }

    private static String getMessageOrPattern(boolean format, int code, Object... objects) {
        Field f = errorCodes.get(code);
        String template = I18N.translate(getTemplateStr(f, getNamespace(f), code), LOCALE_CONTEXT.get());
        return format ? Renderer.render(template, objects) : template;

//        ErrorMsg formatterValue = null;
//        if (f == null) {
//            if (getServiceContext() instanceof ServerSideContext) {
//                log.debug("errorCode [{}] has not register.", code);
//            }
//        } else {
//            ErrorMsg errorMsg = f.getAnnotation(ErrorMsg.class);
//            formatterValue = errorMsg == null ? f.getDeclaringClass().getAnnotation(ErrorMsg.class) : errorMsg;
//        }
//
//        AbstractErrorCodes.Namespace namespace =
//                f == null ? null :
//                        f.getDeclaringClass().getAnnotation(AbstractErrorCodes.Namespace.class);
//
//        String errorMessageNamespace = namespace == null ? "message" : namespace.value();
//        errorMessageNamespace = Common.isBlank(errorMessageNamespace) && f != null ? f.getDeclaringClass().getName() : errorMessageNamespace;
//
//        String msgTemp = (formatterValue == null || Common.isBlank(formatterValue.value().trim())) ?
//                errorMessageNamespace + "." + code :
//                formatterValue.value();
//
//        String pattern = I18N.translate(msgTemp);
//        pattern = pattern != null ? pattern : msgTemp;
//        return format ? Renderer.render(pattern, objects) : pattern;
    }


    public static List<ErrorDefinition> getAllErrorInfo() {
        final List<ErrorDefinition> errorDefinitions = new ArrayList<>();
        for (Integer i : allRegisteredErrorCodes()) {
            errorDefinitions.add(new ErrorDefinition(i));
        }
        Collections.sort(errorDefinitions);
        return errorDefinitions;
    }

}
