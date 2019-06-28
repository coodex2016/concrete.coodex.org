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

package org.coodex.concrete.core.signature;

import org.coodex.concrete.common.ConcreteException;
import org.coodex.concrete.common.ErrorCodes;
import org.coodex.concrete.common.SignatureSerializer;
import org.coodex.util.Common;
import org.coodex.util.PojoInfo;
import org.coodex.util.PojoProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by davidoff shen on 2017-04-21.
 */
public class DefaultSignatureSerializer implements SignatureSerializer {

    private final static Logger log = LoggerFactory.getLogger(DefaultSignatureSerializer.class);

    private static final Class[] PRIMITIVE_CLASS = new Class[]{
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

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn(e.getLocalizedMessage(), e);
            return null;
        }
    }

    @Override
    public byte[] serialize(Map<String, Object> values) {
        if (values == null || values.size() == 0)
            return new byte[0];
        else
            return toSign(values).getBytes();
    }

    @SuppressWarnings("unchecked")
    private String joint(String key, Object o, boolean first) {
        if (o == null)
            return null;

        Class<?> c = o.getClass();
        String s = null;
        // 数组、List、Set，Set需要排序
        if (c.isArray()) {
            s = jointArray(key, Arrays.asList((Object[]) o));
        } else if (List.class.isAssignableFrom(c)) {
            s = jointArray(key, (List<Object>) o);
        } else if (Set.class.isAssignableFrom(c)) {
            Object[] objects = ((Set) o).toArray();
            Arrays.sort(objects);
            s = jointArray(key, Arrays.asList(objects));
        }
        // 基本类型，double按照保留6位小数转换
        else if (Common.inArray(c, PRIMITIVE_CLASS)) {
            s = encode(key) + "=" +
                    encode((double.class.equals(c) || Double.class.equals(c)) ?
                            String.format("%.6f", o)
                            : o.toString());

        }
        // Pojo
        else {
            s = jointPojo(key, o);
        }

        return Common.isBlank(s) ? null : ((first ? "" : "&") + s);
    }

    private String jointArray(String key, List<Object> list) {
        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (Object o : list) {
            if (o != null) {
                String joint = joint(key, o, isFirst);
                if (!Common.isBlank(joint)) {
                    builder.append(joint);
                    isFirst = false;
                }
            }
        }
        return builder.toString();
    }

    private Object getValue(PojoProperty pojoProperty, Object o) throws InvocationTargetException, IllegalAccessException {
        Method method = pojoProperty.getMethod();
        if (method != null) {
            method.setAccessible(true);
            return method.invoke(o);
        }
        Field field = pojoProperty.getField();
        if (field != null) {
            field.setAccessible(true);
            return field.get(o);
        }
        return null;
    }

    private String jointPojo(String key, Object o) {
        Map<String, Object> map = new HashMap<String, Object>();
        // TODO : bug fix
        PojoInfo pojoInfo = new PojoInfo(o.getClass());
        for (PojoProperty pojoProperty : pojoInfo.getProperties()) {
            String propertyName = pojoProperty.getName();
            try {
                map.put(propertyName, getValue(pojoProperty, o));
            } catch (InvocationTargetException e) {
                log.error("unable to get field value : {}", propertyName);
                throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, e.getLocalizedMessage());
            } catch (IllegalAccessException e) {
                log.error("unable to get field value : {}", propertyName);
                throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, e.getLocalizedMessage());
            }
        }
//        for (Field field : ReflectHelper.getAllDeclaredFields(o.getClass())) {
//            field.setAccessible(true);
//            if (Modifier.isStatic(field.getModifiers())
//                    || Modifier.isTransient(field.getModifiers())) continue;
//            try {
//                map.put(field.getName(), field.get(o));
//            } catch (IllegalAccessException e) {
//                log.error("unable to get field value : {}", field.getName());
//                throw new ConcreteException(ErrorCodes.UNKNOWN_ERROR, e.getLocalizedMessage());
//            }
//        }
        String s = toSign(map);
        return Common.isBlank(s) ? null : (encode(key) + "=" + encode(s));
    }

    private String toSign(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        String[] keys = map.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        boolean isFirst = true;
        for (String key : keys) {
            String joint = joint(key, map.get(key), isFirst);
            if (!Common.isBlank(joint)) {
                builder.append(joint);
                isFirst = false;
            }
        }
        return builder.toString();
    }

}
