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
import java.util.stream.Collectors;


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
        return Optional.ofNullable(field)
                .map(f -> f.getDeclaringClass().getAnnotation(ErrorCode.class))
                .map(ec -> Common.isBlank(ec.value()) ? null : ec.value())
                .orElse(ErrorCode.DEFAULT_NAMESPACE);
//        if (field == null) {
//            return ErrorCode.DEFAULT_NAMESPACE;
//        }
//        ErrorCode errorCode = field.getDeclaringClass().getAnnotation(ErrorCode.class);
//        return errorCode == null || Common.isBlank(errorCode.value()) ? ErrorCode.DEFAULT_NAMESPACE : errorCode.value();
    }

    private static String getKey(Field field, String namespace, int code) {
//        if (field == null) {
//            return namespace + '.' + code;
//        }
//
//        ErrorCode.Key key = field.getAnnotation(ErrorCode.Key.class);
//        if (key != null && !Common.isBlank(key.value())) {
//            return namespace + "." + key.value();
//        }
//
//        ErrorCode.Template template = field.getAnnotation(ErrorCode.Template.class);
//        if (template != null && !Common.isBlank(template.value())) {
//            String s = template.value();
//            if (s.startsWith("{") && s.endsWith("}")) {
//                return s.substring(1, s.length() - 1);
//            }
//        }
//
//        return namespace + "." + code;
        String keyFromTemplate = getKeyFromTemplate(getTemplateStr(field));
        if (keyFromTemplate != null) return keyFromTemplate;

        return namespace + "." + Optional.ofNullable(field)
                .map(f -> f.getAnnotation(ErrorCode.Key.class))
                .map(key -> Common.isBlank(key.value()) ? null : key.value())
                .orElse(String.valueOf(code));

//        ErrorCode.Key key = field.getAnnotation(ErrorCode.Key.class);
//        return namespace + '.' +
//                (key == null || Common.isBlank(key.value()) ? String.valueOf(code) : key.value());
    }

    private static String getKeyFromTemplate(String template) {
        return new KeyStat(template).key();
    }

    private static String getTemplateStr(Field field) {
        return Optional.ofNullable(field)
                .map(f -> f.getAnnotation(ErrorCode.Template.class))
                .map(t -> Common.isBlank(t.value()) ? null : t.value())
                .orElse(null);
    }


    public static String getKey(int code) {
        Field f = errorCodes.get(code);
        return getKey(f, getNamespace(f), code);
    }

    public static String getTemplate(int code) {
        Field f = errorCodes.get(code);
        String template = getTemplateStr(f);
        if (template == null) {
            return I18N.translate(getKey(code), LOCALE_CONTEXT.get());
        } else {
            return Optional.ofNullable(getKeyFromTemplate(template))
                    .map(key -> I18N.translate(key, LOCALE_CONTEXT.get()))
                    .orElse(template);
        }

    }

    private static String getMessageOrPattern(boolean format, int code, Object... objects) {
//        Field f = errorCodes.get(code);
//        String template = getTemplateStr(f);
//        if (template == null) {
//            template = I18N.translate(getKey(code), LOCALE_CONTEXT.get());
//        }
        String template = getTemplate(code);
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
//        final List<ErrorDefinition> errorDefinitions = new ArrayList<>();
////        for (Integer i : allRegisteredErrorCodes()) {
////            errorDefinitions.add(new ErrorDefinition(i));
////        }
//        Collections.sort(errorDefinitions);
//        return errorDefinitions;
        return errorCodes.values().stream().map(ErrorDefinition::new)
                .sorted()
                .collect(Collectors.toList());
    }

//    public static void main(String[] args) {
//        register(ECTest.class);
//        getAllErrorInfo().forEach(System.out::println);
//    }
}

class KeyStat {
    private boolean dot = false;
    private boolean hyphen = false;
    private int dotFound = 0;
    private StringBuilder builder = null;

    KeyStat(String template) {
        if (template == null) return;
        char[] chars = template.trim().toCharArray();
        if (chars.length < 5) return;
        if (chars[0] == '{' && chars[chars.length - 1] == '}') {
            if (chars[1] >= '0' && chars[1] <= '9') return;

            for (int i = 1; i < chars.length - 1; i++) {
                if (next(chars[i])) {
                    builder = (builder == null ? new StringBuilder() : builder).append(chars[i]);
                } else {
                    builder = null;
                    break;
                }
            }
            if (dotFound == 0) builder = null;
        }
    }

    public String key() {
        return builder == null ? null : builder.toString();
    }

    private Type typeOf(char c) {
        if (c == '.') return Type.DOT;
        if (c == '-' || c == '_') return Type.HYPHEN;
        if (c >= '0' && c <= '9') return Type.ASCII_CHAR;
        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) return Type.ASCII_CHAR;
        return Type.OTHER;
    }

    private boolean next(char c) {
        switch (typeOf(c)) {
            case ASCII_CHAR:
                dot = true;
                hyphen = true;
                return true;
            case DOT:
                if (!dot) return false;
                dotFound++;
                dot = false;
                hyphen = false;
                return true;
            case HYPHEN:
                if (!hyphen) return false;
                dot = false;
                return true;
            default:
                return false;
        }
    }


    enum Type {
        DOT, ASCII_CHAR, HYPHEN, OTHER
    }
}


//@ErrorCode("ectest")
//class ECTest {
//    @ErrorCode.Key("my-test")
//    @ErrorCode.Template("the message")
//    public static final int MY_TEST = 11111;
//
//    @ErrorCode.Key("kkkkk")
//    @ErrorCode.Template("{b.b1-_3.c}")
//    public static final int MY_TEST2 = 22222;
//}