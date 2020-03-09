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

import org.coodex.concrete.api.ErrorMsg;
import org.coodex.util.Common;
import org.coodex.util.I18N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

import static org.coodex.concrete.common.ConcreteContext.getServiceContext;


/**
 * Created by davidoff shen on 2016-09-04.
 */
public class ErrorMessageFacade extends AbstractMessageFacade {

    /*
     * 没有MessageFormatter Provider的时候，以此Formatter输出，确保formatter不为空
     * 2016-11-01，修改默认使用JavaTextFormatMessageFormatter
     */
    private final static Logger log = LoggerFactory.getLogger(ErrorMessageFacade.class);


    private final static Set<Class<? extends AbstractErrorCodes>> REGISTERED = new HashSet<Class<? extends AbstractErrorCodes>>();

    private final static Map<Integer, Field> errorCodes = new HashMap<Integer, Field>();


    private ErrorMessageFacade() {
    }

    private static void registerClass(Class<? extends AbstractErrorCodes> clz) {
        if (clz == null) return;
        if (REGISTERED.contains(clz)) return;

        synchronized (errorCodes) {
            for (Field f : clz.getDeclaredFields()) {

                if (!(int.class.equals(f.getType())
                        && Modifier.isStatic(f.getModifiers())
                        && Modifier.isPublic(f.getModifiers()))) continue;

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
        }
        REGISTERED.add(clz);
    }

    @SuppressWarnings("unchecked")
    public static void register(Class<? extends AbstractErrorCodes>... classes) {
        for (Class<? extends AbstractErrorCodes> clz : classes)
            registerClass(clz);
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

    private static String getMessageOrPattern(boolean format, int code, Object... objects) {
        Field f = errorCodes.get(code);
        ErrorMsg errorMsg = null;
        ErrorMsg formatterValue = null;
        if (f == null) {
            if(getServiceContext() instanceof ServerSideContext) {
                log.debug("errorCode [{}] has not register.", code);
            }
//            return null;
        } else {

            errorMsg = f.getAnnotation(ErrorMsg.class);
            formatterValue = errorMsg == null ? f.getDeclaringClass().getAnnotation(ErrorMsg.class) : errorMsg;
        }
        MessageFormatter formatter = getFormatter(formatterValue == null ? null : formatterValue.formatterClass());

        AbstractErrorCodes.Namespace namespace = f == null ? null : f.getDeclaringClass().getAnnotation(AbstractErrorCodes.Namespace.class);

        String errorMessageNamespace = namespace == null ? "message" : namespace.value();
        errorMessageNamespace = Common.isBlank(errorMessageNamespace) ? f.getDeclaringClass().getName() : errorMessageNamespace;

        String msgTemp = (errorMsg == null || Common.isBlank(errorMsg.value().trim())) ?
                "{" + (formatter.getNamespace() == null ? "" : (formatter.getNamespace() + ".")) + errorMessageNamespace + "." + code + "}" : errorMsg.value();

//        String pattern = msgTemp;
//        if (msgTemp.startsWith("{") && msgTemp.endsWith("}")) {
//            String key = msgTemp.substring(1, msgTemp.length() - 1).trim();
//            pattern = getPatternLoader(errorMsg == null ? null : errorMsg.patternLoaderClass())
//                    .getMessageTemplate(key);
//        }
        String pattern = I18N.translate(msgTemp);

        return (pattern != null) ? (format ? formatter.format(pattern, objects) : pattern) : null;
    }

    private static Object[] actualObjects(Object[] objects) {
        if (objects == null || objects.length == 0) return objects;
        Object[] objectArray = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            objectArray[i] = objects[i] instanceof Supplier ? ((Supplier) objects[i]).get() : objects[i];
        }
        return objectArray;
    }


    public static List<ErrorDefinition> getAllErrorInfo() {
        final List<ErrorDefinition> errorDefinitions = new ArrayList<ErrorDefinition>();
        for (Integer i : allRegisteredErrorCodes()) {
            errorDefinitions.add(new ErrorDefinition(i));
        }
        Collections.sort(errorDefinitions);
        return errorDefinitions;
    }

}
