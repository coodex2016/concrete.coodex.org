/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.apitools.jaxrs.axios;

import org.coodex.closure.StackClosureContext;
import org.coodex.concrete.api.Description;
import org.coodex.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TsDefineHelper {

    private static final Logger log = LoggerFactory.getLogger(TsDefineHelper.class);
//    @Deprecated
//    private static final Type[] NUMBER = {
//            int.class, float.class, double.class, byte.class, long.class, short.class,
//            Integer.class, Long.class, Byte.class, Short.class, Float.class, Double.class
//    };

    private static final Type[] INTEGER_NUMBER = {
            int.class, Integer.class, byte.class, Byte.class,
            short.class, Short.class, Long.class, long.class
    };

    private static final Type[] FLOAT_NUMBER = {
            float.class, Float.class, double.class, Double.class
    };
    private static final Type[] STRING = {
            char.class, Character.class, String.class
    };
    private static final Type[] BOOLEAN = {
            boolean.class, Boolean.class
    };
    private final static Map<Type, TsType> CACHE = new HashMap<>();
    private final static Map<String, TsType> EMPTY_MAP = Common.cast(Collections.EMPTY_MAP);
    private final static StackClosureContext<Type> stackClosureContext = new StackClosureContext<>();
    private final static String BOOL_NAME = "boolean";
    private final static String STRING_NAME = "string";
//    @Deprecated
//    private final static String NUMBER_NAME = "number";

    private final static String INTEGER_NAME = "Int";
    private final static String FLOAT_NAME = "Float";

    private final static String ANY_NAME = "any";
    private final static String VOID_NAME = "void";
    private final static String[] TS_PRIMITIVE_TYPES = {
            BOOL_NAME, STRING_NAME, ANY_NAME, VOID_NAME, INTEGER_NAME, FLOAT_NAME
    };
    private static final StackClosureContext<String> dependenciesContext = new StackClosureContext<>();

    private static boolean isEnum(Type t) {
        if (t instanceof Class<?>) {
            Class<?> c = (Class<?>) t;
            if (c.isEnum()) return true;
            if (c.isArray()) return isEnum(c.getComponentType());
            return false;
        } else if (t instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) t;
            return isEnum(gat.getGenericComponentType());
        } else if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type rowType = pt.getRawType();
            if (rowType instanceof Class<?>) {
                Class<?> rowClz = (Class<?>) rowType;
                if (Collection.class.isAssignableFrom(rowClz)) {
                    return isEnum(
                            GenericTypeHelper.solveFromType(Collection.class.getTypeParameters()[0], t)
                    );
                }
            }
        }
        return false;
    }

    public static void ifEnum(Type t, Consumer<Class<Enum<?>>> consumer) {
        if (isEnum(t)) {
            consumer.accept(Common.cast(t));
        }
    }

    public static String getTypeScriptValueType(Class<Enum<?>> enumClass) {
        if (Valuable.class.isAssignableFrom(enumClass)) {
            Type t = GenericTypeHelper.solveFromType(
                    Valuable.class.getTypeParameters()[0],
                    enumClass
            );
            if (Common.inArray(t, STRING)) {
                return STRING_NAME;
            } else if (Common.inArray(t, INTEGER_NUMBER)) {
                return INTEGER_NAME;
            } else if (Common.inArray(t, FLOAT_NUMBER)) {
                return FLOAT_NAME;
            } else if (Common.inArray(t, BOOLEAN)) {
                return BOOL_NAME;
            } else {
                throw new RuntimeException("nonsupport type: " + t);
            }
        } else {
            return STRING_NAME;
        }
    }

    private static TsType contextRunnable(Type t, Type... context) {
        if (CACHE.containsKey(t)) return CACHE.get(t);

        if (stackClosureContext.contains(t)) {
            return new TsTypeRef(t, context);
        }
        TsType tsType = Common.cast(stackClosureContext.call(t, () -> {
            if (t instanceof Class<?>) {
                return TsClass.of((Class<?>) t);
            } else if (t instanceof ParameterizedType) {
                return TsParameterizedType.of((ParameterizedType) t);
            } else if (t instanceof GenericArrayType) {
                return TsParameterizedType.of((GenericArrayType) t);
            } else if (t instanceof TypeVariable) {
                return new TsTypeVariable(((TypeVariable<?>) t).getName());
            }
            throw new RuntimeException("unsupported type: " + t);
        }));
        CACHE.put(t, tsType);
        return tsType;
    }

    public static TsType javaToTs(Type t, Type... context) {
        return contextRunnable(t, context);
    }

    public static String toTypeScriptDef(List<TsType> tsTypes) {
        Map<String, TsType> tsTypeMap = new HashMap<>();
        tsTypes.forEach(type -> {
            if (type.isDefinable()) {
                tsTypeMap.putIfAbsent(type.getName(), type);
            }
            tsTypeMap.putAll(type.getDependencies());
        });
        TsNameSpace root = new TsNameSpace(null);
        tsTypeMap.forEach((k, v) -> {
            if (!v.isDefinable()) return;
            String[] nodes = k.split("\\.");
            TsNameSpace x = root;
            for (int i = 0, l = nodes.length - 1; i < l; i++) {
                x = x.get(nodes[i]);
            }
            x.tsTypes.add(v);
        });
//        System.out.println(root.simplify());
        List<TsNameSpace> simplified = root.simplify();
        StringBuilder builder = new StringBuilder();
        simplified.forEach(ns -> {
            builder.append(ns.toDTS(ns.name == null ? null : "", ns.name != null));
        });

        return builder.toString();
    }

    public interface TsType {

        String getName();

        String toText();

        boolean isDefinable();

        default boolean isPrimitive() {
            return Common.inArray(getName(), TS_PRIMITIVE_TYPES);
        }

        default Map<String, TsType> getDependencies() {
            return EMPTY_MAP;
        }

        default String toDTS(String intend, boolean declare) {
            return null;
        }

    }

    static class TsNameSpace {
        private final List<TsType> tsTypes = new ArrayList<>();
        private final List<TsNameSpace> nameSpaces = new ArrayList<>();
        private String name;

        TsNameSpace(String name) {
            this.name = name;
        }

        private boolean isBlank() {
            return tsTypes.isEmpty();
        }

        private String nextIntend(String indent) {
            return indent == null ? "" : (indent + (isBlank() ? "" : "\t"));
        }


        private List<TsNameSpace> simplify() {
            if (isBlank()) {
                List<TsNameSpace> simplifiedNameSpace = new ArrayList<>();
                nameSpaces.forEach(n -> simplifiedNameSpace.addAll(n.simplify()));
                if (name != null)
                    simplifiedNameSpace.forEach(n -> n.name = name + "." + n.name);
                return simplifiedNameSpace;
            } else {
                return Collections.singletonList(this);
            }
        }

        public String getName() {
            return name;
        }

        public TsNameSpace get(String sub) {
            return nameSpaces.stream()
                    .filter(n -> Objects.equals(n.name, sub))
                    .findFirst()
                    .orElseGet(() -> {
                        TsNameSpace tsNameSpace = new TsNameSpace(sub);
                        nameSpaces.add(tsNameSpace);
                        return tsNameSpace;
                    });
        }


        public String toDTS(String indent, boolean declare) {
            boolean root = Common.isBlank(name);
            String _indent = indent == null ? "" : indent;
            String nextIndent = nextIntend(indent);
            StringBuilder builder = new StringBuilder(_indent);


            if (!root) {// 非root
                builder.append(declare ? "declare " : "")
                        .append("namespace ").append(name).append(" {\n");
            }

            nameSpaces.stream().sorted(Comparator.comparing(TsNameSpace::getName)).forEach(n -> {
                builder.append(n.toDTS(nextIndent, root));
            });

            tsTypes.stream().sorted(Comparator.comparing(TsType::getName)).forEach(t -> {
                if (t instanceof TsClass) {
                    TsClass tsClass = (TsClass) t;
                    builder.append(tsClass.toDTS(nextIndent, Common.isBlank(name))).append("\n");
                } else {
                    throw new RuntimeException("???:" + t);
                }
            });

            if (!root) {
                builder.append(_indent).append("}\n");
            }
            return builder.toString();
        }

        @Override
        public String toString() {
            return "TsNameSpace{" +
                    "tsTypes=" + tsTypes +
                    ", name='" + name + '\'' +
                    ", nameSpaces=" + nameSpaces +
                    '}';
        }
    }

    public static class TsTypeRef implements TsType {
        private final Singleton<TsType> supplier;

        public TsTypeRef(Type type, Type[] context) {
            this.supplier = Singleton.with(() -> javaToTs(type, context));
        }

        public TsType getActualType() {
            return supplier.get();
        }

        @Override
        public String getName() {
            return supplier.get().getName();
        }

        @Override
        public String toText() {
            return supplier.get().toText();
        }

        @Override
        public boolean isDefinable() {
            return supplier.get().isDefinable();
        }

        @Override
        public Map<String, TsType> getDependencies() {
            return supplier.get().getDependencies();
        }
    }

    public static class TsClass implements TsType {


        public static TsClass BOOL = new TsClass(BOOL_NAME, false, null);
        //        public static TsClass INTEGER = new TsClass()
//        @Deprecated
//        public static TsClass NUMBER = new TsClass(NUMBER_NAME, false, null);
        public static TsClass FLOAT = new TsClass(FLOAT_NAME, false, null);
        public static TsClass INT = new TsClass(INTEGER_NAME, false, null);
        public static TsClass STRING = new TsClass(STRING_NAME, false, null);

        public static TsClass ANY = new TsClass(ANY_NAME, false, null);
        public static TsClass VOID = new TsClass(VOID_NAME, false, null);
        private final Set<TsField> fields = new HashSet<>();
        private final List<TsType> typeParameters = new ArrayList<>();
        private final String name;
        private final boolean array;
        private final TsType componentClass;
        private String namespace;
        private boolean def = false;


        public TsClass(String name, boolean array, TsType componentClass) {
            this.name = name;
            this.array = array;
            this.componentClass = componentClass;
        }

        public static TsType of(Class<?> c) {
            if (c.isArray()) return new TsClass("", true, of(c.getComponentType()));
//            if (Common.inArray(c, TsDefineHelper.NUMBER)) return NUMBER;
            if (Common.inArray(c, TsDefineHelper.FLOAT_NUMBER)) return FLOAT;
            if (Common.inArray(c, TsDefineHelper.INTEGER_NUMBER)) return INT;
            if (Common.inArray(c, TsDefineHelper.BOOLEAN)) return BOOL;
            if (Common.inArray(c, TsDefineHelper.STRING)) return STRING;
            if (c.isEnum())
                return new TsClass(getTypeScriptValueType(Common.cast(c)), false, null);

            if (Void.class.equals(c) || void.class.equals(c)) return VOID;

            if (c.getPackage().getName().startsWith("java.")) {
                // non support
                log.warn("unsupported type: {}. use `any`", c);
                return ANY;
            }
            TsClass tsClass = new TsClass(c.getTypeName().substring(c.getPackage().getName().length() + 1), false, null);
            tsClass.namespace = c.getPackage().getName();
            tsClass.def = true;
            Arrays.stream(c.getTypeParameters()).forEach(t -> tsClass.typeParameters.add(javaToTs(t)));
            PojoInfo pojoInfo = new PojoInfo(c);
            for (PojoProperty property : pojoInfo.getProperties()) {
                tsClass.fields.add(new TsField(property, c));
            }
            return tsClass;
        }

        @Override
        public String getName() {
            return (Common.isBlank(namespace) ? "" : (namespace + ".")) + name;
        }

        public String getSimpleName() {
            return name;
        }

        @Override
        public String toText() {
            String text = array ? componentClass.toText() : getName();
            if (!typeParameters.isEmpty()) {
                text += "<" + typeParameters.stream().map(TsType::toText)
                        .collect(Collectors.joining(", ")) + ">";
            }
            if (array) {
                text += "[]";
            }
            return text;
        }

        @Override
        public boolean isDefinable() {
            return !array && def;
        }

        @Override
        public Map<String, TsType> getDependencies() {
            if (array) return componentClass.getDependencies();
            if (dependenciesContext.contains(toText())) return EMPTY_MAP;

            return Common.cast(dependenciesContext.call(toText(), () -> {
                Map<String, TsType> map = new HashMap<>();
                map.put(toText(), this);
                fields.forEach(f -> {
                    if (f.getType() instanceof TsTypeVariable) return;
                    String label = f.getType().getName();
                    if (dependenciesContext.contains(label)) return;
                    TsType type = f.getType();
                    while (type instanceof TsTypeRef) {
                        type = ((TsTypeRef) type).getActualType();
                    }
                    if (!type.isPrimitive()) {
                        if (label != null)
                            map.putIfAbsent(label, type);
                    }
                    map.putAll(type.getDependencies());
                });
                return map;
            }));

        }

        @Override
        public String toString() {
//            StringBuilder builder = new StringBuilder("declare class ")
//                    .append(getSimpleName());
//            if (!typeParameters.isEmpty()) {
//                builder.append("<")
//                        .append(typeParameters.stream().map(TsType::getName).collect(Collectors.joining(",")))
//                        .append(">");
//            }
//            builder.append("{");
//            fields.forEach(field -> {
//                builder.append("\n\t").append(field.getName()).append("?: ").append(field.getType().toText())
//                        .append(";");
//            });
//            return builder.append("\n}").toString();
            return toDTS("", true);
        }

        @Override
        public String toDTS(String intend, boolean declare) {
            StringBuilder builder = new StringBuilder(intend).append(declare ? "declare " : "").append("class ")
                    .append(getSimpleName());
            if (!typeParameters.isEmpty()) {
                builder.append("<")
                        .append(typeParameters.stream().map(TsType::getName).collect(Collectors.joining(",")))
                        .append(">");
            }
            builder.append("{");
            fields.forEach(field -> {
                field.docLines.forEach(s -> builder.append("\n").append(intend).append("\t").append(s));
                builder.append("\n").append(intend).append("\t").append(field.getName()).append("?: ").append(field.getType().toText())
                        .append(";");
            });
            return builder.append("\n").append(intend).append("}").toString();
        }
    }

    public static class TsTypeVariable implements TsType {
        private final String name;

        public TsTypeVariable(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toText() {
            return name;
        }

        @Override
        public boolean isDefinable() {
            return false;
        }
    }

    public static class TsField {
        private final String name;
        private final TsType type;

        private final List<String> docLines = new ArrayList<>();

        public TsField(PojoProperty property, Type c) {
            this.name = property.getName();
            this.type = javaToTs(property.getType(), c);
            List<String> lines = new ArrayList<>();
            Optional.ofNullable(property.getAnnotation(Description.class))
                    .ifPresent(desc -> {
                        lines.add(" * " + desc.name());
                        if (Common.isBlank(desc.description())) return;
                        Arrays.stream(desc.description().split("\n"))
                                .forEach(s -> lines.add(" * " + s));
                    });
            Optional.ofNullable(property.getAnnotation(Deprecated.class))
                    .ifPresent(deprecated -> lines.add(" * @deprecated "));
            ifEnum(property.getType(), e -> {
                lines.add(" * @see " + e.getCanonicalName());
            });
            if (!lines.isEmpty()) {
                docLines.add("/**");
                docLines.addAll(lines);
                docLines.add(" */");
            }
        }

        @Deprecated
        public TsField(String name, TsType type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public TsType getType() {
            return type;
        }
    }

    public static class TsParameterizedType implements TsType {
        private final List<TsType> parameterTypes = new ArrayList<>();
        //        private TsClass rowType;
        private TsType rowType;
        private boolean collection = false;
        private TsType componentType;


//        private final Set<TsField> fields = new HashSet<>();

        public static TsType of(ParameterizedType parameterizedType, Type... context) {
            TsParameterizedType type = new TsParameterizedType();
            Class<?> rowType = (Class<?>) parameterizedType.getRawType();
            if (Collection.class.isAssignableFrom(rowType)) {
                type.collection = true;
                type.componentType = javaToTs(parameterizedType.getActualTypeArguments()[0], context);
//                type.parameterTypes.add(type.componentType);
                return type;
            }
            if (rowType.getPackage().getName().startsWith("java.")) {
                log.warn("unsupported type: {}", parameterizedType);
                return TsClass.ANY;
            }
            type.rowType = javaToTs(rowType);

            Arrays.stream(parameterizedType.getActualTypeArguments())
                    .forEach(t -> type.parameterTypes.add(javaToTs(t, context)));
//            PojoInfo pojoInfo = new PojoInfo(parameterizedType);
//            for (PojoProperty property : pojoInfo.getProperties()) {
//                type.fields.add(new TsField(property.getName(), javaToTs(property.getType(), c)));
//            }
            return type;

//

        }

        public static TsType of(GenericArrayType genericArrayType, Type... context) {
            TsParameterizedType type = new TsParameterizedType();
            type.collection = true;
            type.componentType = javaToTs(genericArrayType.getGenericComponentType(), context);
            return type;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String toText() {
            try {
                String text = collection ? componentType.toText() : rowType.getName();
                if (!parameterTypes.isEmpty()) {
                    text += "<" + parameterTypes.stream().map(TsType::toText).collect(Collectors.joining(", ")) + ">";
                }
                if (collection) {
                    text += "[]";
                }
                return text;
            } catch (NullPointerException e) {
                System.out.println(collection);
                System.out.println(getName());
                System.out.println(componentType);
                System.out.println(rowType);
                throw e;
            }
        }

        @Override
        public boolean isDefinable() {
            return false;
        }

        @Override
        public Map<String, TsType> getDependencies() {
            if (collection) return componentType.getDependencies();

            Map<String, TsType> tsTypeMap = new HashMap<>();
            if (!dependenciesContext.contains(rowType.toText())) {
                tsTypeMap.putIfAbsent(rowType.toText(), rowType);
                tsTypeMap.putAll(rowType.getDependencies());
            }
            parameterTypes.forEach(t -> tsTypeMap.putAll(t.getDependencies()));
            return tsTypeMap;
        }
    }
}
