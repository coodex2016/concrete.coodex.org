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

package org.coodex.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by davidoff shen on 2017-05-12.
 */
public class PojoInfo {
    private final Type type;
    private final Class<?> rowType;
    private final Map<String, PojoProperty> properties = new HashMap<>();

    public PojoInfo(Type type) {
        this.type = type;
        this.rowType = GenericTypeHelper.typeToClass(this.type);
        if (rowType == null) throw new RuntimeException(type + " is NOT POJO.");
        buildMethodProperties();
        buildFieldProperties();
    }

    public Type getType() {
        return type;
    }

    public Class<?> getRowType() {
        return rowType;
    }

    private void buildFieldProperties() {
        for (Field field : rowType.getFields()) {
            int mod = field.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isStatic(mod) && !properties.containsKey(field.getName())) {
                properties.put(field.getName(),
                        buildProperty(null, field,
                                Modifier.isFinal(field.getModifiers()),
                                GenericTypeHelper.toReference(field.getGenericType(), this.type),
                                field.getName()));
            }
        }
    }

    private void buildMethodProperties() {
        for (Method method : rowType.getMethods()) {

            if (void.class.equals(method.getReturnType()) ||
                    method.getParameterTypes().length > 0 ||
                    method.getName().equals("getClass") ||
                    Class.class.equals(method.getDeclaringClass()) ||
                    Object.class.equals(method.getDeclaringClass()))
                continue;

            PojoProperty property = testProperty(method, testBoolProperty(method));

            if (property != null) {
                properties.put(property.getName(), property);
            }
        }
    }

    private PojoProperty testProperty(Method method, PojoProperty property) {
        if (property == null) {
            String methodName = method.getName();
            if (methodName.length() > 3
                    && methodName.startsWith("get")) {

                String beanName = lowerFirstChar(methodName.substring(3));
                return buildProperty(
                        method, testField(beanName), isReadOnly(method),
                        GenericTypeHelper.toReference(method.getGenericReturnType(), this.type), beanName);
            }
        }
        return property;
    }

    protected PojoProperty buildProperty(Method method, Field field, boolean readonly, Type type, String name) {
        return new PojoProperty(method, field, readonly, type, name);
    }

    private PojoProperty testBoolProperty(Method method) {
        String methodName = method.getName();
        if (boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType())) {
            if (methodName.length() > 2 && methodName.startsWith("is")) {
                String beanName = lowerFirstChar(methodName.substring(2));
                return buildProperty(
                        method, testField(beanName), isReadOnly(method), method.getReturnType(), beanName);
            }
        }
        return null;
    }

    private boolean isReadOnly(Method method) {
        try {
            rowType.getMethod(
                    "set" + method.getName().substring(method.getName().startsWith("is") ? 2 : 3),
                    method.getReturnType());
            return false;
        } catch (NoSuchMethodException ignored) {
        }
        return true;
    }

    private Field testField(String beanName) {
        return getField(beanName, rowType);
    }

    private Field getField(String fieldName, Class<?> c) {
        if (c == null) return null;
        try {
            return c.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return getField(fieldName, c.getSuperclass());
        }
    }

    private String lowerFirstChar(String s) {
        char ch = s.charAt(0);

        if (ch >= 'A' && ch <= 'Z') {
            char[] buf = s.toCharArray();
            buf[0] = (char) (ch - 'A' + 'a');
            return new String(buf);
        } else
            return s;
    }


    public PojoProperty getProperty(String name) {
        return properties.get(name);
    }

    public Collection<PojoProperty> getProperties() {
        return properties.values();
    }
}
