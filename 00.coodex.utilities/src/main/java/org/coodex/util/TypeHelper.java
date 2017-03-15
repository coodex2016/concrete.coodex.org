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
    public static Type findActualClassFrom(TypeVariable type, Type instanceClass) {
        return findActualClassFromInstanceClass(type, instanceClass);
    }

    @SuppressWarnings("unchecked")
    public static Type findActualClassFromInstanceClass(
            TypeVariable<Class<?>> type, Type instancedClass) {
        return findActualClassFromInstanceClass(type, instancedClass, null);
    }

    @SuppressWarnings("unchecked")
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

    public static Type searchActualType(TypeVariable<Class<?>> type,
                                        Type instancedInfo) {
        return searchActualType(type, instancedInfo, javaLangExceptFilter);
    }

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


    public static Type toTypeReference(Type type, Class<?> contextClass) {
        if (type instanceof ParameterizedType) {
            return new $$ParameterizedType((ParameterizedType) type, contextClass);
        } else if (type instanceof TypeVariable) {
            return findActualClassFrom((TypeVariable) type, contextClass);
        } else if (type instanceof GenericArrayType) {
            return new $$GenericArrayType((GenericArrayType) type, contextClass);
        }
        return type;
    }

    private static class $$GenericArrayType implements GenericArrayType {
        private final Type genericComponentType;

        private $$GenericArrayType(GenericArrayType type, Class<?> contextClass) {
            this.genericComponentType = toTypeReference(type.getGenericComponentType(), contextClass);
        }

        public Type getGenericComponentType() {
            return genericComponentType;
        }
    }


    private static class $$ParameterizedType implements ParameterizedType {
        private final ParameterizedType type;
        private List<Type> types = new ArrayList<Type>();

        $$ParameterizedType(ParameterizedType type, Class<?> contextClass) {
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
            return "$$ParameterizedType{" +
                    "type=" + type +
                    ", types=" + types +
                    '}';
        }
    }

}
