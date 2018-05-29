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

package org.coodex.concrete.message;

import org.coodex.util.SingletonMap;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GenericTypeHelper {

    private static SingletonMap<Type, GenericTypeInfo> typeInfos = new SingletonMap<Type, GenericTypeInfo>(
            new SingletonMap.Builder<Type, GenericTypeInfo>() {
                @Override
                public GenericTypeInfo build(Type key) {
                    return new GenericTypeInfo(key);
                }
            }
    );

    public static Type solve(TypeVariable t, Type context) {
        return typeInfos.getInstance(context).find(t);
    }

    public static Type toReference(Type t, Type context) {
        return t instanceof TypeVariable ?
                solve((TypeVariable) t, context) :
                build(t, context);
    }


    private static Type build(Type t, Type context) {
        GenericTypeInfo contextInfo = typeInfos.getInstance(context);
        if (t instanceof Class) {
            return t;
        } else if (t instanceof ParameterizedType) {
            return new ParameterizedTypeImpl(contextInfo, (ParameterizedType) t);
        } else if (t instanceof GenericArrayType) {
            return new GenericArrayTypeImpl(contextInfo, (GenericArrayType) t);
        } else if (t instanceof TypeVariable) {
            return contextInfo.find((TypeVariable) t);
        } else {
            return t;
        }
    }

    public abstract static class GenericType<T> {

        private final Type type;

        protected GenericType() {
            this.type = typeInfos.getInstance(getClass())
                    .find(GenericType.class.getTypeParameters()[0]);
        }

        protected GenericType(Type context) {
            this.type = build(context);
        }

        private Type build(Type context) {
            Type t = typeInfos.getInstance(getClass())
                    .find(GenericType.class.getTypeParameters()[0]);
            return GenericTypeHelper.build(t, context);

        }

        public Type getType() {
            return type;
        }
    }


    private static class GenericArrayTypeImpl implements GenericArrayType {

        private final Type genericComponentType;

        GenericArrayTypeImpl(GenericTypeInfo genericTypeInfo, GenericArrayType genericArrayType) {
            Type gct = genericArrayType.getGenericComponentType();
            if (gct instanceof TypeVariable) {
                genericComponentType = genericTypeInfo.find((TypeVariable<Class>) gct);
            } else if (gct instanceof ParameterizedType) {
                genericComponentType = new ParameterizedTypeImpl(genericTypeInfo, (ParameterizedType) gct);
            } else if (gct instanceof GenericArrayType) {
                genericComponentType = new GenericArrayTypeImpl(genericTypeInfo, (GenericArrayType) gct);
            } else {
                genericComponentType = gct;
            }
        }


        @Override
        public Type getGenericComponentType() {
            return genericComponentType;
        }


        @Override
        public String toString() {
            return genericComponentType.toString() + "[]";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GenericArrayTypeImpl that = (GenericArrayTypeImpl) o;

            return genericComponentType != null ? genericComponentType.equals(that.genericComponentType) : that.genericComponentType == null;
        }

        @Override
        public int hashCode() {
            return genericComponentType != null ? genericComponentType.hashCode() : 0;
        }
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        private final Type rawType;
        private final Type ownerType;
        private final List<Type> actualTypeArguments = new ArrayList<Type>();

        ParameterizedTypeImpl(GenericTypeInfo genericTypeInfo, ParameterizedType pt) {
            rawType = pt.getRawType();
            ownerType = pt.getOwnerType();
            for (Type t : pt.getActualTypeArguments()) {
                if (t instanceof TypeVariable) {
                    actualTypeArguments.add(genericTypeInfo.find((TypeVariable<Class>) t));
                } else if (t instanceof ParameterizedType) {
                    actualTypeArguments.add(new ParameterizedTypeImpl(genericTypeInfo, (ParameterizedType) t));
                } else if (t instanceof GenericArrayType) {
                    actualTypeArguments.add(new GenericArrayTypeImpl(genericTypeInfo, (GenericArrayType) t));
                } else {
                    actualTypeArguments.add(t);
                }
            }
        }


        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments.toArray(new Type[0]);
        }

        @Override
        public Type getRawType() {
            return this.rawType;
        }

        @Override
        public Type getOwnerType() {
            return this.ownerType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ParameterizedTypeImpl that = (ParameterizedTypeImpl) o;

            if (rawType != null ? !rawType.equals(that.rawType) : that.rawType != null) return false;
            if (ownerType != null ? !ownerType.equals(that.ownerType) : that.ownerType != null) return false;
            return actualTypeArguments != null ? actualTypeArguments.equals(that.actualTypeArguments) : that.actualTypeArguments == null;
        }

        @Override
        public int hashCode() {
            int result = rawType != null ? rawType.hashCode() : 0;
            result = 31 * result + (ownerType != null ? ownerType.hashCode() : 0);
            result = 31 * result + (actualTypeArguments != null ? actualTypeArguments.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(((Class) getRawType()).getName());
            if (actualTypeArguments.size() > 0) {
                builder.append("<");

                for (int i = 0; i < actualTypeArguments.size(); i++) {
                    if (i > 0) builder.append(',');
                    Type t = actualTypeArguments.get(i);
                    if (t == null) {
                        builder.append("null");
                    } else if (t instanceof TypeVariable) {
                        builder.append(((TypeVariable) t).getName())
                                .append(" in ").append(((TypeVariable) t).getGenericDeclaration());
                    } else {
                        builder.append(t == null ? null : t.toString());
                    }
                }
                builder.append(">");
            }
            return builder.toString();
        }
    }

    private static class GenericTypeInfo {

        private Map<TypeVariable<Class>, Type> map = new ConcurrentHashMap<TypeVariable<Class>, Type>();
        private Set<Type> processed = new HashSet<Type>();

        GenericTypeInfo(Type x) {
            process(x);
        }

        Type find(TypeVariable t) {
            Type type = map.get(t);
            if (type == null) return t;
            if (type instanceof TypeVariable) {
                return find((TypeVariable) type);
            } else if (type instanceof ParameterizedType) {
                return new ParameterizedTypeImpl(this, (ParameterizedType) type);
            } else if (type instanceof GenericArrayType) {
                return new GenericArrayTypeImpl(this, (GenericArrayType) type);

            } else {
                return type;
            }
        }

        private void process(Type x) {
            if (x == null) return;
            if (processed.contains(x)) return;
            processed.add(x);

            if (x instanceof Class) {
                if (((Class) x).isArray()) {
                    process(((Class) x).getComponentType());
                } else {
                    if (!((Class) x).isInterface())
                        process(((Class) x).getGenericSuperclass());
                    for (Type intf : ((Class) x).getGenericInterfaces()) {
                        process(intf);
                    }
                }

            } else if (x instanceof ParameterizedType) {
                Class c = (Class) ((ParameterizedType) x).getRawType();
                for (int i = 0; i < c.getTypeParameters().length; i++) {
                    map.put(c.getTypeParameters()[i], ((ParameterizedType) x).getActualTypeArguments()[i]);
                }
                process(c);
            } else if (x instanceof GenericArrayType) {
                process(((GenericArrayType) x).getGenericComponentType());
            } else {

            }
        }


    }


}
