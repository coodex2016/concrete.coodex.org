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

/**
 *
 */
package org.coodex.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author davidoff
 */
public class TypeHelper {

    public /* static */ interface ExceptClassFilter {
        boolean except(Class<?> c);
    }

    private static class JavaLangExceptFilter implements ExceptClassFilter {
        //      @Override
        public boolean except(Class<?> c) {
            return c.getPackage().getName().startsWith("java.lang");
        }
    }

    private final static ExceptClassFilter javaLangExceptFilter = new JavaLangExceptFilter();

    @SuppressWarnings("unchecked")
    @Deprecated
    public static Type findActualClassFrom(TypeVariable type, Type instanceClass) {
        return findActualClassFromInstanceClass(type, instanceClass);
    }


    @SuppressWarnings("unchecked")
    @Deprecated
    public static Type findActualClassFromInstanceClass(
            TypeVariable<Class<?>> type, Type instancedClass) {
        return findActualClassFromInstanceClass(type, instancedClass, null);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static Type findActualClassFromInstanceClass(
            TypeVariable<Class<?>> type, Type instancedClass,
            ExceptClassFilter filter) {
        Type t = type;
        do {
            t = searchActualType((TypeVariable<Class<?>>) t, instancedClass,
                    filter);
        } while (t instanceof TypeVariable);

        return t;
    }

    @Deprecated
    private static Type searchActualTypeInParameterizedType(
            TypeVariable<Class<?>> type, ParameterizedType pt,
            ExceptClassFilter filter) {

        Type result = null;
        Class<?> declaringClass = type.getGenericDeclaration();

        if (pt.getRawType() == declaringClass) {

            Type[] t = declaringClass.getTypeParameters();
            for (int i = 0; i < t.length; i++)
                if (type == t[i]) {
                    result = pt.getActualTypeArguments()[i];
                    break;
                }
        } else {
//            Type[] types = pt.getActualTypeArguments();
//            for (int i = 0; i < types.length; i++) {
            for (Type $ : pt.getActualTypeArguments()) {
                result = searchActualType(type, $/*types[i]*/, filter);
                if (result != null)
                    break;
            }
//            }
        }
        return result;
    }

    @Deprecated
    public static Type searchActualType(TypeVariable<Class<?>> type,
                                        Type instancedInfo) {
        return searchActualType(type, instancedInfo, javaLangExceptFilter);
    }

    @Deprecated
    public static Type searchActualType(TypeVariable<Class<?>> type,
                                        Type instancedInfo, ExceptClassFilter filter) {

        if (type == null || instancedInfo == null)
            return null;

        Type result = null;

        if (instancedInfo instanceof ParameterizedType) {

            result = searchActualTypeInParameterizedType(type,
                    (ParameterizedType) instancedInfo, filter);

        } else if (instancedInfo instanceof Class) {
            result = searchActualTypeInClass(type, (Class<?>) instancedInfo,
                    filter);

        } else if (instancedInfo instanceof GenericArrayType) {

            result = searchActualType(type,
                    ((GenericArrayType) instancedInfo).getGenericComponentType(),
                    filter);
        }

        return result;
    }

    @Deprecated
    private static Type searchActualTypeInClass(TypeVariable<Class<?>> type,
                                                Class<?> instancedInfo, ExceptClassFilter filter) {
        if (filter != null && filter.except(instancedInfo)
                || javaLangExceptFilter.except(instancedInfo))
            return null;

        Type result;
        // search in genericSuperClass
        result = searchActualType(type, instancedInfo.getGenericSuperclass(),
                filter);

        // search in genericInterfaces
        if (result == null) {
            Type[] interfaces = instancedInfo.getGenericInterfaces();
            for (Type $ : interfaces) {
                result = searchActualType(type, $, filter);
                if (result != null)
                    break;
            }
        }

        // search in super class
        if (result == null)
            result = searchActualType(type, instancedInfo.getSuperclass(), filter);

        // search in interfaces
        if (result == null) {
            Class<?>[] interfaces = instancedInfo.getInterfaces();
            for (Class<?> $ : interfaces) {
                result = searchActualType(type, $, filter);
                if (result != null)
                    break;
            }
        }
        return result;
    }

    private static class $$GenericArrayType implements GenericArrayType {
        private final Type genericComponentType;

        private $$GenericArrayType(GenericArrayType type, Type... contextClass) {
            this.genericComponentType = toTypeReference(type.getGenericComponentType(), contextClass);
        }

        public Type getGenericComponentType() {
            return genericComponentType;
        }

        @Override
        public String toString() {
            return genericComponentType.toString() + "[]";
        }
    }


    private static class $$ParameterizedType implements ParameterizedType {
        private final ParameterizedType type;
        private List<Type> types = new ArrayList<Type>();

        $$ParameterizedType(ParameterizedType type, Type... contextClass) {
            this.type = type;
            for (int i = 0; i < type.getActualTypeArguments().length; i++) {
                Type $ = type.getActualTypeArguments()[i];
                types.add(toTypeReference($, contextClass));
            }
        }

        public Type[] getActualTypeArguments() {
            return types.toArray(new Type[0]);
        }

        public Type getRawType() {
            return type.getRawType();
        }

        public Type getOwnerType() {
            return type.getOwnerType();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(((Class) getRawType()).getName());
            if (types.size() > 0) {
                builder.append("<");

                for (int i = 0; i < types.size(); i++) {
                    if (i > 0) builder.append(',');
                    builder.append(types.get(i).toString());
                }
                builder.append(">");
            }
            return builder.toString();
        }
    }

    /////////////////////////
    // 2017-05-12 彻底重构
    /////////////////////////

    public static Class typeToClass(Type type) {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedType) {
            return (Class) ((ParameterizedType) type).getRawType();
        } else {
            return null;
        }
    }

    public static Type toTypeReference(Type type, Type... contextClass) {
        if (type instanceof ParameterizedType) {
            return buildParameterizedType((ParameterizedType) type, contextClass);
        } else if (type instanceof TypeVariable) {
            return solve((TypeVariable) type, contextClass);
        } else if (type instanceof GenericArrayType) {
            return buildGenericArrayType((GenericArrayType) type, contextClass);
        } else
            return type;
    }

    public static Type solve(TypeVariable variable, Type... types) {
        if (types == null || types.length == 0) return null;
        if (variable.getGenericDeclaration() instanceof Class) {
            Type t = solveClassTypeVariable(variable, types);
            return t == null ? variable : t;
        } else { // Method 等怎么搞？
            return variable;
        }
    }

    // 查找定义在Class中的TypeVariable
    private static Type solveClassTypeVariable(TypeVariable variable, Type... types) {
        Class declaration = (Class) variable.getGenericDeclaration();
        Type $ = null;
        for (Type t : types) {
            t = isSuper(declaration, t);
            if (t != null) { // 只检查declaration的子类
                // t 只有可能是两种情况: class/ parameterizedType/
                if (t instanceof Class) {
                    $ = solveInClass(variable, (Class) t);
                } else if (t instanceof ParameterizedType) {
                    $ = solveInParameterizedType(variable, (ParameterizedType) t);
                }

                if ($ instanceof TypeVariable) { // TypeVariable: 继续找
                    $ = solve((TypeVariable) $, types);
                } else if ($ instanceof ParameterizedType) { // 构建具体的ParameterizedType
                    return buildParameterizedType((ParameterizedType) $, types);
                } else if ($ instanceof GenericArrayType) { // 构建具体的GenericArrayType
                    return buildGenericArrayType((GenericArrayType) $, types);
                } else if ($ != null) {
                    break;
                }
            }
        }
        return $;
    }

    private static Type buildGenericArrayType(GenericArrayType $, Type... types) {
        return $ instanceof $$GenericArrayType ? $ : new $$GenericArrayType($, types);
    }

    private static Type buildParameterizedType(ParameterizedType $, Type... types) {
        return $ instanceof $$ParameterizedType ? $ : new $$ParameterizedType($, types);
    }

    private static Type solveInClass(TypeVariable variable, Class c) {
        if (c == null)
            return null;

        // search in genericSuperClass
        Type result = solveInSuper(variable, c.getGenericSuperclass());

        // search in genericInterfaces
        if (result == null) {
            for (Type $ : c.getGenericInterfaces()) {
                result = solveInSuper(variable, $);
                if (result != null)
                    break;
            }
        }

        // search in super class
        if (result == null)
            result = solveInClass(variable, c.getSuperclass());

        // search in interfaces
        if (result == null) {
            for (Class<?> $ : c.getInterfaces()) {
                result = solveInClass(variable, $);
                if (result != null)
                    break;
            }
        }
        return result;
    }

    private static Type solveInSuper(TypeVariable variable, Type superType) {
        if (superType instanceof Class) {
            return solveInClass(variable, (Class) superType);
        } else if (superType instanceof ParameterizedType) {
            return solveInParameterizedType(variable, (ParameterizedType) superType);
        } else
            return null;
    }

    private static Type solveInParameterizedType(TypeVariable variable, ParameterizedType pt) {

        Class<?> declaringClass = (Class<?>) variable.getGenericDeclaration();
        if (declaringClass.equals(pt.getRawType())) {

            Type[] t = declaringClass.getTypeParameters();
            for (int i = 0; i < t.length; i++)
                if (variable == t[i]) {
                    return pt.getActualTypeArguments()[i];
                }
        } else {
            // 是否有必要？
            return solveClassTypeVariable(variable, pt.getActualTypeArguments());
        }
        return null;
    }


    private static Type isSuper(Class<?> c, Type toTest) {
        if (toTest == null || c == null) return null;
        if (toTest instanceof Class) {
            Class<?> toTestClass = (Class<?>) toTest;
            return toTestClass.isArray() ? isSuper(c, ((Class) toTest).getComponentType()) :
                    c.isAssignableFrom(toTestClass) ? toTestClass : null;
        } else if (toTest instanceof ParameterizedType) {
            return toTest;
        } else if (toTest instanceof GenericArrayType) {
            return isSuper(c, ((GenericArrayType) toTest).getGenericComponentType());
        }
        return null;
    }


    private static final Class[] PRIMITIVE_CLASSES = new Class[]{
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
        return Common.inArray(c, PRIMITIVE_CLASSES);
    }

}
