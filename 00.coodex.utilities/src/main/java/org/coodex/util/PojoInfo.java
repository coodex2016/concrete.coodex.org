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

package org.coodex.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import static org.coodex.util.TypeHelper.toTypeReference;

/**
 * Created by davidoff shen on 2017-05-12.
 */
public class PojoInfo {
    private final Type type;
    private final Class rowType;
    private final Map<String, PojoProperty> properties = new HashMap<String, PojoProperty>();

    public Type getType() {
        return type;
    }

    public Class getRowType() {
        return rowType;
    }

    public PojoInfo(Type type, Type... context) {
        this.type = toTypeReference(type, context);
        rowType = TypeHelper.typeToClass(this.type);
        if (rowType == null) throw new RuntimeException(type + " is NOT POJO.");

        Set<Type> contextSet = new HashSet<Type>();
        if(context != null && context.length > 0){
            contextSet.addAll(Arrays.asList(context));
        }
        contextSet.add(type);
        context = contextSet.toArray(new Type[0]);
        buildMethodProperties(context);
        buildFieldProperties(context);
    }

    private void buildFieldProperties(Type... context) {
        for (Field field : rowType.getFields()) {
            int mod = field.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isStatic(mod) && !properties.containsKey(field.getName())) {
                properties.put(field.getName(),
                        new PojoProperty(field, toTypeReference(field.getGenericType(), context)));
            }
        }
    }

    private void buildMethodProperties(Type... context) {
        for (Method method : rowType.getMethods()) {

            if (void.class.equals(method.getReturnType()) ||
                    method.getParameterTypes().length > 0 ||
                    method.getName().equals("getClass") ||
                    Class.class.equals(method.getDeclaringClass()) ||
                    Object.class.equals(method.getDeclaringClass()))
                continue;

            PojoProperty property = testProperty(method, testBoolProperty(method), context);

            if (property != null) {
                properties.put(property.getName(), property);
            }
        }
    }

    private PojoProperty testProperty(Method method, PojoProperty property, Type... context) {
        if (property == null) {
            String methodName = method.getName();
            if (methodName.length() > 3
                    && methodName.startsWith("get")) {

                String beanName = lowerFirstChar(methodName.substring(3));
                return new PojoProperty(
                        method, testField(beanName), isReadOnly(method),
                        toTypeReference(method.getGenericReturnType(), context), beanName);
            }
        }
        return property;
    }

    private PojoProperty testBoolProperty(Method method) {
        String methodName = method.getName();
        if (boolean.class.equals(method.getReturnType()) || Boolean.class.equals(method.getReturnType())) {
            if (methodName.length() > 2 && methodName.startsWith("is")) {
                String beanName = lowerFirstChar(methodName.substring(2));
                return new PojoProperty(
                        method, testField(beanName), isReadOnly(method), method.getReturnType(), beanName);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean isReadOnly(Method method) {
        try {
            rowType.getMethod(
                    "set" + method.getName().substring(method.getName().startsWith("is") ? 2 : 3),
                    method.getReturnType());
            return false;
        } catch (NoSuchMethodException e) {
        }
        return true;
    }

    private Field testField(String beanName) {
        return getField(beanName, rowType);
    }

    private Field getField(String fieldName, Class c) {
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
